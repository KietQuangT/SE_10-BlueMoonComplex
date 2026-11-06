package vn.edu.se10.bluemoon.identity.application;

import java.util.HashSet;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class TemporaryPasswordGeneratorTest {
    @Test void createsDistinctPasswordsWithAllRequiredCharacterClasses() {
        var generated = new HashSet<String>();
        var generator = new TemporaryPasswordGenerator();
        for (int i = 0; i < 1000; i++) {
            String value = generator.generate();
            assertThat(value).hasSize(16).matches("[A-HJ-NP-Za-km-z2-9]+").containsPattern("[A-Z]").containsPattern("[a-z]").containsPattern("[2-9]");
            assertThat(generated.add(value)).isTrue();
        }
    }
}
