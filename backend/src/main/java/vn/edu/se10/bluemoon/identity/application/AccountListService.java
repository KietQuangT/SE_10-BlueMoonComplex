package vn.edu.se10.bluemoon.identity.application;

import java.time.Clock;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.identity.persistence.UserRepository;

@Service
public class AccountListService {
    private final UserRepository users;
    private final Clock clock;

    public AccountListService(UserRepository users, Clock clock) {
        this.users = users;
        this.clock = clock;
    }

    public record Item(long id, String username, String fullName, String email, String role,
                       Long householdId, String status, boolean locked) {}
    public record Result(List<Item> content, int page, int totalPages, long totalElements) {}

    @Transactional(readOnly = true)
    public Result list(int page, int size) {
        var now = clock.instant();
        var result = users.findAll(PageRequest.of(page, size, Sort.by("id")));
        var items = result.getContent().stream().map(u -> new Item(
            u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getRole().getCode(),
            u.getHouseholdId(), u.getStatus(), u.getLockedUntil() != null && u.getLockedUntil().isAfter(now)
        )).toList();
        return new Result(items, page, result.getTotalPages(), result.getTotalElements());
    }
}
