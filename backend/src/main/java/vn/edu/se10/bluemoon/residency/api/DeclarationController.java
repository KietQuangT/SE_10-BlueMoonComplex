package vn.edu.se10.bluemoon.residency.api;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.residency.application.DeclarationService;
@RestController @RequestMapping("/api/v1")
public class DeclarationController {
 private final DeclarationService service;
 public DeclarationController(DeclarationService service){this.service=service;}
 @PostMapping("/residency-declarations") @ResponseStatus(HttpStatus.CREATED) public DeclarationResponse create(@RequestBody DeclarationRequest request,@AuthenticationPrincipal AuthenticatedUser actor){return service.create(request,actor.householdId(),actor.userId());}
 @GetMapping("/residency-declarations/mine") public DeclarationPage mine(@AuthenticationPrincipal AuthenticatedUser actor,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return DeclarationPage.from(service.mine(actor.householdId(),actor.userId(),page,size));}
 @GetMapping("/residency-declarations") public DeclarationPage list(@RequestParam(required=false) String status,@RequestParam(required=false) String apartment,@RequestParam(required=false) String q,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return DeclarationPage.from(service.list(status,apartment,q,page,size));}
 @PostMapping("/residency-declarations/{id}/cancel") public DeclarationResponse cancel(@PathVariable long id,@AuthenticationPrincipal AuthenticatedUser actor){return service.cancel(id,actor.householdId(),actor.userId());}
 @PostMapping("/residency-declarations/{id}/approve") public DeclarationResponse approve(@PathVariable long id,@AuthenticationPrincipal AuthenticatedUser actor){return service.approve(id,actor.userId());}
 @PostMapping("/residency-declarations/{id}/reject") public DeclarationResponse reject(@PathVariable long id,@RequestBody RejectRequest request,@AuthenticationPrincipal AuthenticatedUser actor){return service.reject(id,request,actor.userId());}
 @PostMapping("/residents/{id}/return") public ResidentResponse returnResident(@PathVariable long id,@AuthenticationPrincipal AuthenticatedUser actor){return service.returnResident(id,actor.userId());}
 @GetMapping("/residents/suggest") public List<ResidentSuggestion> suggest(@RequestParam(required=false) String q){return service.suggest(q);}
}
