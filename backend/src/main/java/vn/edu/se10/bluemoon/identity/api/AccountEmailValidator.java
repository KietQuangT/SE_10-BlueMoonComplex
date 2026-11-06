package vn.edu.se10.bluemoon.identity.api;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import vn.edu.se10.bluemoon.identity.application.EmailNormalizer;
public class AccountEmailValidator implements ConstraintValidator<ValidAccountEmail, String> {
    @Override public boolean isValid(String value, ConstraintValidatorContext context) {
        try { EmailNormalizer.normalize(value); return true; }
        catch (IllegalArgumentException ex) { return false; }
    }
}
