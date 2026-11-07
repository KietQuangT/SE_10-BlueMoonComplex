package vn.edu.se10.bluemoon.residency;
import java.time.*;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.residency.api.DeclarationRequest;
import vn.edu.se10.bluemoon.residency.application.DeclarationService;
import vn.edu.se10.bluemoon.residency.persistence.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class DeclarationServiceTest {
 ResidentRepository residents=mock(ResidentRepository.class); HouseholdRepository households=mock(HouseholdRepository.class); ResidencyChangeRepository changes=mock(ResidencyChangeRepository.class); AuditService audit=mock(AuditService.class);
 Clock clock=Clock.fixed(Instant.parse("2026-11-06T18:00:00Z"),ZoneOffset.UTC);
 DeclarationService service=new DeclarationService(residents,households,changes,audit,clock);
 @Test void rejectsPastAndEndBeforeStartAtVietnamBoundary(){
  assertThatThrownBy(()->service.create(new DeclarationRequest(1L,LocalDate.of(2026,11,6),null,"reason"),1L,1)).isInstanceOfSatisfying(BadRequestException.class, e -> assertThat(e.code()).isEqualTo("INVALID_DATE"));
  assertThatThrownBy(()->service.create(new DeclarationRequest(1L,LocalDate.of(2026,11,7),LocalDate.of(2026,11,6),"reason"),1L,1)).isInstanceOfSatisfying(BadRequestException.class, e -> assertThat(e.code()).isEqualTo("INVALID_DATE"));
 }
 @Test void createApproveAndReturnLockGuardBeforeResident(){
  Resident r=new Resident(1,"Name",null,null,null,null,clock.instant());ReflectionTestUtils.setField(r,"id",1L);
  Household h=mock(Household.class);when(h.getId()).thenReturn(1L);when(households.findById(1L)).thenReturn(Optional.of(h));when(residents.findById(1L)).thenReturn(Optional.of(r));when(residents.lockById(1L)).thenReturn(Optional.of(r));when(households.lockIdentityGuard()).thenReturn(1L);
  when(changes.saveAndFlush(any())).thenAnswer(i->{ResidencyChange c=i.getArgument(0);ReflectionTestUtils.setField(c,"id",10L);return c;});
  service.create(new DeclarationRequest(1L,LocalDate.of(2026,11,7),null,"reason"),1L,7L);
  var order=inOrder(households,residents);order.verify(households).lockIdentityGuard();order.verify(residents).lockById(1L);
  clearInvocations(households,residents);ResidencyChange c=ResidencyChange.absence(r,LocalDate.of(2026,11,7),null,"reason",7L,clock.instant());ReflectionTestUtils.setField(c,"id",10L);when(changes.lockById(10L)).thenReturn(Optional.of(c));
  service.approve(10L,9L);order=inOrder(households,residents);order.verify(households).lockIdentityGuard();order.verify(residents).lockById(1L);assertThat(r.getResidencyStatus()).isEqualTo(ResidencyStatus.TEMPORARILY_ABSENT);
  clearInvocations(households,residents);service.returnResident(1L,9L);order=inOrder(households,residents);order.verify(households).lockIdentityGuard();order.verify(residents).lockById(1L);assertThat(r.getResidencyStatus()).isEqualTo(ResidencyStatus.RESIDING);
 }
}
