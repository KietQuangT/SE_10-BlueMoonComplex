package vn.edu.se10.bluemoon.identity;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import vn.edu.se10.bluemoon.identity.application.BootstrapAdminService;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@org.junit.jupiter.api.extension.ExtendWith(org.springframework.boot.test.system.OutputCaptureExtension.class)
class BootstrapAdminIT {
    @Autowired BootstrapAdminService service;
    @Autowired JdbcTemplate jdbc;
    @Autowired TransactionTemplate transactions;
    @Autowired org.springframework.context.ApplicationContext context;
    @Test void runnerIsAbsentWithoutBootstrapProfile() {
        assertThat(context.getBeansOfType(vn.edu.se10.bluemoon.identity.bootstrap.BootstrapAdminRunner.class)).isEmpty();
    }
    @Test void bootstrapWithoutConsoleExitsWithoutCreatingUsers() throws Exception {
        int before=jdbc.queryForObject("SELECT COUNT(*) FROM users",Integer.class);
        var output=java.nio.file.Files.createTempFile("s02-bootstrap-", ".log");
        try {
            var process=new ProcessBuilder(System.getProperty("java.home")+"/bin/java", "-jar", "target/bluemoon-backend-0.1.0-SNAPSHOT.jar", "--spring.profiles.active=bootstrap")
                    .redirectErrorStream(true).redirectOutput(output.toFile()).start();
            if (!process.waitFor(60, java.util.concurrent.TimeUnit.SECONDS)) { process.destroyForcibly(); throw new AssertionError("Bootstrap timeout"); }
            assertThat(process.exitValue()).isNotZero();
            assertThat(java.nio.file.Files.readString(output)).contains("Cần terminal tương tác");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users",Integer.class)).isEqualTo(before);
        } finally { java.nio.file.Files.deleteIfExists(output); }
    }
    @Test void createsOnlyOneAdminAndAuditsWithoutActor(org.springframework.boot.test.system.CapturedOutput output) {
        transactions.executeWithoutResult(tx -> {
            try {
                jdbc.update("UPDATE users SET role_id=(SELECT id FROM roles WHERE code='ACCOUNTANT') WHERE role_id=(SELECT id FROM roles WHERE code='ADMIN')");
                String name="bootstrap-"+UUID.randomUUID();
                char[] password="test-only-pass".toCharArray();
                assertThat(service.create(name,"Test User",name+"@example.test",password,"test-only-pass".toCharArray())).isTrue();
                assertThat(output.getAll()).doesNotContain(name, name+"@example.test", "test-only-pass");
                assertThat(password).containsOnly('\0');
                assertThat(service.create(name+"x","Test User",name+"x@example.test","test-only-pass".toCharArray(),"test-only-pass".toCharArray())).isFalse();
                long id=jdbc.queryForObject("SELECT id FROM users WHERE username=?",Long.class,name);
                assertThat(jdbc.queryForObject("SELECT must_change_password FROM users WHERE id=?",Boolean.class,id)).isFalse();
                assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE action='BOOTSTRAP_ADMIN' AND actor_user_id IS NULL AND entity_id=?",Integer.class,Long.toString(id))).isEqualTo(1);
                var changes=tools.jackson.databind.json.JsonMapper.builder().build().readTree(jdbc.queryForObject("SELECT changes FROM audit_log WHERE action='BOOTSTRAP_ADMIN' AND entity_id=?",String.class,Long.toString(id)));
                assertThat(changes.size()).isEqualTo(1); assertThat(changes.get("role").asString()).isEqualTo("ADMIN");
            } finally { tx.setRollbackOnly(); }
        });
    }
    @Test void inactiveAdminStillDisablesBootstrap() {
        transactions.executeWithoutResult(tx -> {
            try {
                jdbc.update("UPDATE users SET role_id=(SELECT id FROM roles WHERE code='ACCOUNTANT') WHERE role_id=(SELECT id FROM roles WHERE code='ADMIN')");
                String username="inactive-"+UUID.randomUUID();
                jdbc.update("INSERT INTO users (role_id,username,password_hash,full_name,status,created_at,updated_at) VALUES ((SELECT id FROM roles WHERE code='ADMIN'),?,'hash','Test','INACTIVE',NOW(6),NOW(6))",username);
                assertThat(service.create(username+"new","Test",username+"@example.test","test-only-pass".toCharArray(),"test-only-pass".toCharArray())).isFalse();
            } finally { tx.setRollbackOnly(); }
        });
    }
    @Test void rejectsInvalidPasswordsWithoutCreatingAnAdmin() {
        for (String[] values : new String[][] {{"short","short"},{"valid-pass","different"},{"x".repeat(73),"x".repeat(73)},{"é".repeat(37),"é".repeat(37)}}) {
            transactions.executeWithoutResult(tx -> {
                try {
                    jdbc.update("UPDATE users SET role_id=(SELECT id FROM roles WHERE code='ACCOUNTANT') WHERE role_id=(SELECT id FROM roles WHERE code='ADMIN')");
                    String username="bad-"+UUID.randomUUID();
                    assertThatThrownBy(() -> service.create(username,"Test",username+"@example.test",values[0].toCharArray(),values[1].toCharArray())).isInstanceOf(IllegalArgumentException.class);
                    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username=?",Integer.class,username)).isZero();
                } finally { tx.setRollbackOnly(); }
            });
        }
    }
    @Test void rejectsDuplicateUsernameOrEmailWithoutCreatingAnAdmin(org.springframework.boot.test.system.CapturedOutput output) {
        for (boolean duplicateEmail : new boolean[] {true,false}) {
            transactions.executeWithoutResult(tx -> {
                try {
                    jdbc.update("UPDATE users SET role_id=(SELECT id FROM roles WHERE code='ACCOUNTANT') WHERE role_id=(SELECT id FROM roles WHERE code='ADMIN')");
                    String username="duplicate-"+UUID.randomUUID();
                    String email=username+"@example.test";
                    jdbc.update("INSERT INTO users (role_id,username,password_hash,full_name,status,email,created_at,updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'),?,'hash','Test','ACTIVE',?,NOW(6),NOW(6))",username,email);
                    String targetName=duplicateEmail ? username+"x" : username.toUpperCase(java.util.Locale.ROOT);
                    String targetEmail=duplicateEmail ? email.toUpperCase(java.util.Locale.ROOT) : "new"+email;
                    assertThatThrownBy(() -> service.create(targetName,"Test",targetEmail,"test-only-pass".toCharArray(),"test-only-pass".toCharArray())).isInstanceOf(IllegalArgumentException.class);
                    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE role_id=(SELECT id FROM roles WHERE code='ADMIN')",Integer.class)).isZero();
                    assertThat(output.getAll()).doesNotContain(username,email,"test-only-pass");
                } finally { tx.setRollbackOnly(); }
            });
        }
    }
    @Test void rejectsInvalidUsernameAndEmail() {
        for (String[] values : new String[][] {{"!bad", "valid@example.test"},{"valid-name", "not-an-email"}}) {
            transactions.executeWithoutResult(tx -> {
                try {
                    jdbc.update("UPDATE users SET role_id=(SELECT id FROM roles WHERE code='ACCOUNTANT') WHERE role_id=(SELECT id FROM roles WHERE code='ADMIN')");
                    assertThatThrownBy(() -> service.create(values[0],"Test",values[1],"test-only-pass".toCharArray(),"test-only-pass".toCharArray())).isInstanceOf(IllegalArgumentException.class);
                    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE role_id=(SELECT id FROM roles WHERE code='ADMIN')",Integer.class)).isZero();
                } finally { tx.setRollbackOnly(); }
            });
        }
    }
}
