package vn.edu.se10.bluemoon.residency;
import java.util.List;
import java.math.BigDecimal;
import java.time.Instant;
import vn.edu.se10.bluemoon.residency.api.HouseholdResponse;
import static org.mockito.ArgumentMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.residency.api.HouseholdController;
import vn.edu.se10.bluemoon.residency.application.HouseholdService;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(HouseholdController.class) @Import(SecurityConfig.class)
class HouseholdApiTest {
 @Autowired MockMvc mvc; @MockitoBean HouseholdService service;
 private static final String BODY="{\"householdCode\":\"H1\",\"apartmentCode\":\"A1\",\"areaM2\":20}";
 private static UsernamePasswordAuthenticationToken principal(String role,boolean change){return UsernamePasswordAuthenticationToken.authenticated(new AuthenticatedUser(1,"admin","Admin",role,null,change),null,List.of(new SimpleGrantedAuthority("ROLE_"+role)));}
 @Test void authorizationAndCsrf() throws Exception {
  mvc.perform(get("/api/v1/households")).andExpect(status().isUnauthorized());
  mvc.perform(get("/api/v1/households/1")).andExpect(status().isUnauthorized());
  mvc.perform(post("/api/v1/households").with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isUnauthorized());
  mvc.perform(patch("/api/v1/households/1").with(csrf()).contentType("application/json").content("{\"areaM2\":1}")).andExpect(status().isUnauthorized());
  for(String role:List.of("RESIDENT","ACCOUNTANT")) {
   var auth=authentication(principal(role,false));
   mvc.perform(get("/api/v1/households").with(auth)).andExpect(status().isForbidden());
   mvc.perform(get("/api/v1/households/1").with(auth)).andExpect(status().isForbidden());
   mvc.perform(post("/api/v1/households").with(auth).with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isForbidden());
   mvc.perform(patch("/api/v1/households/1").with(auth).with(csrf()).contentType("application/json").content("{\"areaM2\":1}")).andExpect(status().isForbidden());
  }
  var admin=authentication(principal("ADMIN",false));
  mvc.perform(post("/api/v1/households").with(admin).contentType("application/json").content(BODY)).andExpect(status().isForbidden());
  mvc.perform(patch("/api/v1/households/1").with(admin).with(csrf().useInvalidToken()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
  mvc.perform(delete("/api/v1/households/1").with(admin).with(csrf())).andExpect(status().isForbidden());
  mvc.perform(get("/api/v1/households").with(authentication(principal("ADMIN",true)))).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
  verifyNoInteractions(service);
 }
 @Test void invalidBodyDoesNotReachService() throws Exception {
  var admin=authentication(principal("ADMIN",false));
  for(String body:List.of("{}",BODY.replace("20","0"),BODY.replace("20","1.001"),BODY.replace("20","100000000"),BODY.replace("H1","x".repeat(51)),BODY.replace("A1","x".repeat(51))))
   mvc.perform(post("/api/v1/households").with(admin).with(csrf()).contentType("application/json").content(body)).andExpect(status().isBadRequest());
  mvc.perform(patch("/api/v1/households/1").with(admin).with(csrf()).contentType("application/json").content("{}")).andExpect(status().isBadRequest());
  verifyNoInteractions(service);
 }
 @Test void responseContainsOnlyContractFields() throws Exception {
  when(service.create(any(),eq(1L))).thenReturn(new HouseholdResponse(1L,"H1","A1",new BigDecimal("20.00"),null,"ACTIVE",Instant.EPOCH,Instant.EPOCH));
  var result=mvc.perform(post("/api/v1/households").with(authentication(principal("ADMIN",false))).with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("ACTIVE")).andReturn();
  org.assertj.core.api.Assertions.assertThat(tools.jackson.databind.json.JsonMapper.builder().build().readTree(result.getResponse().getContentAsString()).size()).isEqualTo(8);
 }

}
