package vn.edu.se10.bluemoon.config;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CurrentAccountService;

public class CurrentAccountFilter extends OncePerRequestFilter {
    private final CurrentAccountService accounts;
    public CurrentAccountFilter(CurrentAccountService accounts) { this.accounts = accounts; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        var context = SecurityContextHolder.getContext();
        var auth = context.getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof AuthenticatedUser principal) {
            var current = accounts.refresh(principal.userId());
            if (current == null) {
                SecurityContextHolder.clearContext();
                response.setStatus(401); response.setContentType("application/problem+json"); response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Chưa xác thực\",\"status\":401}");
                return;
            }
            current = new AuthenticatedUser(current.userId(), current.username(), current.fullName(), current.role(), current.householdId(), principal.mustChangePassword());
            var replacement = UsernamePasswordAuthenticationToken.authenticated(current, null, List.of(new SimpleGrantedAuthority("ROLE_" + current.role())));
            var freshContext = SecurityContextHolder.createEmptyContext();
            freshContext.setAuthentication(replacement);
            SecurityContextHolder.setContext(freshContext);
        }
        chain.doFilter(request, response);
    }
}
