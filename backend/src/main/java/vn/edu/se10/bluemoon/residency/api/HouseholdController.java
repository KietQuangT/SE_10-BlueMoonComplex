package vn.edu.se10.bluemoon.residency.api;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.residency.application.HouseholdService;
@RestController @RequestMapping("/api/v1/households")
public class HouseholdController {
 private final HouseholdService service;
 public HouseholdController(HouseholdService service){this.service=service;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public HouseholdResponse create(@Valid @RequestBody CreateHouseholdRequest request,@AuthenticationPrincipal AuthenticatedUser actor){return service.create(request,actor.userId());}
 @GetMapping public HouseholdPage list(@RequestParam(required=false) String apartmentCode,@RequestParam(required=false) String householdCode,@RequestParam(required=false) String status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return HouseholdPage.from(service.list(apartmentCode,householdCode,status,page,size));}
 @GetMapping("/{id}") public HouseholdResponse get(@PathVariable long id){return service.get(id);}
 @PatchMapping("/{id}") public HouseholdResponse update(@PathVariable long id,@Valid @RequestBody UpdateHouseholdRequest request,@AuthenticationPrincipal AuthenticatedUser actor){return service.update(id,request,actor.userId());}
}
