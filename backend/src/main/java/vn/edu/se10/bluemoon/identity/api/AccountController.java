package vn.edu.se10.bluemoon.identity.api;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.se10.bluemoon.identity.application.AccountProvisioningService;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CreateAccountCommand;
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountProvisioningService service;
    private final vn.edu.se10.bluemoon.identity.application.AccountManagementService management;
    public AccountController(AccountProvisioningService service, vn.edu.se10.bluemoon.identity.application.AccountManagementService management) { this.service=service; this.management=management; }
    @PostMapping("/{id}/disable") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable long id, @AuthenticationPrincipal AuthenticatedUser actor) { management.disable(id, actor.userId()); }
    @PostMapping("/{id}/enable") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable long id, @AuthenticationPrincipal AuthenticatedUser actor) { management.enable(id, actor.userId()); }
    @PostMapping("/{id}/unlock") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlock(@PathVariable long id, @AuthenticationPrincipal AuthenticatedUser actor) { management.unlock(id, actor.userId()); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        var account = service.create(new CreateAccountCommand(request.username(), request.fullName(), request.email(), request.role(), request.householdId()), actor.userId());
        return new AccountResponse(account.id(), account.username(), account.fullName(), account.email(), account.role(), account.householdId(), "SIMULATED");
    }
}
