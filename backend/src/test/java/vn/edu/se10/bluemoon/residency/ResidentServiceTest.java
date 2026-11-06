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
 Clock clock=Clock.fixed(Instant.parse("2026-11-06T12:00:00Z"),ZoneOffset.UTC);
 ResidentService service=new ResidentService(residents,households,changes,audit,clock);
 ResidentRequest request(String identity){return new ResidentRequest("Name",identity,null,null,null);}
 @Test void rejectsInactiveHousehold(){Household h=mock(Household.class);when(h.getStatus()).thenReturn("INACTIVE");when(households.findById(1L)).thenReturn(Optional.of(h));assertThatThrownBy(()->service.create(1,request(null),1)).isInstanceOfSatisfying(BadRequestException.class,e->assertThat(e.code()).isEqualTo("HOUSEHOLD_INVALID"));verifyNoInteractions(residents,audit);}
 @Test void rejectsIdentityAlreadyUsed(){Household h=mock(Household.class);when(h.getStatus()).thenReturn("ACTIVE");when(households.findById(1L)).thenReturn(Optional.of(h));when(households.lockIdentityGuard()).thenReturn(1L);when(residents.findConflicts(anyString(),anyCollection(),isNull())).thenReturn(List.of(mock(Resident.class)));assertThatThrownBy(()->service.create(1,request("secret"),1)).isInstanceOfSatisfying(ConflictException.class,e->{assertThat(e.code()).isEqualTo("IDENTITY_NUMBER_ALREADY_USED");assertThat(e.getMessage()).doesNotContain("secret");});verifyNoInteractions(audit);}
 @Test void rejectsSecondMoveOut(){Resident r=new Resident(1,"Name",null,null,null,null,clock.instant());r.moveOut(clock.instant());when(residents.lockById(1)).thenReturn(Optional.of(r));assertThatThrownBy(()->service.moveOut(1,new MoveOutRequest(LocalDate.of(2026,11,6),null),1)).isInstanceOfSatisfying(ConflictException.class,e->assertThat(e.code()).isEqualTo("RESIDENT_ALREADY_MOVED_OUT"));verifyNoInteractions(changes,audit);}
 @Test void rejectsFutureDatesUsingVietnamDate(){assertThatThrownBy(()->service.create(1,new ResidentRequest("Name",null,LocalDate.of(2026,11,7),null,null),1)).isInstanceOf(BadRequestException.class);assertThatThrownBy(()->service.moveOut(1,new MoveOutRequest(LocalDate.of(2026,11,7),null),1)).isInstanceOf(BadRequestException.class);verifyNoInteractions(residents,households,changes,audit);}
}
