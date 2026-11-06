package vn.edu.se10.bluemoon.residency.api;
import java.util.List;
import java.time.*;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.residency.application.ResidentService;
import vn.edu.se10.bluemoon.residency.persistence.ResidencyStatus;
@RestController @RequestMapping("/api/v1")
public class ResidentController {
 private final ResidentService service; private final Clock clock;
 public ResidentController(ResidentService service,Clock clock){this.service=service;this.clock=clock;}
 @PostMapping("/households/{id}/residents") @ResponseStatus(HttpStatus.CREATED) public ResidentResponse create(@PathVariable long id,@Valid @RequestBody ResidentRequest request,@AuthenticationPrincipal AuthenticatedUser actor){ResidentService.validate(request,true,clock);return service.create(id,request,actor.userId());}
 @GetMapping("/households/{id}/residents") public ResidentPage list(@PathVariable long id,@RequestParam(required=false) ResidencyStatus status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return ResidentPage.from(service.list(id,status,page,size));}
 @GetMapping("/households/{id}/residency-changes") public ResidencyChangePage history(@PathVariable long id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return ResidencyChangePage.from(service.history(id,page,size));}
 @GetMapping("/households/me/residents") public List<MyResidentResponse> mine(@AuthenticationPrincipal AuthenticatedUser actor){return service.mine(actor.householdId());}
 @GetMapping("/residents/{id}") public ResidentResponse get(@PathVariable long id){return service.get(id);}
 @PatchMapping("/residents/{id}") public ResidentResponse update(@PathVariable long id,@Valid @RequestBody ResidentRequest request,@AuthenticationPrincipal AuthenticatedUser actor){ResidentService.validate(request,false,clock);return service.update(id,request,actor.userId());}
 @PostMapping("/residents/{id}/move-out") public ResidentResponse moveOut(@PathVariable long id,@Valid @RequestBody MoveOutRequest request,@AuthenticationPrincipal AuthenticatedUser actor){if(request.effectiveDate().isAfter(LocalDate.now(clock.withZone(ZoneId.of("Asia/Ho_Chi_Minh")))))throw new BadRequestException("INVALID_DATE");return service.moveOut(id,request,actor.userId());}
}
