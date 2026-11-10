package vn.edu.se10.bluemoon.identity.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.identity.application.AccountListService;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountListController {
    private final AccountListService accounts;

    public AccountListController(AccountListService accounts) { this.accounts = accounts; }

    @GetMapping
    public AccountListService.Result list(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException("INVALID_FILTER");
        }
        return accounts.list(page, size);
    }
}
