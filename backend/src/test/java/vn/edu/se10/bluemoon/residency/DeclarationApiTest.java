package vn.edu.se10.bluemoon.residency;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.*;
import vn.edu.se10.bluemoon.residency.api.DeclarationController;
import vn.edu.se10.bluemoon.residency.api.ResidentController;
import vn.edu.se10.bluemoon.residency.application.ResidentService;
import java.time.Clock;
import vn.edu.se10.bluemoon.residency.application.DeclarationService;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest({DeclarationController.class, ResidentController.class}) @Import(SecurityConfig.class)
class DeclarationApiTest {
 @Autowired MockMvc mvc; @MockitoBean DeclarationService service; @MockitoBean CurrentAccountService currentAccount; @MockitoBean ResidentService residentService; @MockitoBean Clock clock;
 @BeforeEach void refresh(){when(currentAccount.refresh(anyLong())).thenAnswer(i->(AuthenticatedUser)org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal());}
 private UsernamePasswordAuthenticationToken principal(String role,boolean change){return UsernamePasswordAuthenticationToken.authenticated(new AuthenticatedUser(7,"user","User",role,1L,change),null,List.of(new SimpleGrantedAuthority("ROLE_"+role)));}
 @Test void allRoutesEnforceRolesCsrfAndPasswordChange() throws Exception {
  for(String path:List.of("/api/v1/residency-declarations/mine","/api/v1/residency-declarations","/api/v1/residents/suggest")){
   String allowed=path.endsWith("mine")?"RESIDENT":"ADMIN", denied=allowed.equals("ADMIN")?"RESIDENT":"ADMIN";
   mvc.perform(get(path)).andExpect(status().isUnauthorized());
   for(String role:List.of(denied,"ACCOUNTANT","OTHER"))mvc.perform(get(path).with(authentication(principal(role,false)))).andExpect(status().isForbidden());
   mvc.perform(get(path).with(authentication(principal(allowed,true)))).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
  }
  for(String path:List.of("/api/v1/residency-declarations","/api/v1/residency-declarations/1/cancel","/api/v1/residency-declarations/1/approve","/api/v1/residency-declarations/1/reject","/api/v1/residents/1/return")){
   String allowed=path.endsWith("declarations")||path.endsWith("cancel")?"RESIDENT":"ADMIN",denied=allowed.equals("ADMIN")?"RESIDENT":"ADMIN";
   mvc.perform(post(path).with(csrf()).contentType("application/json").content("{}" )).andExpect(status().isUnauthorized());
   for(String role:List.of(denied,"ACCOUNTANT","OTHER"))mvc.perform(post(path).with(authentication(principal(role,false))).with(csrf()).contentType("application/json").content("{}" )).andExpect(status().isForbidden());
   mvc.perform(post(path).with(authentication(principal(allowed,false))).with(csrf().useInvalidToken()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
   mvc.perform(post(path).with(authentication(principal(allowed,false))).contentType("application/json").content("{}" )).andExpect(status().isForbidden());
   mvc.perform(post(path).with(authentication(principal(allowed,true))).with(csrf()).contentType("application/json").content("{}" )).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
  }
  verifyNoInteractions(service);
 }
 @Test void suggestUsesSpecificRoute() throws Exception {when(service.suggest("An")).thenReturn(List.of());mvc.perform(get("/api/v1/residents/suggest").param("q","An").with(authentication(principal("ADMIN",false)))).andExpect(status().isOk());verify(service).suggest("An");}
}
