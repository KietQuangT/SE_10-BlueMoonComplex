package vn.edu.se10.bluemoon.billing.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.billing.api.ResidentCampaignResponse;
import vn.edu.se10.bluemoon.billing.api.StaffCampaignResponse;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryCampaign;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryContributionRepository;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryCampaignRepository;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;

@Service
public class VoluntaryCampaignService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final VoluntaryCampaignRepository campaigns;
    private final AuditService audit;
    private final VoluntaryContributionRepository contributions;
    private final Clock clock;

    public VoluntaryCampaignService(VoluntaryCampaignRepository campaigns, AuditService audit, Clock clock, VoluntaryContributionRepository contributions) {
        this.campaigns = campaigns;
        this.audit = audit;
        this.contributions = contributions;
        this.clock = clock;
    }

    @Transactional
    public VoluntaryCampaign create(String name, String description, LocalDate opensOn, LocalDate closesOn, long actor) {
        validate(name, description, opensOn, closesOn);
        var campaign = campaigns.saveAndFlush(new VoluntaryCampaign(name.strip(), trim(description), opensOn, closesOn, actor, clock.instant()));
        record(actor, "VOLUNTARY_CAMPAIGN_CREATED", campaign);
        return campaign;
    }

    @Transactional
    public VoluntaryCampaign update(long id, long version, String name, String description, LocalDate opensOn, LocalDate closesOn, long actor) {
        validate(name, description, opensOn, closesOn);
        var campaign = locked(id, version, "DRAFT");
        campaign.update(name.strip(), trim(description), opensOn, closesOn);
        campaigns.flush();
        record(actor, "VOLUNTARY_CAMPAIGN_UPDATED", campaign);
        return campaign;
    }

    @Transactional
    public VoluntaryCampaign publish(long id, long version, long actor) {
        var campaign = locked(id, version, "DRAFT");
        if (campaign.getClosesOn().isBefore(today())) throw new ConflictException("INVALID_STATE");
        campaign.publish(actor, clock.instant());
        campaigns.flush();
        record(actor, "VOLUNTARY_CAMPAIGN_PUBLISHED", campaign);
        return campaign;
    }

    @Transactional
    public VoluntaryCampaign close(long id, long version, long actor) {
        var campaign = locked(id, version, "PUBLISHED");
        campaign.close(actor, clock.instant());
        campaigns.flush();
        record(actor, "VOLUNTARY_CAMPAIGN_CLOSED", campaign);
        return campaign;
    }

    @Transactional(readOnly = true)
    public ResidentCampaignResponse residentDetail(long id) {
        var campaign = get(id);
        if (campaign.getPublishedAt() == null) throw new NotFoundException("CAMPAIGN_NOT_FOUND");
        return resident(campaign);
    }

    @Transactional(readOnly = true)
    public StaffCampaignResponse staffDetail(long id) {
        var campaign = get(id);
        return staff(campaign, totals(List.of(id)).getOrDefault(id, new Total(0, BigDecimal.ZERO)));
    }

    @Transactional(readOnly = true)
    public Page<ResidentCampaignResponse> listResident(String status, int page, int size) {
        return search(true, status, page, size).map(this::resident);
    }

    @Transactional(readOnly = true)
    public Page<StaffCampaignResponse> listStaff(String status, int page, int size) {
        var result = search(false, status, page, size);
        var totals = totals(result.getContent().stream().map(VoluntaryCampaign::getId).toList());
        return result.map(c -> staff(c, totals.getOrDefault(c.getId(), new Total(0, BigDecimal.ZERO))));
    }

    private Page<VoluntaryCampaign> search(boolean publishedOnly, String status, int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (status != null && !List.of("DRAFT", "PUBLISHED", "CLOSED").contains(status))) {
            throw new BadRequestException("INVALID_FILTER");
        }
        return campaigns.search(publishedOnly, status, today(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id")));
    }

    private VoluntaryCampaign get(long id) {
        return campaigns.findById(id).orElseThrow(() -> new NotFoundException("CAMPAIGN_NOT_FOUND"));
    }

    private ResidentCampaignResponse resident(VoluntaryCampaign c) {
        return new ResidentCampaignResponse(c.getId(), c.getName(), c.getDescription(), c.getOpensOn(), c.getClosesOn(), visibleStatus(c));
    }

    private StaffCampaignResponse staff(VoluntaryCampaign c, Total total) {
        return new StaffCampaignResponse(c.getId(), c.getName(), c.getDescription(), c.getOpensOn(), c.getClosesOn(), visibleStatus(c),
                c.getVersion(), c.getCreatedBy(), c.getCreatedAt(), c.getPublishedBy(), c.getPublishedAt(), c.getClosedBy(),
                c.getClosedAt(), total.count(), total.amount().toPlainString());
    }

    private String visibleStatus(VoluntaryCampaign c) {
        return "PUBLISHED".equals(c.getStatus()) && c.getClosesOn().isBefore(today()) ? "CLOSED" : c.getStatus();
    }

    private record Total(long count, BigDecimal amount) { }

    private Map<Long, Total> totals(List<Long> ids) {
        Map<Long, Total> result = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] row : contributions.totals(ids)) {
                result.put(((Number) row[0]).longValue(), new Total(((Number) row[1]).longValue(), (BigDecimal) row[2]));
            }
        }
        return result;
    }

    private VoluntaryCampaign locked(long id, long version, String status) {
        var campaign = campaigns.lockById(id).orElseThrow(() -> new NotFoundException("CAMPAIGN_NOT_FOUND"));
        if (campaign.getVersion() != version) throw new ConflictException("STALE_VERSION");
        if (!status.equals(campaign.getStatus())) throw new ConflictException("INVALID_STATE");
        return campaign;
    }

    private void validate(String name, String description, LocalDate opensOn, LocalDate closesOn) {
        if (name == null || name.isBlank() || name.strip().codePointCount(0, name.strip().length()) > 150 || (description != null && description.strip().codePointCount(0, description.strip().length()) > 2000)) {
            throw new BadRequestException("VALIDATION");
        }
        if (opensOn == null || closesOn == null || closesOn.isBefore(opensOn)) throw new BadRequestException("INVALID_DATE");
    }

    private void record(long actor, String action, VoluntaryCampaign campaign) {
        audit.record(actor, action, "VoluntaryCampaign", campaign.getId().toString(), Map.of(
                "status", campaign.getStatus(), "opensOn", campaign.getOpensOn().toString(), "closesOn", campaign.getClosesOn().toString()));
    }

    private static String trim(String value) { return value == null ? null : value.strip(); }
    private LocalDate today() { return LocalDate.now(clock.withZone(BUSINESS_ZONE)); }
}
