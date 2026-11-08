package vn.edu.se10.bluemoon.billing.application;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryCampaign;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryCampaignRepository;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryContribution;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryContributionRepository;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;
import vn.edu.se10.bluemoon.residency.application.ResidentDonorLookup;

@Service
public class VoluntaryContributionService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final VoluntaryContributionRepository contributions;
    private final VoluntaryCampaignRepository campaigns;
    private final ResidentDonorLookup donors;
    private final AuditService audit;
    private final Clock clock;
    private final TransactionTemplate transactions;

    public VoluntaryContributionService(VoluntaryContributionRepository contributions, VoluntaryCampaignRepository campaigns,
                                        ResidentDonorLookup donors, AuditService audit, Clock clock, PlatformTransactionManager manager) {
        this.contributions = contributions;
        this.campaigns = campaigns;
        this.donors = donors;
        this.audit = audit;
        this.clock = clock;
        this.transactions = new TransactionTemplate(manager);
    }

    public record Result(VoluntaryContribution contribution, boolean replay) { }

    @Transactional(propagation = Propagation.NEVER)
    public Result record(ContributionInput input, long actor) {
        Optional<Result> prior = replay(input, actor);
        if (prior.isPresent()) return prior.get();
        try {
            return transactions.execute(status -> recordNew(input, actor));
        } catch (DataIntegrityViolationException failure) {
            if (!duplicateKey(failure)) throw failure;
            return replay(input, actor).orElseThrow(() -> failure);
        }
    }

    private Result recordNew(ContributionInput input, long actor) {
        Optional<Result> prior = replay(input, actor);
        if (prior.isPresent()) return prior.get();
        VoluntaryCampaign campaign = campaigns.lockById(input.campaignId())
                .orElseThrow(() -> new NotFoundException("CAMPAIGN_NOT_FOUND"));
        validateWindow(campaign, input);
        var donor = donors.lockById(input.residentId()).orElseThrow(() -> new NotFoundException("RESIDENT_NOT_FOUND"));
        var contribution = contributions.saveAndFlush(new VoluntaryContribution(input.campaignId(), donor.id(), donor.householdId(),
                donor.fullName(), donor.householdCode(), donor.apartmentCode(), input.amount(), input.paidOn(),
                input.lateReason(), input.idempotencyKey(), input.fingerprint(), actor, clock.instant()));
        audit.record(actor, "VOLUNTARY_CONTRIBUTION_RECORDED", "VoluntaryContribution", contribution.getId().toString(),
                Map.of("campaignId", Long.toString(campaign.getId()), "amount", input.amount().toPlainString(),
                        "paidOn", input.paidOn().toString(), "status", "POSTED"));
        return new Result(contribution, false);
    }

    @Transactional
    public VoluntaryContribution voidContribution(long id, long version, String reason, long actor) {
        String normalizedReason = requiredReason(reason);
        long campaignId = contributions.findCampaignId(id).orElseThrow(() -> new NotFoundException("CONTRIBUTION_NOT_FOUND"));
        campaigns.lockById(campaignId).orElseThrow(() -> new NotFoundException("CAMPAIGN_NOT_FOUND"));
        var contribution = contributions.lockById(id).orElseThrow(() -> new NotFoundException("CONTRIBUTION_NOT_FOUND"));
        if ("VOIDED".equals(contribution.getStatus())) return contribution;
        if (contribution.getVersion() != version) throw new ConflictException("STALE_VERSION");
        contribution.markVoided(actor, clock.instant(), normalizedReason);
        contributions.flush();
        audit.record(actor, "VOLUNTARY_CONTRIBUTION_VOIDED", "VoluntaryContribution", Long.toString(id),
                Map.of("campaignId", Long.toString(campaignId), "amount", contribution.getAmount().toPlainString(), "status", "VOIDED"));
        return contribution;
    }

    @Transactional(readOnly = true)
    public Page<VoluntaryContribution> list(long campaignId, String status, int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (status != null && !java.util.List.of("POSTED", "VOIDED").contains(status))) {
            throw new BadRequestException("INVALID_FILTER");
        }
        if (!campaigns.existsById(campaignId)) throw new NotFoundException("CAMPAIGN_NOT_FOUND");
        return contributions.search(campaignId, status, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "recordedAt", "id")));
    }

    private Optional<Result> replay(ContributionInput input, long actor) {
        return contributions.findByRecordedByAndIdempotencyKey(actor, input.idempotencyKey()).map(existing -> {
            if (!existing.getRequestFingerprint().equals(input.fingerprint())) throw new ConflictException("IDEMPOTENCY_CONFLICT");
            return new Result(existing, true);
        });
    }

    private void validateWindow(VoluntaryCampaign campaign, ContributionInput input) {
        if (campaign.getPublishedAt() == null || "DRAFT".equals(campaign.getStatus())) {
            throw new ConflictException("CAMPAIGN_NOT_RECEIVABLE");
        }
        LocalDate published = campaign.getPublishedAt().atZone(BUSINESS_ZONE).toLocalDate();
        LocalDate closed = campaign.getClosedAt() == null ? null : campaign.getClosedAt().atZone(BUSINESS_ZONE).toLocalDate();
        LocalDate today = LocalDate.now(clock.withZone(BUSINESS_ZONE));
        var window = new VoluntaryReceiptWindow(campaign.getOpensOn(), campaign.getClosesOn(), published, closed);
        if (!window.accepts(input.paidOn(), today)) throw new ConflictException("INVALID_PAID_DATE");
        if (window.requiresLateReason(today) && input.lateReason() == null) throw new BadRequestException("REASON_REQUIRED");
    }

    private static String requiredReason(String reason) {
        if (reason == null || reason.isBlank() || reason.trim().length() > 255) throw new BadRequestException("REASON_REQUIRED");
        return reason.trim();
    }

    private static boolean duplicateKey(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && sql.getErrorCode() == 1062
                    && sql.getMessage() != null && sql.getMessage().contains("uq_contribution_actor_key")) return true;
        }
        return false;
    }
}
