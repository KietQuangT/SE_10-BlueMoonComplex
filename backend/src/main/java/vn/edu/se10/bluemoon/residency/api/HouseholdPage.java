package vn.edu.se10.bluemoon.residency.api;

import java.util.List;
import org.springframework.data.domain.Page;

public record HouseholdPage(List<HouseholdResponse> content, int page, int size, long totalElements, int totalPages) {
    static HouseholdPage from(Page<HouseholdResponse> result) {
        return new HouseholdPage(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
