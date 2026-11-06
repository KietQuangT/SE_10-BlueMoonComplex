package vn.edu.se10.bluemoon.residency;
import java.time.*;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.residency.api.*;
import vn.edu.se10.bluemoon.residency.application.ResidentService;
import vn.edu.se10.bluemoon.common.error.NotFoundException;
import vn.edu.se10.bluemoon.residency.persistence.ResidencyStatus;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(ResidentController.class) @Import(SecurityConfig.class)
class ResidentApiTest {
 @Autowired MockMvc mvc; @MockitoBean ResidentService service; @MockitoBean Clock clock;
 @BeforeEach void setup(){when(clock.withZone(any())).thenReturn(Clock.fixed(Instant.parse("2026-11-06T12:00:00Z"),ZoneOffset.UTC));}
 private static UsernamePasswordAuthenticationToken principal(String role,boolean change){return UsernamePasswordAuthenticationToken.authenticated(new AuthenticatedUser(1,"user","User",role,1L,change),null,List.of(new SimpleGrantedAuthority("ROLE_"+role)));}
 private static final String BODY="{\"fullName\":\"Name\"}", MOVE="{\"effectiveDate\":\"2026-11-06\"}";
 @Test void residentMeRouteIsAuthorizedBeforeHouseholdWildcard() throws Exception {
  when(service.mine(1L)).thenReturn(List.of(new MyResidentResponse(2L,"Name",null,null,ResidencyStatus.RESIDING)));
  mvc.perform(get("/api/v1/households/me/residents").with(authentication(principal("RESIDENT",false)))).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(2)).andExpect(jsonPath("$[0].identityNumber").doesNotExist()).andExpect(jsonPath("$[0].phone").doesNotExist());
  verify(service).mine(1L);
 }
 @Test void routesEnforceAuthenticationRolesCsrfAndPasswordChange() throws Exception {
  for(String path:List.of("/api/v1/households/1/residents","/api/v1/households/1/residency-changes","/api/v1/residents/1","/api/v1/households/me/residents")) {
   mvc.perform(get(path)).andExpect(status().isUnauthorized());
   mvc.perform(get(path).with(authentication(principal("ACCOUNTANT",false)))).andExpect(status().isForbidden());
   mvc.perform(get(path).with(authentication(principal(path.contains("/me/")?"ADMIN":"RESIDENT",false)))).andExpect(status().isForbidden());
   mvc.perform(get(path).with(authentication(principal(path.contains("/me/")?"RESIDENT":"ADMIN",true)))).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
  }
  for(String path:List.of("/api/v1/households/1/residents","/api/v1/residents/1/move-out")) {
   String body=path.endsWith("move-out")?MOVE:BODY;
   mvc.perform(post(path).with(csrf()).contentType("application/json").content(body)).andExpect(status().isUnauthorized());
   for(String role:List.of("RESIDENT","ACCOUNTANT"))mvc.perform(post(path).with(authentication(principal(role,false))).with(csrf()).contentType("application/json").content(body)).andExpect(status().isForbidden());
   mvc.perform(post(path).with(authentication(principal("ADMIN",false))).contentType("application/json").content(body)).andExpect(status().isForbidden());
   mvc.perform(post(path).with(authentication(principal("ADMIN",false))).with(csrf().useInvalidToken()).contentType("application/json").content(body)).andExpect(status().isForbidden());
   mvc.perform(post(path).with(authentication(principal("ADMIN",true))).with(csrf()).contentType("application/json").content(body)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
  }
  String path="/api/v1/residents/1";
  mvc.perform(patch(path).with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isUnauthorized());
  for(String role:List.of("RESIDENT","ACCOUNTANT"))mvc.perform(patch(path).with(authentication(principal(role,false))).with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isForbidden());
  mvc.perform(patch(path).with(authentication(principal("ADMIN",false))).contentType("application/json").content(BODY)).andExpect(status().isForbidden());
  mvc.perform(patch(path).with(authentication(principal("ADMIN",false))).with(csrf().useInvalidToken()).contentType("application/json").content(BODY)).andExpect(status().isForbidden());
  mvc.perform(patch(path).with(authentication(principal("ADMIN",true))).with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
  mvc.perform(delete(path).with(authentication(principal("ADMIN",false))).with(csrf())).andExpect(status().isForbidden());
  verifyNoInteractions(service);
 }
 @Test void invalidBodiesAreRejectedBeforeService() throws Exception {
  var admin=authentication(principal("ADMIN",false));
  for(String body:List.of("{}","{\"fullName\":\"  \"}","{\"fullName\":\""+"x".repeat(151)+"\"}","{\"fullName\":\"Name\",\"dateOfBirth\":\"2026-11-07\"}"))mvc.perform(post("/api/v1/households/1/residents").with(admin).with(csrf()).contentType("application/json").content(body)).andExpect(status().isBadRequest());
  mvc.perform(patch("/api/v1/residents/1").with(admin).with(csrf()).contentType("application/json").content("{}" )).andExpect(status().isBadRequest());
  for(String body:List.of("{}","{\"effectiveDate\":\"2026-11-07\"}"))mvc.perform(post("/api/v1/residents/1/move-out").with(admin).with(csrf()).contentType("application/json").content(body)).andExpect(status().isBadRequest());
  verifyNoInteractions(service);
 }
 @Test void notFoundAndSuccessResponses() throws Exception {
  var admin=authentication(principal("ADMIN",false));
  when(service.get(9)).thenThrow(new NotFoundException("RESIDENT_NOT_FOUND"));
  when(service.list(eq(9L),isNull(),eq(0),eq(20))).thenThrow(new NotFoundException("HOUSEHOLD_NOT_FOUND"));
  mvc.perform(get("/api/v1/residents/9").with(admin)).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("RESIDENT_NOT_FOUND"));
  mvc.perform(get("/api/v1/households/9/residents").with(admin)).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("HOUSEHOLD_NOT_FOUND"));
  var response=new ResidentResponse(1L,1L,"Name","secret",null,"phone",null,ResidencyStatus.RESIDING,Instant.EPOCH,Instant.EPOCH);
  when(service.update(eq(1L),any(),eq(1L))).thenReturn(response);when(service.moveOut(eq(1L),any(),eq(1L))).thenReturn(response);
  when(service.create(eq(1L),any(),eq(1L))).thenReturn(response);
  when(service.list(eq(1L),isNull(),eq(0),eq(20))).thenReturn(new PageImpl<>(List.of(response),PageRequest.of(0,20),1));
  when(service.history(1L,0,20)).thenReturn(new PageImpl<>(List.of(new ResidencyChangeResponse(2L,1L,"MOVE_OUT",LocalDate.of(2020,1,1),"APPROVED",1L,Instant.EPOCH)),PageRequest.of(0,20),1));
  mvc.perform(post("/api/v1/households/1/residents").with(admin).with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isCreated()).andExpect(jsonPath("$.householdId").value(1));
  mvc.perform(get("/api/v1/households/1/residents").with(admin)).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(1)).andExpect(jsonPath("$.totalElements").value(1));
  mvc.perform(get("/api/v1/households/1/residency-changes").with(admin)).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(2)).andExpect(jsonPath("$.content[0].reason").doesNotExist());
  mvc.perform(patch("/api/v1/residents/1").with(admin).with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isOk()).andExpect(jsonPath("$.identityNumber").value("secret"));
  mvc.perform(post("/api/v1/residents/1/move-out").with(admin).with(csrf()).contentType("application/json").content(MOVE)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
 }
}
