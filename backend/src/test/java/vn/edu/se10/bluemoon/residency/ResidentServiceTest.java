package vn.edu.se10.bluemoon.residency;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.*;
import vn.edu.se10.bluemoon.residency.api.*;
import vn.edu.se10.bluemoon.residency.application.ResidentService;
import vn.edu.se10.bluemoon.residency.persistence.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class ResidentServiceTest {
 ResidentRepository residents=mock(ResidentRepository.class); HouseholdRepository households=mock(HouseholdRepository.class); ResidencyChangeRepository changes=mock(ResidencyChangeRepository.class); AuditService audit=mock(AuditService.class);
 Clock clock=Clock.fixed(Instant.parse("2026-11-06T18:00:00Z"),ZoneOffset.UTC);
 ResidentService service=new ResidentService(residents,households,changes,audit,clock);
 ResidentRequest request(String identity){return new ResidentRequest("Name",identity,null,null,null);}
 @Test void rejectsInactiveHousehold(){Household h=mock(Household.class);when(h.getStatus()).thenReturn("INACTIVE");when(households.findById(1L)).thenReturn(Optional.of(h));when(households.lockIdentityGuard()).thenReturn(1L);assertThatThrownBy(()->service.create(1,request(null),1)).isInstanceOfSatisfying(BadRequestException.class,e->assertThat(e.code()).isEqualTo("HOUSEHOLD_INVALID"));verify(households).lockIdentityGuard();verify(residents,never()).saveAndFlush(any());verifyNoInteractions(audit);}
 @Test void rejectsIdentityAlreadyUsed(){Household h=mock(Household.class);when(h.getStatus()).thenReturn("ACTIVE");when(households.findById(1L)).thenReturn(Optional.of(h));when(households.lockIdentityGuard()).thenReturn(1L);when(residents.findConflicts(anyString(),anyCollection(),isNull())).thenReturn(List.of(mock(Resident.class)));assertThatThrownBy(()->service.create(1,request("secret"),1)).isInstanceOfSatisfying(ConflictException.class,e->{assertThat(e.code()).isEqualTo("IDENTITY_NUMBER_ALREADY_USED");assertThat(e.getMessage()).doesNotContain("secret");});verify(households).lockIdentityGuard();verify(residents,never()).saveAndFlush(any());verifyNoInteractions(audit);}
 @Test void rejectsSecondMoveOut(){Resident r=new Resident(1,"Name",null,null,null,null,clock.instant());r.moveOut(clock.instant());when(households.lockIdentityGuard()).thenReturn(1L);ReflectionTestUtils.setField(r,"id",1L);when(residents.lockById(1)).thenReturn(Optional.of(r));assertThatThrownBy(()->service.moveOut(1,new MoveOutRequest(LocalDate.of(2026,11,6),null),1)).isInstanceOfSatisfying(ConflictException.class,e->assertThat(e.code()).isEqualTo("RESIDENT_ALREADY_MOVED_OUT"));verify(households).lockIdentityGuard();verifyNoInteractions(changes,audit);}
 @Test void vietnamCalendarBoundaryForBirthAndMoveOut(){
  when(households.lockIdentityGuard()).thenReturn(1L);
  Resident r=new Resident(1,"Name",null,null,null,null,clock.instant());ReflectionTestUtils.setField(r,"id",1L);when(residents.lockById(1)).thenReturn(Optional.of(r));
  ResidentService.validate(new ResidentRequest("Name",null,LocalDate.of(2026,11,7),null,null),true,clock);
  assertThatThrownBy(()->ResidentService.validate(new ResidentRequest("Name",null,LocalDate.of(2026,11,8),null,null),true,clock)).isInstanceOf(BadRequestException.class);
  service.moveOut(1,new MoveOutRequest(LocalDate.of(2026,11,7),null),1);
  assertThatThrownBy(()->service.moveOut(1,new MoveOutRequest(LocalDate.of(2026,11,8),null),1)).isInstanceOf(BadRequestException.class);
 }
 @Test void updateLocksGuardBeforeResident(){
  when(households.lockIdentityGuard()).thenReturn(1L);Resident r=new Resident(1,"Name",null,null,null,null,clock.instant());ReflectionTestUtils.setField(r,"id",1L);when(residents.lockById(1)).thenReturn(Optional.of(r));
  service.update(1,request(null),1);var order=inOrder(households,residents);order.verify(households).lockIdentityGuard();order.verify(residents).lockById(1);
 }
 @Test void moveOutLocksGuardBeforeResident(){
  when(households.lockIdentityGuard()).thenReturn(1L);Resident r=new Resident(1,"Name",null,null,null,null,clock.instant());ReflectionTestUtils.setField(r,"id",1L);when(residents.lockById(1)).thenReturn(Optional.of(r));
  service.moveOut(1,new MoveOutRequest(LocalDate.of(2026,11,7),null),1);var order=inOrder(households,residents);order.verify(households).lockIdentityGuard();order.verify(residents).lockById(1);
 }
 @Test void excessiveReasonUsesValidationCode(){when(households.lockIdentityGuard()).thenReturn(1L);assertThatThrownBy(()->service.moveOut(1,new MoveOutRequest(LocalDate.of(2026,11,7),"x".repeat(256)),1)).isInstanceOfSatisfying(BadRequestException.class,e->assertThat(e.code()).isEqualTo("VALIDATION"));}
 @Test void moveOutCancelsPendingAbsencesAndAuditsCause(){
  when(households.lockIdentityGuard()).thenReturn(1L); Resident r=new Resident(1,"Name",null,null,null,null,clock.instant()); ReflectionTestUtils.setField(r,"id",1L); when(residents.lockById(1)).thenReturn(Optional.of(r));
  ResidencyChange c=ResidencyChange.absence(r,LocalDate.of(2026,11,7),null,"private reason",7L,clock.instant()); ReflectionTestUtils.setField(c,"id",42L);
  when(changes.findByResidentIdAndChangeTypeAndStatus(1L,"TEMPORARY_ABSENCE","PENDING")).thenReturn(List.of(c));
  service.moveOut(1,new MoveOutRequest(LocalDate.of(2026,11,7),null),9L);
  assertThat(c.getStatus()).isEqualTo("CANCELLED"); assertThat(c.getDecidedAt()).isEqualTo(clock.instant()); assertThat(c.getDecisionNote()).isNull();
  verify(audit).record(9L,"ABSENCE_CANCELLED","ResidencyChange","42",Map.of("id","42","residentId","1","householdId","1","effectiveDate","2026-11-07","cause","MOVE_OUT"));
 }
 @Test void moveOutWithoutPendingAbsenceDoesNotAuditCancellation(){
  when(households.lockIdentityGuard()).thenReturn(1L); Resident r=new Resident(1,"Name",null,null,null,null,clock.instant()); ReflectionTestUtils.setField(r,"id",1L); when(residents.lockById(1)).thenReturn(Optional.of(r));
  service.moveOut(1,new MoveOutRequest(LocalDate.of(2026,11,7),null),9L); verify(audit,never()).record(anyLong(),eq("ABSENCE_CANCELLED"),anyString(),anyString(),anyMap());
 }
 @Test void moveOutAllowsTemporarilyAbsentResident(){
  when(households.lockIdentityGuard()).thenReturn(1L); Resident r=new Resident(1,"Name",null,null,null,null,clock.instant()); ReflectionTestUtils.setField(r,"id",1L); r.setResidencyStatus(ResidencyStatus.TEMPORARILY_ABSENT,clock.instant()); when(residents.lockById(1)).thenReturn(Optional.of(r));
  service.moveOut(1,new MoveOutRequest(LocalDate.of(2026,11,7),null),9L); assertThat(r.getResidencyStatus()).isEqualTo(ResidencyStatus.MOVED_OUT);
 }
}
