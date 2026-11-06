package vn.edu.se10.bluemoon.identity;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.edu.se10.bluemoon.config.CurrentAccountFilter;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CurrentAccountService;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrentAccountFilterTest {
    CurrentAccountService accounts=mock(CurrentAccountService.class);
    CurrentAccountFilter filter=new CurrentAccountFilter(accounts);
    MockHttpServletRequest request=new MockHttpServletRequest();
    MockHttpServletResponse response=new MockHttpServletResponse();
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }
    void authenticated() {
        var user=new AuthenticatedUser(1,"user","Name","ADMIN",1L,false);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user,null,List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }
    @Test void inactiveAccountIsRejected() throws Exception {
        authenticated(); when(accounts.refresh(1)).thenReturn(null);
        var session=request.getSession();
        var chain=new MockFilterChain(); filter.doFilter(request,response,chain);
        assertThat(((org.springframework.mock.web.MockHttpSession) session).isInvalid()).isTrue();
        assertThat(response.getStatus()).isEqualTo(401); assertThat(chain.getRequest()).isNull();
    }
    @Test void roleAndHouseholdComeFromDatabase() throws Exception {
        authenticated(); when(accounts.refresh(1)).thenReturn(new AuthenticatedUser(1,"user","Name","RESIDENT",2L,false));
        var chain=new MockFilterChain(); filter.doFilter(request,response,chain);
        var auth=SecurityContextHolder.getContext().getAuthentication();
        assertThat(((AuthenticatedUser)auth.getPrincipal()).householdId()).isEqualTo(2L);
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_RESIDENT");
        assertThat(chain.getRequest()).isNotNull();
    }
}
