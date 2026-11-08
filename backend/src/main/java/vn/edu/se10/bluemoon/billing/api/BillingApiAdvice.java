package vn.edu.se10.bluemoon.billing.api;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = {BillingPeriodController.class, VoluntaryController.class})
public class BillingApiAdvice {
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail invalidFilter(MethodArgumentTypeMismatchException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Tham số không hợp lệ.");
        problem.setProperty("code", "INVALID_FILTER");
        return problem;
    }
}
