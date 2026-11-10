package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.reporting.application.AgencyDataPackageService;
import vn.edu.se10.bluemoon.reporting.application.AgencyDataPackageWriter;
import vn.edu.se10.bluemoon.reporting.persistence.AgencyDataPackageStore;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AgencyDataPackageService.class})
class AgencyDataPackageWiringTest {

    @MockitoBean
    private AgencyDataPackageStore store;

    @MockitoBean
    private AgencyDataPackageWriter writer;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private Clock clock;

    @MockitoBean
    private PlatformTransactionManager transactionManager;

    @Autowired
    private AgencyDataPackageService service;

    @Test
    void springContextCreatesBeanViaAutowiredConstructor() {
        assertNotNull(service, "Spring container must wire and instantiate AgencyDataPackageService bean");
    }

    @Test
    void fourParameterConstructorRemainsUsableForUnitTests() {
        AgencyDataPackageService unitService = new AgencyDataPackageService(store, writer, auditService, clock);
        assertNotNull(unitService, "4-parameter constructor remains available for lightweight unit testing");
    }
}
