package vn.edu.se10.bluemoon.identity;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.api.AccountListController;
import vn.edu.se10.bluemoon.identity.application.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountListController.class) @Import(SecurityConfig.class)
class AccountListApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean AccountListService accounts;
    @MockitoBean CurrentAccountService current;
    @BeforeEach void refresh() {
        when(current.refresh(anyLong())).thenAnswer(i -> (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
    private UsernamePasswordAuthenticationToken principal(String role) {
        var user = new AuthenticatedUser(1, "test", "Test", role, null, false);
        return UsernamePasswordAuthenticationToken.authenticated(user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }
    @Test void listRequiresAdmin() throws Exception {
        mvc.perform(get("/api/v1/accounts")).andExpect(status().isUnauthorized());
        for (var role : List.of("ACCOUNTANT", "RESIDENT", "AUTHORITY"))
            mvc.perform(get("/api/v1/accounts").with(authentication(principal(role)))).andExpect(status().isForbidden());
        verifyNoInteractions(accounts);
    }
    @Test void adminReceivesOnlyListFieldsAndPaginationIsBounded() throws Exception {
        when(accounts.list(0, 100)).thenReturn(new AccountListService.Result(List.of(
            new AccountListService.Item(2, "user", "Test", null, "RESIDENT", 3L, "ACTIVE", true)), 0, 1, 1));
        mvc.perform(get("/api/v1/accounts").with(authentication(principal("ADMIN"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].username").value("user"))
            .andExpect(jsonPath("$.content[0].locked").value(true))
            .andExpect(jsonPath("$.content[0].passwordHash").doesNotExist());
        mvc.perform(get("/api/v1/accounts?size=101").with(authentication(principal("ADMIN")))).andExpect(status().isBadRequest());
    }
}
