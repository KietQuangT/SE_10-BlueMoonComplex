package vn.edu.se10.bluemoon.config;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, CsrfTokenRepository csrfTokenRepository, SecurityContextRepository contextRepository, vn.edu.se10.bluemoon.identity.application.CurrentAccountService currentAccount) throws Exception {
        http.cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())
            .requestCache(cache -> cache.disable())
            .addFilterBefore(new MustChangePasswordFilter(), AuthorizationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**", "/api/v1/system/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/csrf").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/change-password").authenticated()
                .requestMatchers("/api/v1/auth/me", "/api/v1/auth/logout").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/accounts", "/api/v1/accounts/*/disable", "/api/v1/accounts/*/enable", "/api/v1/accounts/*/unlock").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/residency-declarations/mine").hasRole("RESIDENT")
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/resident/invoices",
                    "/api/v1/resident/invoices/*",
                    "/api/v1/resident/receipts",
                    "/api/v1/resident/credit-allocations",
                    "/api/v1/resident/billing-summary").hasRole("RESIDENT")
                .requestMatchers(HttpMethod.POST, "/api/v1/residency-declarations", "/api/v1/residency-declarations/*/cancel").hasRole("RESIDENT")
                .requestMatchers(HttpMethod.GET, "/api/v1/residency-declarations", "/api/v1/residents/suggest").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/residency-declarations/*/approve", "/api/v1/residency-declarations/*/reject", "/api/v1/residents/*/return").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/households/me/residents").hasRole("RESIDENT")
                .requestMatchers(HttpMethod.GET, "/api/v1/households/*/residents", "/api/v1/households/*/residency-changes", "/api/v1/residents/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/households/*/residents", "/api/v1/residents/*/move-out").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/residents/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/households").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/households", "/api/v1/households/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/households/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/invoices/*/receipts", "/api/v1/invoices/*/payment-summary", "/api/v1/payment-invoices", "/api/v1/households/*/credit-allocations", "/api/v1/households/*/credit-balance").hasAnyRole("ADMIN", "ACCOUNTANT")
                .requestMatchers(HttpMethod.POST, "/api/v1/invoices/*/receipts", "/api/v1/receipts/*/void", "/api/v1/households/*/credit-allocations", "/api/v1/credit-allocations/*/reverse").hasRole("ACCOUNTANT")
                .requestMatchers(HttpMethod.GET, "/api/v1/fee-types", "/api/v1/fee-rates", "/api/v1/fee-preview").hasAnyRole("ADMIN", "ACCOUNTANT")
                .requestMatchers(HttpMethod.POST, "/api/v1/fee-rates").hasRole("ACCOUNTANT")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/fee-rates/*").hasRole("ACCOUNTANT")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/fee-rates/*").hasRole("ACCOUNTANT")
                .requestMatchers(HttpMethod.GET, "/api/v1/billing-periods", "/api/v1/billing-periods/*", "/api/v1/billing-periods/*/preview", "/api/v1/billing-periods/*/submissions", "/api/v1/billing-periods/*/invoices", "/api/v1/invoices/*").hasAnyRole("ADMIN", "ACCOUNTANT")
                .requestMatchers(HttpMethod.POST, "/api/v1/billing-periods", "/api/v1/billing-periods/*/submit", "/api/v1/billing-periods/*/withdraw").hasRole("ACCOUNTANT")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/billing-periods/*").hasRole("ACCOUNTANT")
                .requestMatchers(HttpMethod.POST, "/api/v1/billing-periods/*/return", "/api/v1/billing-periods/*/issue", "/api/v1/billing-periods/*/cancel").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/voluntary-campaigns/*/contributions").hasAnyRole("ADMIN", "ACCOUNTANT")
                .requestMatchers(HttpMethod.GET, "/api/v1/voluntary-donors").hasRole("ACCOUNTANT")
                .requestMatchers(HttpMethod.GET, "/api/v1/voluntary-campaigns", "/api/v1/voluntary-campaigns/*").hasAnyRole("ADMIN", "ACCOUNTANT", "RESIDENT")
                .requestMatchers(HttpMethod.POST, "/api/v1/voluntary-campaigns", "/api/v1/voluntary-campaigns/*/publish", "/api/v1/voluntary-campaigns/*/close").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/voluntary-campaigns/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/voluntary-campaigns/*/contributions", "/api/v1/voluntary-contributions/*/void").hasRole("ACCOUNTANT")
                .requestMatchers(HttpMethod.GET, "/api/v1/reports/fee-collection.xlsx", "/api/v1/reports/cash-receipts.xlsx").hasAnyRole("ADMIN", "ACCOUNTANT")
                .requestMatchers(HttpMethod.GET, "/api/v1/reports/residency.xlsx").hasRole("ADMIN")
                .anyRequest().denyAll())
            .securityContext(context -> context.securityContextRepository(contextRepository))
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, exception) -> problem(response, 401, "Chưa xác thực"))
                .accessDeniedHandler((request, response, exception) -> problem(response, 403, "Không có quyền truy cập")));
        http.addFilterBefore(new CurrentAccountFilter(currentAccount), MustChangePasswordFilter.class);
        return http.build();
    }

    @Bean CsrfTokenRepository csrfTokenRepository() { return new HttpSessionCsrfTokenRepository(); }
    @Bean SecurityContextRepository securityContextRepository() { return new HttpSessionSecurityContextRepository(); }
    @Bean SessionAuthenticationStrategy sessionAuthenticationStrategy(CsrfTokenRepository repository) {
        return new CompositeSessionAuthenticationStrategy(List.of(new ChangeSessionIdAuthenticationStrategy(), new CsrfAuthenticationStrategy(repository)));
    }
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception { return configuration.getAuthenticationManager(); }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origin}") String origin) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "X-CSRF-TOKEN", "Idempotency-Key"));
        config.setExposedHeaders(List.of("X-Request-Id"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    private static void problem(HttpServletResponse response, int status, String title) throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"" + title + "\",\"status\":" + status + "}");
    }
}
