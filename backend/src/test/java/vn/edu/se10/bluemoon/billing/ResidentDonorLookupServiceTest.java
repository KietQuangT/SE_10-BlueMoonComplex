package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.residency.application.ResidentDonorLookupService;
import vn.edu.se10.bluemoon.residency.persistence.ResidentRepository;

class ResidentDonorLookupServiceTest {
    @Test
    void searchNeedsTwoUnicodeCharacters() {
        var service = new ResidentDonorLookupService(mock(ResidentRepository.class));
        assertEquals("INVALID_FILTER", assertThrows(BadRequestException.class,
                () -> service.search("😀", 0, 20)).code());
    }
}
