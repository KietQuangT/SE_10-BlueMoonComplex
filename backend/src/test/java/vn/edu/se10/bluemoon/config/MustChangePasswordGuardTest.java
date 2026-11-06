package vn.edu.se10.bluemoon.config;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import static org.assertj.core.api.Assertions.*;

class MustChangePasswordGuardTest {
    private final MustChangePasswordFilter filter = new MustChangePasswordFilter();
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    private MockHttpServletResponse request(String method, String path, boolean flagged, boolean authenticated) throws Exception {
        if (authenticated) SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                new AuthenticatedUser(1,"user","User","ADMIN",null,flagged),null,List.of()));
        else SecurityContextHolder.clearContext();
        var request = new MockHttpServletRequest(method,path); request.setServletPath(path);
        var response = new MockHttpServletResponse(); var chain = new MockFilterChain();
        filter.doFilter(request,response,chain);
        if (response.getStatus() != 403) assertThat(chain.getRequest()).isNotNull();
        return response;
    }
    @Test void allowsOnlyTheSpecifiedMethodAndPaths() throws Exception {
        for (String path : List.of("/api/v1/auth/me", "/api/v1/auth/csrf", "/actuator/health", "/actuator/health/readiness", "/api/v1/system/info"))
            assertThat(request("GET",path,true,true).getStatus()).isEqualTo(200);
        for (String path : List.of("/api/v1/auth/login", "/api/v1/auth/logout"))
            assertThat(request("POST",path,true,true).getStatus()).isEqualTo(200);
        for (String path : List.of("/api/v1/accounts", "/missing", "/api/v1/auth/me")) {
            var response=request("POST",path,true,true);
            assertThat(response.getStatus()).isEqualTo(403);
            assertThat(response.getContentAsString()).contains("PASSWORD_CHANGE_REQUIRED");
        }
    }
    @Test void unflaggedAndAnonymousRequestsPassThrough() throws Exception {
        assertThat(request("GET","/missing",false,true).getStatus()).isEqualTo(200);
        assertThat(request("GET","/missing",false,false).getStatus()).isEqualTo(200);
    }
}
