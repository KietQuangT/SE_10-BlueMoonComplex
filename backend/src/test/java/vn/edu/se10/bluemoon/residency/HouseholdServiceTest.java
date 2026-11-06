package vn.edu.se10.bluemoon.residency;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;
import vn.edu.se10.bluemoon.residency.api.UpdateHouseholdRequest;
import vn.edu.se10.bluemoon.residency.application.HouseholdService;
import vn.edu.se10.bluemoon.residency.persistence.HouseholdRepository;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class HouseholdServiceTest {
 HouseholdRepository repository=mock(HouseholdRepository.class); AuditService audit=mock(AuditService.class);
 HouseholdService service=new HouseholdService(repository,audit,Clock.systemUTC());
 @Test void rejectsInvalidPaginationAndStatus() {
  for(int size:new int[]{0,101}) assertThatThrownBy(()->service.list(null,null,null,0,size)).isInstanceOf(BadRequestException.class);
  assertThatThrownBy(()->service.list(null,null,null,-1,10)).isInstanceOf(BadRequestException.class);
  assertThatThrownBy(()->service.list(null,null,"OTHER",0,10)).isInstanceOf(BadRequestException.class);
  verifyNoInteractions(repository,audit);
 }
 @Test void rejectsEmptyUpdateAndMissingHousehold() {
  assertThatThrownBy(()->service.update(1,new UpdateHouseholdRequest(null,null),1)).isInstanceOf(BadRequestException.class);
  assertThatThrownBy(()->service.get(1)).isInstanceOf(NotFoundException.class);
  verifyNoInteractions(audit);
 }
}
