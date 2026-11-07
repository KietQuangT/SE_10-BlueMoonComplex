package vn.edu.se10.bluemoon.residency.api;
import java.util.List;
import org.springframework.data.domain.Page;
public record DeclarationPage(List<DeclarationResponse> content,int page,int size,long totalElements,int totalPages) {
 public static DeclarationPage from(Page<DeclarationResponse> p){return new DeclarationPage(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages());}
}
