package vn.edu.se10.bluemoon.common.error;
public class BadRequestException extends RuntimeException {
    private final String code;
    public BadRequestException(String code) { super("Dữ liệu gửi lên không hợp lệ."); this.code = code; }
    public String code() { return code; }
}
