package vn.edu.se10.bluemoon.identity;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.api.AccountController;
import vn.edu.se10.bluemoon.identity.application.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
class AccountRefreshApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean AccountProvisioningService provisioning;
    @MockitoBean AccountManagementService management;
    @MockitoBean CurrentAccountService current;
    @Test void databaseRoleOverridesSessionRoleBeforeAuthorization() throws Exception {
        var old=new AuthenticatedUser(1,"user","Name","ADMIN",null,false);
        var auth=UsernamePasswordAuthenticationToken.authenticated(old,null,List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(current.refresh(1)).thenReturn(new AuthenticatedUser(1,"user","Name","ACCOUNTANT",null,false));
        mvc.perform(post("/api/v1/accounts/2/disable").with(authentication(auth)).with(csrf())).andExpect(status().isForbidden());
        when(current.refresh(1)).thenReturn(null);
        mvc.perform(post("/api/v1/accounts/2/disable").with(authentication(auth)).with(csrf())).andExpect(status().isUnauthorized());
        verifyNoInteractions(management);
    }
}
