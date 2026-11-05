package vn.edu.se10.bluemoon.systeminfo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.common.web.RequestIdFilter;
import vn.edu.se10.bluemoon.systeminfo.api.SystemInfoController;
import vn.edu.se10.bluemoon.systeminfo.application.SystemInfoService;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemInfoController.class)
@Import({SecurityConfig.class, SystemInfoService.class, RequestIdFilter.class})
@TestPropertySource(properties = "app.cors.allowed-origin=http://localhost:5173")
class SystemInfoApiTest {
    @Autowired MockMvc mvc;

    @Test void moduleMauChiTraThongTinCongKhai() throws Exception {
        mvc.perform(get("/api/v1/system/info"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.stage").value("skeleton"))
            .andExpect(header().exists("X-Request-Id"));
    }

    @Test void chuaXacThucKhongVaoDuocDuongDanKhac() throws Exception {
        mvc.perform(get("/api/v1/private"))
            .andExpect(status().isUnauthorized()).andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }

    @Test void skeletonVanChanNguoiDaDangNhapNeuChuaCapQuyen() throws Exception {
        mvc.perform(get("/api/v1/private").with(user("kiem-thu")))
            .andExpect(status().isForbidden());
    }

    @Test void postThieuCsrfBiChan() throws Exception {
        mvc.perform(post("/api/v1/private").with(user("kiem-thu")))
            .andExpect(status().isForbidden())
            .andExpect(request().attribute("_csrf", org.hamcrest.Matchers.notNullValue()));
    }

    @Test void corsTuOriginLaBiChan() throws Exception {
        mvc.perform(options("/api/v1/system/info").header("Origin", "https://example.invalid")
            .header("Access-Control-Request-Method", "GET")).andExpect(status().isForbidden());
    }
}
