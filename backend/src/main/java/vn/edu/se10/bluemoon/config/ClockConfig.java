package vn.edu.se10.bluemoon.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {
    /** Thời điểm lưu UTC (D09); test thay bằng Clock cố định. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
