package vn.edu.se10.bluemoon.billing.api;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import vn.edu.se10.bluemoon.billing.application.ContributionInput;
import vn.edu.se10.bluemoon.billing.application.VoluntaryCampaignService;
import vn.edu.se10.bluemoon.billing.application.VoluntaryContributionService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.residency.application.ResidentDonorLookup;

@RestController
@RequestMapping("/api/v1")
public class VoluntaryController {
    private final VoluntaryCampaignService campaigns;
    private final VoluntaryContributionService contributions;
    private final ResidentDonorLookup donors;

    public VoluntaryController(VoluntaryCampaignService campaigns, VoluntaryContributionService contributions, ResidentDonorLookup donors) {
        this.campaigns = campaigns;
        this.contributions = contributions;
        this.donors = donors;
    }

    public record CampaignRequest(String name, String description, String opensOn, String closesOn, Long version) { }
    public record VersionRequest(Long version) { }
    public record VoidRequest(Long version, String reason) { }
    public record ContributionRequest(Long residentId, JsonNode amount, String paidOn, String lateReason) { }
    public record DonorResponse(long id, String fullName, String householdCode, String apartmentCode) { }

    @GetMapping("/voluntary-campaigns")
    public Object list(@RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size, @AuthenticationPrincipal AuthenticatedUser actor) {
        return "RESIDENT".equals(actor.role()) ? PageResponse.from(campaigns.listResident(status, page, size))
                : PageResponse.from(campaigns.listStaff(status, page, size));
    }

    @GetMapping("/voluntary-campaigns/{id}")
    public Object detail(@PathVariable long id, @AuthenticationPrincipal AuthenticatedUser actor) {
        return "RESIDENT".equals(actor.role()) ? campaigns.residentDetail(id) : campaigns.staffDetail(id);
    }

    @PostMapping("/voluntary-campaigns")
    public ResponseEntity<StaffCampaignResponse> create(@RequestBody CampaignRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        var campaign = campaigns.create(request.name(), request.description(), date(request.opensOn()), date(request.closesOn()), actor.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(campaigns.staffDetail(campaign.getId()));
    }

    @PatchMapping("/voluntary-campaigns/{id}")
    public StaffCampaignResponse update(@PathVariable long id, @RequestBody CampaignRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        campaigns.update(id, version(request.version()), request.name(), request.description(), date(request.opensOn()), date(request.closesOn()), actor.userId());
        return campaigns.staffDetail(id);
    }

    @PostMapping("/voluntary-campaigns/{id}/publish")
    public StaffCampaignResponse publish(@PathVariable long id, @RequestBody VersionRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        campaigns.publish(id, version(request.version()), actor.userId());
        return campaigns.staffDetail(id);
    }

    @PostMapping("/voluntary-campaigns/{id}/close")
    public StaffCampaignResponse close(@PathVariable long id, @RequestBody VersionRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        campaigns.close(id, version(request.version()), actor.userId());
        return campaigns.staffDetail(id);
    }

    @GetMapping("/voluntary-donors")
    public PageResponse<DonorResponse> donors(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(donors.search(q, page, size).map(d -> new DonorResponse(d.id(), d.fullName(), d.householdCode(), d.apartmentCode())));
    }

    @GetMapping("/voluntary-campaigns/{id}/contributions")
    public PageResponse<ContributionResponse> contributions(@PathVariable long id, @RequestParam(required = false) String status,
                                                            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(contributions.list(id, status, page, size).map(ContributionResponse::from));
    }

    @PostMapping("/voluntary-campaigns/{id}/contributions")
    public ResponseEntity<ContributionResponse> record(@PathVariable long id, @RequestHeader(value = "Idempotency-Key", required = false) String key,
                                                       @RequestBody ContributionRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        if (request.residentId() == null) throw new BadRequestException("VALIDATION");
        if (request.amount() == null || !request.amount().isTextual()) throw new BadRequestException("INVALID_AMOUNT");
        var input = ContributionInput.parse(id, request.residentId(), request.amount().asString(), date(request.paidOn()), request.lateReason(), key);
        var result = contributions.record(input, actor.userId());
        return ResponseEntity.status(result.replay() ? HttpStatus.OK : HttpStatus.CREATED).body(ContributionResponse.from(result.contribution()));
    }

    @PostMapping("/voluntary-contributions/{id}/void")
    public ContributionResponse voidContribution(@PathVariable long id, @RequestBody VoidRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        return ContributionResponse.from(contributions.voidContribution(id, version(request.version()), request.reason(), actor.userId()));
    }

    private static LocalDate date(String value) {
        try {
            if (value == null) throw new DateTimeParseException("missing", "", 0);
            LocalDate date = LocalDate.parse(value);
            if (date.getYear() < 1000 || date.getYear() > 9999) throw new DateTimeParseException("out of range", value, 0);
            return date;
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("INVALID_DATE");
        }
    }

    private static long version(Long value) {
        if (value == null || value < 0) throw new BadRequestException("VALIDATION");
        return value;
    }
}
