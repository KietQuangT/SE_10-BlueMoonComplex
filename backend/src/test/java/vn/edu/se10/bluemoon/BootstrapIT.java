package vn.edu.se10.bluemoon;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Chạy bằng -Pintegration với MySQL thật; không tự bỏ qua khi thiếu DB.
@SpringBootTest
@AutoConfigureMockMvc
class BootstrapIT {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired SessionRepository<? extends Session> sessions;

    @Test void khoiDongVaHealthVoiMysqlThat() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP")).andExpect(jsonPath("$.components").doesNotExist());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1", Integer.class)).isPositive();
    }

    @Test void flywayV1DenV4VaBangDuocTao() {
        List<String> versions = jdbc.queryForList(
            "SELECT version FROM flyway_schema_history WHERE success = 1 AND version IS NOT NULL", String.class);
        assertThat(versions).contains("1", "2", "3", "4", "5", "6");
        List<String> tables = jdbc.queryForList(
            "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()", String.class);
        // So không phân biệt hoa thường: MySQL với lower_case_table_names=1 (Windows) lưu tên bảng chữ thường.
        assertThat(tables).map(String::toLowerCase).contains("roles", "users", "audit_log", "households", "residents",
            "residency_changes", "mock_emails", "spring_session", "spring_session_attributes");
    }

    @Test void sessionLuuVaXoaDuocTrenMysql() {
        kiemTraSession(sessions);
    }

    private <S extends Session> void kiemTraSession(SessionRepository<S> repository) {
        S session = repository.createSession();
        session.setAttribute("kiem-thu", "du-lieu-gia");
        repository.save(session);
        Session restored = repository.findById(session.getId());
        assertThat(restored).isNotNull();
        assertThat((String) restored.getAttribute("kiem-thu")).isEqualTo("du-lieu-gia");
        repository.deleteById(session.getId());
        assertThat(repository.findById(session.getId())).isNull();
    }
}
