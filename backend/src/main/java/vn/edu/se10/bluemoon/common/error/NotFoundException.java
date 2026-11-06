package vn.edu.se10.bluemoon.common.error;
public class NotFoundException extends RuntimeException {
 private final String code;
 public NotFoundException(String code){this.code=code;}
 public String code(){return code;}
}
