package vn.edu.se10.bluemoon.billing.application;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.billing.api.*;
import vn.edu.se10.bluemoon.billing.persistence.*;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.*;

@Service
public class FeeRateService {
    private final FeeRateRepository rates;
    private final FeeTypeRepository types;
    private final AuditService audit;
    private final Clock clock;

    public FeeRateService(FeeRateRepository rates, FeeTypeRepository types, AuditService audit, Clock clock) {
        this.rates = rates;
        this.types = types;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<FeeTypeResponse> types() {
        return types.findAll(Sort.by("id")).stream().map(FeeTypeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public FeeRatePage list(Long typeId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException("INVALID_FILTER");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "effectiveFrom", "id"));
        Page<FeeRate> result = typeId == null ? rates.findAll(pageable) : rates.findByFeeTypeId(typeId, pageable);
        LocalDate month = currentMonth();
        return FeeRatePage.from(result.map(rate -> FeeRateResponse.from(rate, month)));
    }

    @Transactional
    public FeeRateResponse create(CreateFeeRateRequest request, long actor) {
        LocalDate month = currentMonth();
        if (request.effectiveFrom().getDayOfMonth() != 1 || request.effectiveFrom().isBefore(month)) {
            throw new BadRequestException("INVALID_DATE");
        }
        FeeType type = types.findById(request.feeTypeId()).orElseThrow(() -> new NotFoundException("FEE_TYPE_NOT_FOUND"));
        if (rates.existsByFeeTypeIdAndEffectiveFrom(request.feeTypeId(), request.effectiveFrom())) {
            throw new ConflictException("FEE_RATE_DUPLICATE_MONTH");
        }
        FeeRate rate = new FeeRate(type, request.unitPrice(), request.effectiveFrom(), actor, clock.instant());
        try {
            rate = rates.saveAndFlush(rate);
        } catch (DataIntegrityViolationException exception) {
            if (duplicateMonth(exception)) {
                throw new ConflictException("FEE_RATE_DUPLICATE_MONTH");
            }
            throw exception;
        }
        record(actor, "FEE_RATE_CREATED", rate, null);
        return FeeRateResponse.from(rate, month);
    }

    @Transactional
    public FeeRateResponse update(long id, BigDecimal unitPrice, long actor) {
        FeeRate rate = editableRate(id);
        BigDecimal previous = rate.getUnitPrice();
        rate.setUnitPrice(unitPrice);
        record(actor, "FEE_RATE_UPDATED", rate, previous);
        return FeeRateResponse.from(rate, currentMonth());
    }

    @Transactional
    public void delete(long id, long actor) {
        FeeRate rate = editableRate(id);
        rates.delete(rate);
        record(actor, "FEE_RATE_DELETED", rate, null);
    }

    private FeeRate editableRate(long id) {
        FeeRate rate = rates.lockById(id).orElseThrow(() -> new NotFoundException("FEE_RATE_NOT_FOUND"));
        if (!rate.getEffectiveFrom().isAfter(currentMonth())) {
            throw new ConflictException("FEE_RATE_FROZEN");
        }
        return rate;
    }

    private LocalDate currentMonth() {
        return LocalDate.now(clock.withZone(ZoneId.of("Asia/Ho_Chi_Minh"))).withDayOfMonth(1);
    }

    private void record(long actor, String action, FeeRate rate, BigDecimal previous) {
        Map<String, String> changes = new HashMap<>();
        changes.put("feeTypeCode", rate.getFeeType().getCode());
        changes.put("effectiveFrom", rate.getEffectiveFrom().toString());
        changes.put("unitPrice", rate.getUnitPrice().toPlainString());
        if (previous != null) {
            changes.put("previousUnitPrice", previous.toPlainString());
        }
        audit.record(actor, action, "FeeRate", rate.getId().toString(), changes);
    }

    private boolean duplicateMonth(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && sql.getErrorCode() == 1062
                    && sql.getMessage() != null && sql.getMessage().contains("uq_fee_rates_type_month")) {
                return true;
            }
        }
        return false;
    }
}
