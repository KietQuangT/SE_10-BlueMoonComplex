package vn.edu.se10.bluemoon.identity.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.LoginAttemptService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final LoginAttemptService attempts;
    private final SessionAuthenticationStrategy sessionStrategy;
    private final SecurityContextRepository contextRepository;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    public AuthController(LoginAttemptService attempts, SessionAuthenticationStrategy sessionStrategy,
                          SecurityContextRepository contextRepository) {
        this.attempts = attempts; this.sessionStrategy = sessionStrategy; this.contextRepository = contextRepository;
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) { return new CsrfResponse(token.getHeaderName(), token.getToken()); }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest,
                                   HttpServletResponse servletResponse) {
        var result = attempts.login(request.username(), request.password());
        if (!result.authenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(org.springframework.http.ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng."));
        }
        AuthenticatedUser user = result.user();
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(user,
                null, java.util.List.of(new SimpleGrantedAuthority("ROLE_" + user.role())));
        sessionStrategy.onAuthentication(authentication, servletRequest, servletResponse);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, servletRequest, servletResponse);
        return ResponseEntity.ok(SessionUserResponse.from(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        logoutHandler.logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public SessionUserResponse me(Authentication authentication) {
        return SessionUserResponse.from((AuthenticatedUser) authentication.getPrincipal());
    }
}
