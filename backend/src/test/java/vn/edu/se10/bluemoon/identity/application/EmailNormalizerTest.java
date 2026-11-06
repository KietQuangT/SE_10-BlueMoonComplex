package vn.edu.se10.bluemoon.identity.application;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class EmailNormalizerTest {
    @Test void normalizesAndRejectsInvalidAddresses() {
        assertThat(EmailNormalizer.normalize("  EXAMPLE+tag@Example.COM  ")).isEqualTo("example+tag@example.com");
        for (String invalid : new String[] {"foo", "a@@b.example", "a@localhost", "aé@b.example", "a\r\n@b.example", "a@b.example\n", "a".repeat(250) + "@b.example"}) {
            assertThatThrownBy(() -> EmailNormalizer.normalize(invalid)).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
