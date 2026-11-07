package vn.edu.se10.bluemoon.identity;
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
import vn.edu.se10.bluemoon.identity.api.*;
import vn.edu.se10.bluemoon.identity.application.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(PasswordController.class) @Import(SecurityConfig.class)
class PasswordChangeApiTest {
 @Autowired MockMvc mvc; @MockitoBean PasswordChangeService service; @MockitoBean CurrentAccountService currentAccount;
 static final String PATH="/api/v1/auth/change-password", BODY="{\"currentPassword\":\"OldPass1!\",\"newPassword\":\"NewPass2!\"}";
 @BeforeEach void setup(){when(currentAccount.refresh(anyLong())).thenAnswer(i -> (AuthenticatedUser) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal());}
 UsernamePasswordAuthenticationToken principal(String role){var p=new AuthenticatedUser(1,"user","User",role,null,true); return UsernamePasswordAuthenticationToken.authenticated(p,null,List.of(new SimpleGrantedAuthority("ROLE_"+role)));}
 @Test void authenticationAndCsrfRequired() throws Exception {mvc.perform(post(PATH).with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isUnauthorized()); mvc.perform(post(PATH).contentType("application/json").content(BODY)).andExpect(status().isForbidden()); mvc.perform(post(PATH).with(authentication(principal("ADMIN"))).with(csrf().useInvalidToken()).contentType("application/json").content(BODY)).andExpect(status().isForbidden()); mvc.perform(post(PATH).with(authentication(principal("ADMIN"))).contentType("application/json").content(BODY)).andExpect(status().isForbidden()); verifyNoInteractions(service);}
 @Test void allRolesChangeOwnPasswordIgnoringExtraFields() throws Exception {when(service.change(anyLong(),anyString(),anyString())).thenReturn(PasswordChangeService.Result.SUCCESS); for(String role:List.of("ADMIN","ACCOUNTANT","RESIDENT")) mvc.perform(post(PATH).with(authentication(principal(role))).with(csrf()).contentType("application/json").content(BODY.substring(0,BODY.length()-1)+",\"userId\":99,\"username\":\"other\"}")).andExpect(status().isNoContent()).andExpect(content().string("")); verify(service,times(3)).change(1,"OldPass1!","NewPass2!");}
 @Test void errorsNeverEchoSecrets() throws Exception {for(var result:PasswordChangeService.Result.values()){if(result==PasswordChangeService.Result.SUCCESS||result==PasswordChangeService.Result.LOCKED) continue; when(service.change(anyLong(),anyString(),anyString())).thenReturn(result); mvc.perform(post(PATH).with(authentication(principal("ADMIN"))).with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(result.name())).andExpect(content().string(not(anyOf(containsString("OldPass1!"),containsString("NewPass2!"),containsString("passwordHash"),containsString("password_hash"),containsString("$2")))));} when(service.change(anyLong(),anyString(),anyString())).thenReturn(PasswordChangeService.Result.LOCKED); mvc.perform(post(PATH).with(authentication(principal("ADMIN"))).with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.detail").value("Tên đăng nhập hoặc mật khẩu không đúng."));}
 @Test void unsupportedMethodsMatchLogoutSecurity() throws Exception {for(var request:List.of(get(PATH),put(PATH).with(csrf()))) mvc.perform(request.with(authentication(principal("ADMIN")))).andExpect(status().isForbidden());}
 @Test void invalidBodyAndToStringDoNotExposePasswords() throws Exception {for(String body:List.of("{}","{","{\"currentPassword\":\"  \",\"newPassword\":\"NewPass2!\"}","{\"currentPassword\":\"OldPass1!\",\"newPassword\":\""+"x".repeat(1025)+"\"}")) mvc.perform(post(PATH).with(authentication(principal("ADMIN"))).with(csrf()).contentType("application/json").content(body)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").doesNotExist()).andExpect(content().string(not(anyOf(containsString("OldPass1!"),containsString("NewPass2!"),containsString("x".repeat(1025)))))); assertThat(new ChangePasswordRequest("secret1","secret2").toString()).doesNotContain("secret1","secret2"); verifyNoInteractions(service);}
}
