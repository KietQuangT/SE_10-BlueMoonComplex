package vn.edu.se10.bluemoon.identity.bootstrap;

import java.io.Console;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import vn.edu.se10.bluemoon.identity.application.BootstrapAdminService;

@Component
@Profile("bootstrap")
public class BootstrapAdminRunner implements CommandLineRunner {
    private final BootstrapAdminService service;
    public BootstrapAdminRunner(BootstrapAdminService service) { this.service=service; }
    @Override public void run(String... args) {
        Console console = System.console();
        if (console == null) throw new IllegalStateException("Cần terminal tương tác.");
        String username = console.readLine("Tên đăng nhập: ");
        String name = console.readLine("Họ tên: ");
        String email = console.readLine("Email: ");
        char[] password = console.readPassword("Mật khẩu: ");
        char[] confirmation = console.readPassword("Nhập lại mật khẩu: ");
        boolean created = service.create(username, name, email, password, confirmation);
        console.printf(created ? "Đã tạo ADMIN.%n" : "Đã có ADMIN, bỏ qua.%n");
    }
}
