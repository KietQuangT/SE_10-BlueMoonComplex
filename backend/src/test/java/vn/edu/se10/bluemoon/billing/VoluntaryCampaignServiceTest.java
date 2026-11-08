package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.se10.bluemoon.billing.application.VoluntaryCampaignService;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryCampaign;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryCampaignRepository;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;

class VoluntaryCampaignServiceTest {
    private final VoluntaryCampaignRepository campaigns = mock(VoluntaryCampaignRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-08T00:00:00Z"), ZoneOffset.UTC);
    private final VoluntaryCampaignService service = new VoluntaryCampaignService(campaigns, mock(AuditService.class), clock);

    @Test
    void invalidCampaignDatesAreRejected() {
        assertEquals("INVALID_DATE", assertThrows(BadRequestException.class,
                () -> service.create("Test", null, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 9), 1)).code());
    }

    @Test
    void expiredCampaignCannotBePublished() {
        var campaign = new VoluntaryCampaign("Test", null, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 7), 1, clock.instant());
        ReflectionTestUtils.setField(campaign, "id", 2L);
        when(campaigns.lockById(2)).thenReturn(Optional.of(campaign));
        assertEquals("INVALID_STATE", assertThrows(ConflictException.class, () -> service.publish(2, 0, 1)).code());
    }
}
