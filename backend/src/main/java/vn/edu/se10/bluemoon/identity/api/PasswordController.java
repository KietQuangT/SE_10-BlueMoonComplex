package vn.edu.se10.bluemoon.identity.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.PasswordChangeService;

@RestController
public class PasswordController {
    private final PasswordChangeService service;
    private final SecurityContextLogoutHandler logout = new SecurityContextLogoutHandler();
    public PasswordController(PasswordChangeService service) { this.service = service; }
    @PostMapping("/api/v1/auth/change-password")
    public ResponseEntity<?> change(@Valid @RequestBody ChangePasswordRequest body, Authentication authentication,
                                    HttpServletRequest request, HttpServletResponse response) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = service.change(user.userId(), body.currentPassword(), body.newPassword());
        if (result == PasswordChangeService.Result.LOCKED)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng."));
        if (result != PasswordChangeService.Result.SUCCESS) throw new BadRequestException(result.name());
        logout.logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }
}
