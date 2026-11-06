package vn.edu.se10.bluemoon.residency.api;
import java.util.List;
import org.springframework.data.domain.Page;
public record ResidencyChangePage(List<ResidencyChangeResponse> content,int page,int size,long totalElements,int totalPages) {
 public static ResidencyChangePage from(Page<ResidencyChangeResponse> p){return new ResidencyChangePage(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages());}
}
