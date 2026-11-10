package vn.edu.se10.bluemoon.common.error;

public class ForbiddenException extends RuntimeException {
    private final String code;

    public ForbiddenException(String code) {
        super("Không có quyền truy cập.");
        this.code = code;
    }

    public String code() {
        return code;
    }
}
