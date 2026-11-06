package vn.edu.se10.bluemoon.residency.application;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.*;
import vn.edu.se10.bluemoon.residency.api.*;
import vn.edu.se10.bluemoon.residency.persistence.*;
@Service
public class ResidentService {
 private static final List<ResidencyStatus> CURRENT=List.of(ResidencyStatus.RESIDING,ResidencyStatus.TEMPORARILY_ABSENT);
 private final ResidentRepository residents; private final HouseholdRepository households; private final ResidencyChangeRepository changes; private final AuditService audit; private final Clock clock;
 public ResidentService(ResidentRepository residents,HouseholdRepository households,ResidencyChangeRepository changes,AuditService audit,Clock clock){this.residents=residents;this.households=households;this.changes=changes;this.audit=audit;this.clock=clock;}
 @Transactional public ResidentResponse create(long householdId,ResidentRequest request,long actor){
  guard(); validate(request,true,clock); Household household=household(householdId); if(!household.getStatus().equals("ACTIVE"))throw new BadRequestException("HOUSEHOLD_INVALID");
  if(request.identityNumber()!=null) checkIdentity(request.identityNumber(),null);
  Resident r=residents.saveAndFlush(new Resident(householdId,request.fullName(),request.identityNumber(),request.dateOfBirth(),request.phone(),request.relationshipToHead(),clock.instant()));
  audit.record(actor,"RESIDENT_CREATED","Resident",r.getId().toString(),Map.of("id",r.getId().toString(),"householdId",r.getHouseholdId().toString(),"residencyStatus",r.getResidencyStatus().name())); return ResidentResponse.from(r);
 }
 @Transactional public ResidentResponse update(long id,ResidentRequest request,long actor){
  guard(); validate(request,false,clock); Resident r=locked(id);
  if(request.identityNumber()!=null && r.getResidencyStatus()!=ResidencyStatus.MOVED_OUT)checkIdentity(request.identityNumber(),id);
  Map<String,String> fields=new LinkedHashMap<>();
  if(request.fullName()!=null && !Objects.equals(request.fullName(),r.getFullName()))fields.put("fullName","changed");
  if(request.identityNumber()!=null && !Objects.equals(request.identityNumber(),r.getIdentityNumber()))fields.put("identityNumber","changed");
  if(request.dateOfBirth()!=null && !Objects.equals(request.dateOfBirth(),r.getDateOfBirth()))fields.put("dateOfBirth","changed");
  if(request.phone()!=null && !Objects.equals(request.phone(),r.getPhone()))fields.put("phone","changed");
  if(request.relationshipToHead()!=null && !Objects.equals(request.relationshipToHead(),r.getRelationshipToHead()))fields.put("relationshipToHead","changed");
  r.update(request.fullName(),request.identityNumber(),request.dateOfBirth(),request.phone(),request.relationshipToHead(),clock.instant());
  audit.record(actor,"RESIDENT_UPDATED","Resident",r.getId().toString(),Map.of("fields",String.join(",",fields.keySet()))); return ResidentResponse.from(r);
 }
 @Transactional public ResidentResponse moveOut(long id,MoveOutRequest request,long actor){
  guard();
  if(request.reason()!=null && request.reason().length()>255)throw new BadRequestException("VALIDATION");
  if(request.effectiveDate()==null || request.effectiveDate().isAfter(today()))throw new BadRequestException("INVALID_DATE");
  Resident r=locked(id); if(r.getResidencyStatus()==ResidencyStatus.MOVED_OUT)throw new ConflictException("RESIDENT_ALREADY_MOVED_OUT");
  Instant now=clock.instant(); r.moveOut(now); changes.save(new ResidencyChange(r,request.effectiveDate(),request.reason(),actor,now));
  audit.record(actor,"RESIDENT_MOVED_OUT","Resident",r.getId().toString(),Map.of("id",r.getId().toString(),"householdId",r.getHouseholdId().toString(),"effectiveDate",request.effectiveDate().toString())); return ResidentResponse.from(r);
 }
 @Transactional(readOnly=true) public ResidentResponse get(long id){return ResidentResponse.from(residents.findById(id).orElseThrow(()->new NotFoundException("RESIDENT_NOT_FOUND")));}
 @Transactional(readOnly=true) public Page<ResidentResponse> list(long householdId,ResidencyStatus status,int page,int size){household(householdId);return residents.findByHouseholdIdAndResidencyStatusIn(householdId,status==null?CURRENT:List.of(status),paging(page,size)).map(ResidentResponse::from);}
 @Transactional(readOnly=true) public Page<ResidencyChangeResponse> history(long householdId,int page,int size){household(householdId);return changes.findByHouseholdId(householdId,paging(page,size)).map(ResidencyChangeResponse::from);}
 @Transactional(readOnly=true) public List<MyResidentResponse> mine(Long householdId){if(householdId==null)throw new AccessDeniedException("Forbidden");return residents.findByHouseholdIdAndResidencyStatusInOrderById(householdId,CURRENT).stream().map(MyResidentResponse::from).toList();}
 private PageRequest paging(int page,int size){if(page<0 || size<1 || size>100)throw new BadRequestException("INVALID_FILTER");return PageRequest.of(page,size,Sort.by("id"));}
 private Household household(long id){return households.findById(id).orElseThrow(()->new NotFoundException("HOUSEHOLD_NOT_FOUND"));}
 private Resident locked(long id){return residents.lockById(id).orElseThrow(()->new NotFoundException("RESIDENT_NOT_FOUND"));}
 // Serializes all resident writes and inserts referencing the guard household; acceptable for v1 scale.
 private void guard(){if(households.lockIdentityGuard()==null)throw new BadRequestException("HOUSEHOLD_INVALID");}
 private void checkIdentity(String identity,Long exclude){if(!residents.findConflicts(identity,CURRENT,exclude).isEmpty())throw new ConflictException("IDENTITY_NUMBER_ALREADY_USED");}
 private LocalDate today(){return LocalDate.now(clock.withZone(ZoneId.of("Asia/Ho_Chi_Minh")));}
 public static void validate(ResidentRequest r,boolean create,Clock clock){
  if((create && (r.fullName()==null || r.fullName().isBlank())) || (r.fullName()!=null && (r.fullName().isBlank() || r.fullName().length()>150)) || (r.identityNumber()!=null && (r.identityNumber().isBlank() || r.identityNumber().length()>50)) || (r.dateOfBirth()!=null && r.dateOfBirth().isAfter(LocalDate.now(clock.withZone(ZoneId.of("Asia/Ho_Chi_Minh"))))) || (r.phone()!=null && r.phone().length()>30) || (r.relationshipToHead()!=null && r.relationshipToHead().length()>100))throw new BadRequestException("INVALID_RESIDENT");
  if(!create && r.fullName()==null && r.identityNumber()==null && r.dateOfBirth()==null && r.phone()==null && r.relationshipToHead()==null)throw new BadRequestException("EMPTY_UPDATE");
 }
}
