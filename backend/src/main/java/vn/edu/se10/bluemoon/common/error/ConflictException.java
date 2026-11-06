package vn.edu.se10.bluemoon.common.error;
public class ConflictException extends RuntimeException {
    private final String code;
    public ConflictException(String code) { super("Thông tin bị trùng."); this.code = code; }
    public String code() { return code; }
}
