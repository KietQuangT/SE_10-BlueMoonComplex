package vn.edu.se10.bluemoon.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.*;

class BootstrapSecurityContextTest {
    @Test void securityBeansCanStartWithoutWebServer() {
        new ApplicationContextRunner().withUserConfiguration(SecurityConfig.class)
                .withBean(vn.edu.se10.bluemoon.identity.application.CurrentAccountService.class, () -> org.mockito.Mockito.mock(vn.edu.se10.bluemoon.identity.application.CurrentAccountService.class))
                .withPropertyValues("app.cors.allowed-origin=http://localhost:5173")
                .run(context -> assertThat(context).hasNotFailed());
    }
}
