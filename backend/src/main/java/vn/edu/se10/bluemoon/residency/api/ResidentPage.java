package vn.edu.se10.bluemoon.residency.api;
import java.util.List;
import org.springframework.data.domain.Page;
public record ResidentPage(List<ResidentResponse> content,int page,int size,long totalElements,int totalPages) {
 public static ResidentPage from(Page<ResidentResponse> p){return new ResidentPage(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages());}
}
