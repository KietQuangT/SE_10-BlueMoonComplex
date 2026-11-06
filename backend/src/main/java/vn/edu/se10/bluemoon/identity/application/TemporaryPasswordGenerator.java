package vn.edu.se10.bluemoon.identity.application;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class TemporaryPasswordGenerator {
    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String ALL = UPPER + LOWER + DIGITS;
    private final SecureRandom random = new SecureRandom();
    public String generate() {
        char[] chars = new char[16];
        chars[0] = pick(UPPER); chars[1] = pick(LOWER); chars[2] = pick(DIGITS);
        for (int i = 3; i < chars.length; i++) chars[i] = pick(ALL);
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1); char c = chars[i]; chars[i] = chars[j]; chars[j] = c;
        }
        return new String(chars);
    }
    private char pick(String alphabet) { return alphabet.charAt(random.nextInt(alphabet.length())); }
}
