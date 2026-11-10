package vn.edu.se10.bluemoon.reporting.api;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.reporting.application.AgencyDataPackageService;
import vn.edu.se10.bluemoon.reporting.application.CreatePackageRequest;
import vn.edu.se10.bluemoon.reporting.application.PackageActionRequest;
import vn.edu.se10.bluemoon.reporting.application.PackageDetail;
import vn.edu.se10.bluemoon.reporting.application.PackagePreviewResponse;
import vn.edu.se10.bluemoon.reporting.application.PackageSummaryPage;

@RestController
@RequestMapping("/api/v1/agency-data-packages")
public class AgencyDataPackageController {
    private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final Pattern SHA_PATTERN = Pattern.compile("^[0-9a-f]{64}$");

    private final AgencyDataPackageService service;
    private final ObjectMapper mapper;

    public AgencyDataPackageController(AgencyDataPackageService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PackageDetail create(@RequestBody(required = false) String rawBody,
                                @AuthenticationPrincipal AuthenticatedUser actor) {
        CreatePackageRequest request = parseCreateRequest(rawBody);
        return service.createPackage(request, actor != null ? actor.userId() : null);
    }

    @GetMapping
    public PackageSummaryPage list(HttpServletRequest request,
                                   @AuthenticationPrincipal AuthenticatedUser actor) {
        validateQueryParams(request, Set.of("status", "page", "size"));
        String status = parseStatus(request.getParameter("status"));
        int page = parsePage(request.getParameter("page"));
        int size = parseSize(request.getParameter("size"));
        return service.listPackages(status, page, size, actor != null ? actor.userId() : null);
    }

    @GetMapping("/{id}")
    public PackageDetail detail(@PathVariable long id,
                                HttpServletRequest request,
                                @AuthenticationPrincipal AuthenticatedUser actor) {
        validateQueryParams(request, Set.of());
        return service.getPackageDetail(id, actor != null ? actor.userId() : null);
    }

    @GetMapping("/{id}/preview")
    public PackagePreviewResponse preview(@PathVariable long id,
                                          HttpServletRequest request,
                                          @AuthenticationPrincipal AuthenticatedUser actor) {
        validateQueryParams(request, Set.of("page", "size"));
        int page = parsePage(request.getParameter("page"));
        int size = parseSize(request.getParameter("size"));
        return service.previewPackage(id, page, size, actor != null ? actor.userId() : null);
    }

    @PostMapping("/{id}/approve")
    public PackageDetail approve(@PathVariable long id,
                                 @RequestBody(required = false) String rawBody,
                                 @AuthenticationPrincipal AuthenticatedUser actor) {
        PackageActionRequest request = parseActionRequest(rawBody);
        return service.approvePackage(id, request, actor != null ? actor.userId() : null);
    }

    @PostMapping("/{id}/reject")
    public PackageDetail reject(@PathVariable long id,
                                @RequestBody(required = false) String rawBody,
                                @AuthenticationPrincipal AuthenticatedUser actor) {
        PackageActionRequest request = parseActionRequest(rawBody);
        return service.rejectPackage(id, request, actor != null ? actor.userId() : null);
    }

    @PostMapping("/{id}/revoke")
    public PackageDetail revoke(@PathVariable long id,
                                @RequestBody(required = false) String rawBody,
                                @AuthenticationPrincipal AuthenticatedUser actor) {
        PackageActionRequest request = parseActionRequest(rawBody);
        return service.revokePackage(id, request, actor != null ? actor.userId() : null);
    }

    @GetMapping("/{id}/content.xlsx")
    public ResponseEntity<byte[]> download(@PathVariable long id,
                                           HttpServletRequest request,
                                           @AuthenticationPrincipal AuthenticatedUser actor) {
        validateQueryParams(request, Set.of());
        byte[] bytes = service.downloadPackage(id, actor != null ? actor.userId() : null);
        String filename = "goi-cu-tru-" + id + "-v1.xlsx";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .header(HttpHeaders.CONTENT_TYPE, XLSX_CONTENT_TYPE)
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .body(bytes);
    }

    @PostMapping("/{id}/handoff")
    public PackageDetail handoff(@PathVariable long id,
                                 @RequestBody(required = false) String rawBody,
                                 @AuthenticationPrincipal AuthenticatedUser actor) {
        PackageActionRequest request = parseActionRequest(rawBody);
        return service.handoffPackage(id, request, actor != null ? actor.userId() : null);
    }

    private CreatePackageRequest parseCreateRequest(String body) {
        if (body == null || body.isBlank()) {
            throw new BadRequestException("INVALID_PACKAGE_REQUEST");
        }
        JsonNode node;
        try {
            node = mapper.readTree(body);
        } catch (Exception e) {
            throw new BadRequestException("INVALID_PACKAGE_REQUEST");
        }
        if (!node.isObject()) {
            throw new BadRequestException("INVALID_PACKAGE_REQUEST");
        }
        Set<String> allowed = Set.of("householdIds", "recipientAgencyCode", "requestReference", "licenceReference");
        for (String field : node.propertyNames()) {
            if (!allowed.contains(field)) {
                throw new BadRequestException("INVALID_PACKAGE_REQUEST");
            }
        }
        if (!node.has("householdIds") || !node.has("recipientAgencyCode") || !node.has("requestReference") || !node.has("licenceReference")) {
            throw new BadRequestException("INVALID_PACKAGE_REQUEST");
        }
        JsonNode hhNode = node.get("householdIds");
        if (hhNode == null || !hhNode.isArray()) {
            throw new BadRequestException("PACKAGE_SCOPE_INVALID");
        }
        List<Long> householdIds = new ArrayList<>();
        for (JsonNode elem : hhNode) {
            if (!elem.isIntegralNumber()) {
                throw new BadRequestException("PACKAGE_SCOPE_INVALID");
            }
            householdIds.add(elem.asLong());
        }
        JsonNode agencyNode = node.get("recipientAgencyCode");
        JsonNode reqRefNode = node.get("requestReference");
        JsonNode licRefNode = node.get("licenceReference");
        if (!agencyNode.isTextual() || !reqRefNode.isTextual() || !licRefNode.isTextual()) {
            throw new BadRequestException("INVALID_PACKAGE_REQUEST");
        }
        return new CreatePackageRequest(householdIds, agencyNode.asText(), reqRefNode.asText(), licRefNode.asText());
    }

    private PackageActionRequest parseActionRequest(String body) {
        if (body == null || body.isBlank()) {
            throw new BadRequestException("INVALID_PACKAGE_REQUEST");
        }
        JsonNode node;
        try {
            node = mapper.readTree(body);
        } catch (Exception e) {
            throw new BadRequestException("INVALID_PACKAGE_REQUEST");
        }
        if (!node.isObject()) {
            throw new BadRequestException("INVALID_PACKAGE_REQUEST");
        }
        Set<String> allowed = Set.of("expectedVersion", "contentSha256");
        for (String field : node.propertyNames()) {
            if (!allowed.contains(field)) {
                throw new BadRequestException("INVALID_PACKAGE_REQUEST");
            }
        }
        if (!node.has("expectedVersion") || !node.has("contentSha256")) {
            throw new BadRequestException("INVALID_PACKAGE_REQUEST");
        }
        JsonNode versionNode = node.get("expectedVersion");
        JsonNode shaNode = node.get("contentSha256");
        if (!versionNode.isIntegralNumber() || !shaNode.isTextual()) {
            throw new BadRequestException("INVALID_PACKAGE_REQUEST");
        }
        long expectedVersion = versionNode.asLong();
        String contentSha256 = shaNode.asText();
        if (expectedVersion <= 0 || !SHA_PATTERN.matcher(contentSha256).matches()) {
            throw new BadRequestException("INVALID_PACKAGE_REQUEST");
        }
        return new PackageActionRequest(expectedVersion, contentSha256);
    }

    private void validateQueryParams(HttpServletRequest request, Set<String> allowedKeys) {
        Map<String, String[]> params = request.getParameterMap();
        for (String key : params.keySet()) {
            if (!allowedKeys.contains(key)) {
                throw new BadRequestException("INVALID_PACKAGE_FILTER");
            }
            String[] vals = params.get(key);
            if (vals == null || vals.length != 1) {
                throw new BadRequestException("INVALID_PACKAGE_FILTER");
            }
        }
    }

    private String parseStatus(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            return null;
        }
        if (!Set.of("DRAFT", "APPROVED", "REJECTED", "REVOKED").contains(statusStr)) {
            throw new BadRequestException("INVALID_PACKAGE_FILTER");
        }
        return statusStr;
    }

    private int parsePage(String pageStr) {
        if (pageStr == null || pageStr.isBlank()) {
            return 0;
        }
        try {
            int page = Integer.parseInt(pageStr);
            if (page < 0) {
                throw new BadRequestException("INVALID_PACKAGE_FILTER");
            }
            return page;
        } catch (NumberFormatException e) {
            throw new BadRequestException("INVALID_PACKAGE_FILTER");
        }
    }

    private int parseSize(String sizeStr) {
        if (sizeStr == null || sizeStr.isBlank()) {
            return 20;
        }
        try {
            int size = Integer.parseInt(sizeStr);
            if (size < 1 || size > 100) {
                throw new BadRequestException("INVALID_PACKAGE_FILTER");
            }
            return size;
        } catch (NumberFormatException e) {
            throw new BadRequestException("INVALID_PACKAGE_FILTER");
        }
    }
}
