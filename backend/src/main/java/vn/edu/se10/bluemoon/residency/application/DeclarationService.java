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
public class DeclarationService {
 private final ResidentRepository residents; private final HouseholdRepository households; private final ResidencyChangeRepository changes; private final AuditService audit; private final Clock clock;
 public DeclarationService(ResidentRepository residents,HouseholdRepository households,ResidencyChangeRepository changes,AuditService audit,Clock clock){this.residents=residents;this.households=households;this.changes=changes;this.audit=audit;this.clock=clock;}
 @Transactional public DeclarationResponse create(DeclarationRequest request,Long household,long actor){
  requireHousehold(household); if(request.residentId()==null)throw new BadRequestException("VALIDATION");
  if(request.effectiveDate()==null || request.effectiveDate().isBefore(today()) || (request.endDate()!=null && request.endDate().isBefore(request.effectiveDate())))throw new BadRequestException("INVALID_DATE");
  validateText(request.reason()); guard(); Resident r=lockedResident(request.residentId());
  if(!household.equals(r.getHouseholdId()))throw new NotFoundException("RESIDENT_NOT_FOUND");
  if(r.getResidencyStatus()!=ResidencyStatus.RESIDING)throw new ConflictException("RESIDENT_NOT_AVAILABLE");
  // The one-pending invariant relies on guard() being locked before this read.
  if(changes.existsByResidentIdAndChangeTypeAndStatus(r.getId(),"TEMPORARY_ABSENCE","PENDING"))throw new ConflictException("DECLARATION_ALREADY_PENDING");
  ResidencyChange c=changes.saveAndFlush(ResidencyChange.absence(r,request.effectiveDate(),request.endDate(),request.reason(),actor,clock.instant())); record(actor,"ABSENCE_REQUESTED",c); return response(c,null);
 }
 @Transactional public DeclarationResponse cancel(long id,Long household,long actor){requireHousehold(household);guard();ResidencyChange c=lockedChange(id);if(!household.equals(c.getHouseholdId()))throw new NotFoundException("DECLARATION_NOT_FOUND");if(!c.getRecordedBy().equals(actor))throw new AccessDeniedException("Forbidden");pending(c);c.decide("CANCELLED",null,null,clock.instant());record(actor,"ABSENCE_CANCELLED",c);return response(c,null);}
 @Transactional public DeclarationResponse approve(long id,long actor){guard();ResidencyChange c=lockedChange(id);pending(c);Resident r=lockedResident(c.getResidentId());if(r.getResidencyStatus()!=ResidencyStatus.RESIDING)throw new ConflictException("RESIDENT_NOT_AVAILABLE");Instant now=clock.instant();r.setResidencyStatus(ResidencyStatus.TEMPORARILY_ABSENT,now);c.decide("APPROVED",actor,null,now);record(actor,"ABSENCE_APPROVED",c);return response(c,null);}
 @Transactional public DeclarationResponse reject(long id,RejectRequest request,long actor){validateText(request.decisionNote());guard();ResidencyChange c=lockedChange(id);pending(c);c.decide("REJECTED",actor,request.decisionNote(),clock.instant());record(actor,"ABSENCE_REJECTED",c);return response(c,null);}
 @Transactional public ResidentResponse returnResident(long id,long actor){guard();Resident r=lockedResident(id);if(r.getResidencyStatus()!=ResidencyStatus.TEMPORARILY_ABSENT)throw new ConflictException("RESIDENT_NOT_ABSENT");Instant now=clock.instant();r.setResidencyStatus(ResidencyStatus.RESIDING,now);ResidencyChange c=changes.saveAndFlush(ResidencyChange.returned(r,today(),actor,now));record(actor,"RESIDENT_RETURNED",c);return ResidentResponse.from(r);}
 @Transactional(readOnly=true) public Page<DeclarationResponse> mine(Long household,long actor,int page,int size){requireHousehold(household);return changes.search(household,null,null,null,paging(page,size)).map(c->response(c,actor));}
 @Transactional(readOnly=true) public Page<DeclarationResponse> list(String status,String apartment,String q,int page,int size){if(status!=null && !Set.of("PENDING","APPROVED","REJECTED","CANCELLED").contains(status))throw new BadRequestException("INVALID_FILTER");return changes.search(null,status,apartment==null?null:pattern(apartment.toLowerCase(Locale.ROOT)),q==null?null:query(q),paging(page,size)).map(c->response(c,null));}
 @Transactional(readOnly=true) public List<ResidentSuggestion> suggest(String q){return residents.suggest(List.of(ResidencyStatus.RESIDING,ResidencyStatus.TEMPORARILY_ABSENT),query(q),PageRequest.of(0,10)).stream().map(r->{Household h=households.findById(r.getHouseholdId()).orElseThrow();return new ResidentSuggestion(r.getId(),r.getFullName(),h.getId(),h.getHouseholdCode(),h.getApartmentCode());}).toList();}
 private DeclarationResponse response(ResidencyChange c,Long actor){Resident r=residents.findById(c.getResidentId()).orElseThrow();Household h=households.findById(c.getHouseholdId()).orElseThrow();return new DeclarationResponse(c.getId(),c.getResidentId(),r.getFullName(),c.getHouseholdId(),h.getHouseholdCode(),h.getApartmentCode(),c.getChangeType(),c.getStatus(),c.getEffectiveDate(),c.getEndDate(),c.getReason(),c.getDecisionNote(),c.getDecidedAt(),c.getCreatedAt(),actor!=null && actor.equals(c.getRecordedBy()) && c.getStatus().equals("PENDING"));}
 private void record(long actor,String action,ResidencyChange c){audit.record(actor,action,"ResidencyChange",c.getId().toString(),Map.of("id",c.getId().toString(),"residentId",c.getResidentId().toString(),"householdId",c.getHouseholdId().toString(),"effectiveDate",c.getEffectiveDate().toString()));}
 private void guard(){if(households.lockIdentityGuard()==null)throw new BadRequestException("HOUSEHOLD_INVALID");}
 private Resident lockedResident(long id){return residents.lockById(id).orElseThrow(()->new NotFoundException("RESIDENT_NOT_FOUND"));}
 private ResidencyChange lockedChange(long id){ResidencyChange c=changes.lockById(id).orElseThrow(()->new NotFoundException("DECLARATION_NOT_FOUND"));if(!c.getChangeType().equals("TEMPORARY_ABSENCE"))throw new NotFoundException("DECLARATION_NOT_FOUND");return c;}
 private void pending(ResidencyChange c){if(!c.getStatus().equals("PENDING"))throw new ConflictException("DECLARATION_NOT_PENDING");}
 private void requireHousehold(Long id){if(id==null)throw new AccessDeniedException("Forbidden");}
 private void validateText(String text){if(text==null || text.isBlank() || text.length()>255)throw new BadRequestException("VALIDATION");}
 private LocalDate today(){return LocalDate.now(clock.withZone(ZoneId.of("Asia/Ho_Chi_Minh")));}
 private String query(String q){if(q==null || q.trim().length()<2)throw new BadRequestException("INVALID_FILTER");return pattern(q.trim());}
 private String pattern(String value){return "%"+value.replace("!","!!").replace("%","!%").replace("_","!_")+"%";}
 private PageRequest paging(int page,int size){if(page<0 || size<1 || size>100)throw new BadRequestException("INVALID_FILTER");return PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt","id"));}
}
