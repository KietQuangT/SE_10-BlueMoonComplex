package vn.edu.se10.bluemoon.billing.api;

import java.util.List;
import org.springframework.data.domain.Page;

public record FeeRatePage(List<FeeRateResponse> content, int page, int size, long totalElements, int totalPages) {
    public static FeeRatePage from(Page<FeeRateResponse> page) {
        return new FeeRatePage(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
