package vn.edu.se10.bluemoon.residency.application;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.*;
import vn.edu.se10.bluemoon.residency.api.*;
import vn.edu.se10.bluemoon.residency.persistence.*;
@Service
public class HouseholdService {
 private final HouseholdRepository repository; private final AuditService audit; private final Clock clock;
 public HouseholdService(HouseholdRepository repository, AuditService audit, Clock clock){this.repository=repository;this.audit=audit;this.clock=clock;}
 @Transactional public HouseholdResponse create(CreateHouseholdRequest request,long actor){
  Household household=new Household(request.householdCode(),request.apartmentCode(),request.areaM2(),request.address(),clock.instant());
  try { household=repository.saveAndFlush(household); } catch(DataIntegrityViolationException ex){throw new ConflictException("HOUSEHOLD_CONFLICT");}
  audit.record(actor,"HOUSEHOLD_CREATED","Household",household.getId().toString(),Map.of("id",household.getId().toString(),"status",household.getStatus(),"areaM2",household.getAreaM2().toPlainString()));
  return HouseholdResponse.from(household);
 }
 @Transactional(readOnly=true) public Page<HouseholdResponse> list(String apartment,String code,String status,int page,int size){
  if(page<0 || size<1 || size>100 || (status!=null && !Set.of("ACTIVE","INACTIVE").contains(status))) throw new BadRequestException("INVALID_FILTER");
  return repository.search(apartment,code,status,PageRequest.of(page,size,Sort.by("id"))).map(HouseholdResponse::from);
 }
 @Transactional(readOnly=true) public HouseholdResponse get(long id){return HouseholdResponse.from(find(id));}
 @Transactional public HouseholdResponse update(long id,UpdateHouseholdRequest request,long actor){
  if(request.areaM2()==null && request.address()==null) throw new BadRequestException("EMPTY_UPDATE");
  Household h=find(id); h.update(request.areaM2(),request.address(),clock.instant());
  Map<String,String> changes=new HashMap<>(); changes.put("id",h.getId().toString());
  if(request.areaM2()!=null) changes.put("areaM2",request.areaM2().toPlainString());
  audit.record(actor,"HOUSEHOLD_UPDATED","Household",h.getId().toString(),changes);
  return HouseholdResponse.from(h);
 }
 private Household find(long id){return repository.findById(id).orElseThrow(()->new NotFoundException("HOUSEHOLD_NOT_FOUND"));}
}
