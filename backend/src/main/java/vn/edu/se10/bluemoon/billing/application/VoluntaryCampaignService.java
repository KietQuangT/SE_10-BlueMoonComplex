package vn.edu.se10.bluemoon.billing.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryCampaign;
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
    private final Clock clock;

    public VoluntaryCampaignService(VoluntaryCampaignRepository campaigns, AuditService audit, Clock clock) {
        this.campaigns = campaigns;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public VoluntaryCampaign create(String name, String description, LocalDate opensOn, LocalDate closesOn, long actor) {
        validate(name, description, opensOn, closesOn);
        var campaign = campaigns.saveAndFlush(new VoluntaryCampaign(name.trim(), trim(description), opensOn, closesOn, actor, clock.instant()));
        record(actor, "VOLUNTARY_CAMPAIGN_CREATED", campaign);
        return campaign;
    }

    @Transactional
    public VoluntaryCampaign update(long id, long version, String name, String description, LocalDate opensOn, LocalDate closesOn, long actor) {
        validate(name, description, opensOn, closesOn);
        var campaign = locked(id, version, "DRAFT");
        campaign.update(name.trim(), trim(description), opensOn, closesOn);
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

    private VoluntaryCampaign locked(long id, long version, String status) {
        var campaign = campaigns.lockById(id).orElseThrow(() -> new NotFoundException("CAMPAIGN_NOT_FOUND"));
        if (campaign.getVersion() != version) throw new ConflictException("STALE_VERSION");
        if (!status.equals(campaign.getStatus())) throw new ConflictException("INVALID_STATE");
        return campaign;
    }

    private void validate(String name, String description, LocalDate opensOn, LocalDate closesOn) {
        if (name == null || name.isBlank() || name.trim().length() > 150 || (description != null && description.trim().length() > 2000)) {
            throw new BadRequestException("VALIDATION");
        }
        if (opensOn == null || closesOn == null || closesOn.isBefore(opensOn)) throw new BadRequestException("INVALID_DATE");
    }

    private void record(long actor, String action, VoluntaryCampaign campaign) {
        audit.record(actor, action, "VoluntaryCampaign", campaign.getId().toString(), Map.of(
                "status", campaign.getStatus(), "opensOn", campaign.getOpensOn().toString(), "closesOn", campaign.getClosesOn().toString()));
    }

    private static String trim(String value) { return value == null ? null : value.trim(); }
    private LocalDate today() { return LocalDate.now(clock.withZone(BUSINESS_ZONE)); }
}
