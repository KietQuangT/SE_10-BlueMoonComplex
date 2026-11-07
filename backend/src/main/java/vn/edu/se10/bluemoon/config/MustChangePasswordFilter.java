package vn.edu.se10.bluemoon.config;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;

public class MustChangePasswordFilter extends OncePerRequestFilter {
    private static final PathPatternRequestMatcher.Builder MATCHER = PathPatternRequestMatcher.withDefaults();
    private static final RequestMatcher ALLOWED = new OrRequestMatcher(
            MATCHER.matcher(HttpMethod.GET, "/api/v1/auth/me"), MATCHER.matcher(HttpMethod.GET, "/api/v1/auth/csrf"),
            MATCHER.matcher(HttpMethod.POST, "/api/v1/auth/change-password"), MATCHER.matcher(HttpMethod.POST, "/api/v1/auth/logout"), MATCHER.matcher(HttpMethod.POST, "/api/v1/auth/login"),
            MATCHER.matcher(HttpMethod.GET, "/actuator/health/**"), MATCHER.matcher(HttpMethod.GET, "/api/v1/system/info"));
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user && user.mustChangePassword() && !ALLOWED.matches(request)) {
            response.setStatus(403); response.setContentType("application/problem+json"); response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Không có quyền truy cập\",\"status\":403,\"detail\":\"Vui lòng đổi mật khẩu.\",\"code\":\"PASSWORD_CHANGE_REQUIRED\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
