package vn.edu.se10.bluemoon.identity;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.AopTestUtils;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class DeclarationIntegrationIT extends AccountIntegrationBase {
 @MockitoSpyBean AuditService audit;
 long h1,h2,r1,r2,u1,u2; String date=LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(1).toString();
 @BeforeEach void fixtures(){
  for(String suffix:List.of("a","b"))jdbc.update("INSERT INTO households (household_code,apartment_code,area_m2,status,created_at,updated_at) VALUES (?,?,10,'ACTIVE',NOW(6),NOW(6))",prefix+suffix,prefix+suffix);
  h1=house("a");h2=house("b");
  for(long h:List.of(h1,h2))jdbc.update("INSERT INTO residents (household_id,full_name,residency_status,created_at,updated_at) VALUES (?,'Test Resident','RESIDING',NOW(6),NOW(6))",h);
  r1=resident(h1);r2=resident(h2);
  for(int n=1;n<=2;n++)jdbc.update("INSERT INTO users (role_id,username,password_hash,full_name,status,household_id,created_at,updated_at) VALUES ((SELECT id FROM roles WHERE code='RESIDENT'),?,?,'Test','ACTIVE',?,NOW(6),NOW(6))",prefix+"u"+n,encoder.encode("test-only-pass"),n==1?h1:h2);
  u1=user("u1");u2=user("u2");
 }
 long house(String suffix){return jdbc.queryForObject("SELECT id FROM households WHERE household_code=?",Long.class,prefix+suffix);} long resident(long h){return jdbc.queryForObject("SELECT id FROM residents WHERE household_id=?",Long.class,h);} long user(String s){return jdbc.queryForObject("SELECT id FROM users WHERE username=?",Long.class,prefix+s);}
 UsernamePasswordAuthenticationToken auth(long id,long h){return UsernamePasswordAuthenticationToken.authenticated(new AuthenticatedUser(id,"x","Test","RESIDENT",h,false),null,List.of(new SimpleGrantedAuthority("ROLE_RESIDENT")));}
 org.springframework.test.web.servlet.ResultActions createRequest(long id,long user,long household) throws Exception{return mvc.perform(post("/api/v1/residency-declarations").with(authentication(auth(user,household))).with(csrf()).contentType("application/json").content("{\"residentId\":"+id+",\"effectiveDate\":\""+date+"\",\"reason\":\"private reason\"}"));}
 long createId(long id) throws Exception{return tools.jackson.databind.json.JsonMapper.builder().build().readTree(createRequest(id,u1,h1).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asLong();}
 @AfterEach void cleanDeclarations(){jdbc.update("DELETE FROM residency_changes WHERE household_id IN (?,?)",h1,h2);jdbc.update("DELETE FROM residents WHERE household_id IN (?,?)",h1,h2);jdbc.update("UPDATE users SET household_id=NULL WHERE username LIKE ?",prefix+"%");jdbc.update("DELETE FROM households WHERE id IN (?,?)",h1,h2);}
 @Test void ownershipDecisionAndReturn() throws Exception {
  createRequest(r2,u1,h1).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("RESIDENT_NOT_FOUND"));long id=createId(r1);
  mvc.perform(get("/api/v1/residency-declarations/mine").with(authentication(auth(u2,h2)))).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
  mvc.perform(post("/api/v1/residency-declarations/"+id+"/cancel").with(authentication(auth(u2,h2))).with(csrf())).andExpect(status().isNotFound());
  mvc.perform(post("/api/v1/residency-declarations/"+id+"/approve").cookie(cookie).header("X-CSRF-TOKEN",csrf)).andExpect(status().isOk());
  mvc.perform(post("/api/v1/residency-declarations/"+id+"/approve").cookie(cookie).header("X-CSRF-TOKEN",csrf)).andExpect(status().isConflict());
  mvc.perform(get("/api/v1/households/"+h1+"/residents").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].residencyStatus").value("TEMPORARILY_ABSENT"));
  mvc.perform(post("/api/v1/residents/"+r1+"/return").cookie(cookie).header("X-CSRF-TOKEN",csrf)).andExpect(status().isOk()).andExpect(jsonPath("$.residencyStatus").value("RESIDING"));
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM residency_changes WHERE resident_id=? AND change_type='RETURN'",Integer.class,r1)).isEqualTo(1);
  long rejected=createId(r1);mvc.perform(post("/api/v1/residency-declarations/"+rejected+"/reject").cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"decisionNote\":\"private note\"}")).andExpect(status().isOk());
  mvc.perform(get("/api/v1/residency-declarations/mine").with(authentication(auth(u1,h1)))).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].decisionNote").value("private note"));
  var logs=jdbc.queryForList("SELECT changes FROM audit_log WHERE actor_user_id IN (?,?) AND (action LIKE 'ABSENCE_%' OR action='RESIDENT_RETURNED')",String.class,u1,adminId);assertThat(logs.toString()).doesNotContain("private reason","private note","Test Resident");
  var actions=jdbc.queryForList("SELECT action FROM audit_log WHERE actor_user_id IN (?,?) AND (action LIKE 'ABSENCE_%' OR action='RESIDENT_RETURNED')",String.class,u1,adminId);assertThat(actions).contains("ABSENCE_REQUESTED","ABSENCE_APPROVED","ABSENCE_REJECTED","RESIDENT_RETURNED");
 }
 @Test void concurrentCreatesHaveOneWinner() throws Exception {ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);try{List<Future<org.springframework.mock.web.MockHttpServletResponse>> f=new ArrayList<>();for(int n=0;n<2;n++)f.add(pool.submit(()->{start.await();return createRequest(r1,u1,h1).andReturn().getResponse();}));start.countDown();var responses=List.of(f.get(0).get(30,TimeUnit.SECONDS),f.get(1).get(30,TimeUnit.SECONDS));assertThat(responses.stream().map(x->x.getStatus()).toList()).containsExactlyInAnyOrder(201,409);assertThat(responses.stream().filter(x->x.getStatus()==409).findFirst().orElseThrow().getContentAsString()).contains("DECLARATION_ALREADY_PENDING");assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM residency_changes WHERE resident_id=? AND status='PENDING'",Integer.class,r1)).isEqualTo(1);}finally{pool.shutdownNow();}}
 @Test void approveAndCancelRaceHasOneWinner() throws Exception {
  long id=createId(r1);ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
  try{Future<Integer> a=pool.submit(()->{start.await();return mvc.perform(post("/api/v1/residency-declarations/"+id+"/approve").cookie(cookie).header("X-CSRF-TOKEN",csrf)).andReturn().getResponse().getStatus();});Future<Integer> b=pool.submit(()->{start.await();return mvc.perform(post("/api/v1/residency-declarations/"+id+"/cancel").with(authentication(auth(u1,h1))).with(csrf())).andReturn().getResponse().getStatus();});start.countDown();assertThat(List.of(a.get(30,TimeUnit.SECONDS),b.get(30,TimeUnit.SECONDS))).containsExactlyInAnyOrder(200,409);}finally{pool.shutdownNow();}
 }
 @Test void nullHouseholdForbiddenAndOnlySubmitterCanCancel() throws Exception {
  long id=createId(r1);jdbc.update("UPDATE users SET household_id=? WHERE id=?",h1,u2);
  mvc.perform(post("/api/v1/residency-declarations/"+id+"/cancel").with(authentication(auth(u2,h1))).with(csrf())).andExpect(status().isForbidden());
  jdbc.update("UPDATE users SET household_id=NULL WHERE id=?",u2);
  mvc.perform(get("/api/v1/residency-declarations/mine").with(authentication(auth(u2,h1)))).andExpect(status().isForbidden());
 }
 @Test void filteringAndCancellationPreserveHistory() throws Exception {
  long id=createId(r1);
  mvc.perform(get("/api/v1/residency-declarations").cookie(cookie).param("status","PENDING").param("q","Test").param("apartment",prefix+"a")).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(id)).andExpect(jsonPath("$.content[0].cancellable").value(false)).andExpect(jsonPath("$.content[0].recordedBy").doesNotExist());
  mvc.perform(get("/api/v1/residency-declarations").cookie(cookie).param("q","%")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_FILTER"));
  mvc.perform(get("/api/v1/residents/suggest").cookie(cookie).param("q","Test")).andExpect(status().isOk()).andExpect(jsonPath("$[0].residentId").exists()).andExpect(jsonPath("$[0].identityNumber").doesNotExist());
  mvc.perform(post("/api/v1/residency-declarations/"+id+"/cancel").with(authentication(auth(u1,h1))).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM residency_changes WHERE id=? AND status='CANCELLED'",Integer.class,id)).isEqualTo(1);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=? AND action='ABSENCE_CANCELLED'",Integer.class,u1)).isEqualTo(1);
 }
 @Test void unavailableResidentsAndSequentialPendingAreRejected() throws Exception {
  for(String state:List.of("TEMPORARILY_ABSENT","MOVED_OUT")) {jdbc.update("UPDATE residents SET residency_status=? WHERE id=?",state,r1);createRequest(r1,u1,h1).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("RESIDENT_NOT_AVAILABLE"));}
  jdbc.update("UPDATE residents SET residency_status='RESIDING' WHERE id=?",r1);createId(r1);createRequest(r1,u1,h1).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DECLARATION_ALREADY_PENDING"));
 }
 @Test void decisionsRequirePendingAndAvailableResident() throws Exception {
  long id=createId(r1);jdbc.update("UPDATE residents SET residency_status='MOVED_OUT' WHERE id=?",r1);
  mvc.perform(post("/api/v1/residency-declarations/"+id+"/approve").cookie(cookie).header("X-CSRF-TOKEN",csrf)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("RESIDENT_NOT_AVAILABLE"));
  mvc.perform(post("/api/v1/residency-declarations/"+id+"/reject").cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"decisionNote\":\"no\"}")).andExpect(status().isOk());
  mvc.perform(post("/api/v1/residency-declarations/"+id+"/reject").cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"decisionNote\":\"no\"}")).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DECLARATION_NOT_PENDING"));
  mvc.perform(post("/api/v1/residency-declarations/"+id+"/cancel").with(authentication(auth(u1,h1))).with(csrf())).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DECLARATION_NOT_PENDING"));
  mvc.perform(post("/api/v1/residents/"+r1+"/return").cookie(cookie).header("X-CSRF-TOKEN",csrf)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("RESIDENT_NOT_ABSENT"));
 }
 @Test void validationAndFiltersReturnCodes() throws Exception {
  long id=createId(r1);
  for(String body:List.of("{}","{\"decisionNote\":\"  \"}","{\"decisionNote\":\""+"x".repeat(256)+"\"}")) mvc.perform(post("/api/v1/residency-declarations/"+id+"/reject").cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content(body)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
  mvc.perform(post("/api/v1/residency-declarations").with(authentication(auth(u1,h1))).with(csrf()).contentType("application/json").content("{\"residentId\":"+r1+",\"effectiveDate\":\""+date+"\",\"reason\":\""+"x".repeat(256)+"\"}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
  for(String[] filter:new String[][]{{"status","UNKNOWN"},{"page","-1"},{"size","0"},{"size","101"}}) mvc.perform(get("/api/v1/residency-declarations").cookie(cookie).param(filter[0],filter[1])).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_FILTER"));
  mvc.perform(get("/api/v1/residents/suggest").cookie(cookie).param("q","a")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_FILTER"));
 }
 @Test void nullHouseholdCannotCreateOrCancel() throws Exception {
  long id=createId(r1);jdbc.update("UPDATE users SET household_id=NULL WHERE id=?",u1);
  createRequest(r1,u1,h1).andExpect(status().isForbidden());
  mvc.perform(post("/api/v1/residency-declarations/"+id+"/cancel").with(authentication(auth(u1,h1))).with(csrf())).andExpect(status().isForbidden());
 }
 @Test void suggestionsEscapeWildcardsLimitResultsAndExcludeMovedOut() throws Exception {
  for(int n=0;n<12;n++) jdbc.update("INSERT INTO residents (household_id,full_name,residency_status,created_at,updated_at) VALUES (?,?,'RESIDING',NOW(6),NOW(6))",h1,prefix+" Search " + n);
  jdbc.update("INSERT INTO residents (household_id,full_name,residency_status,created_at,updated_at) VALUES (?,?,'MOVED_OUT',NOW(6),NOW(6))",h1,prefix+" Exclusive");
  mvc.perform(get("/api/v1/residents/suggest").cookie(cookie).param("q",prefix+" Search")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(10)).andExpect(jsonPath("$[0].identityNumber").doesNotExist()).andExpect(jsonPath("$[0].phone").doesNotExist());
  for(String q:List.of(prefix+" Exclusive",prefix+"%",prefix+"_")) mvc.perform(get("/api/v1/residents/suggest").cookie(cookie).param("q",q)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
 }
 @Test void queueExcludesMoveOutAndReturn() throws Exception {
  long id=createId(r1);jdbc.update("INSERT INTO residency_changes (household_id,resident_id,change_type,status,effective_date,recorded_by,created_at) VALUES (?,?,'MOVE_OUT','APPROVED',?,?,NOW(6)),(?,?,'RETURN','APPROVED',?,?,NOW(6))",h1,r1,date,u1,h1,r1,date,u1);
  mvc.perform(get("/api/v1/residency-declarations").cookie(cookie).param("apartment",prefix+"a")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].id").value(id)).andExpect(jsonPath("$.content[0].changeType").value("TEMPORARY_ABSENCE"));
 }
 @Test void auditFailureRollsBackApproval() throws Exception {
  long id=createId(r1);AuditService target=AopTestUtils.getUltimateTargetObject(audit);
  try {doThrow(new IllegalStateException()).when(target).record(anyLong(),eq("ABSENCE_APPROVED"),anyString(),anyString(),anyMap());
   mvc.perform(post("/api/v1/residency-declarations/"+id+"/approve").cookie(cookie).header("X-CSRF-TOKEN",csrf)).andExpect(status().isInternalServerError());
   assertThat(jdbc.queryForObject("SELECT status FROM residency_changes WHERE id=?",String.class,id)).isEqualTo("PENDING");assertThat(jdbc.queryForObject("SELECT residency_status FROM residents WHERE id=?",String.class,r1)).isEqualTo("RESIDING");
  } finally {reset(target);}
 }

 @Test void moveOutCancelsOnlyPendingAbsencesAndAuditsWithoutReason() throws Exception {
  long approved=createId(r1);
  mvc.perform(post("/api/v1/residency-declarations/"+approved+"/approve").cookie(cookie).header("X-CSRF-TOKEN",csrf)).andExpect(status().isOk());
  mvc.perform(post("/api/v1/residents/"+r1+"/return").cookie(cookie).header("X-CSRF-TOKEN",csrf)).andExpect(status().isOk());
  long rejected=createId(r1);
  mvc.perform(post("/api/v1/residency-declarations/"+rejected+"/reject").cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"decisionNote\":\"no\"}")).andExpect(status().isOk());
  long pending=createId(r1);
  mvc.perform(post("/api/v1/residents/"+r1+"/move-out").cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"effectiveDate\":\""+LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))+"\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.residencyStatus").value("MOVED_OUT"));
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM residency_changes WHERE id=? AND status='CANCELLED' AND decided_at IS NOT NULL AND approved_by IS NULL AND decision_note IS NULL",Integer.class,pending)).isEqualTo(1);
  assertThat(jdbc.queryForObject("SELECT status FROM residency_changes WHERE id=?",String.class,approved)).isEqualTo("APPROVED");
  assertThat(jdbc.queryForObject("SELECT status FROM residency_changes WHERE id=?",String.class,rejected)).isEqualTo("REJECTED");
  mvc.perform(post("/api/v1/residency-declarations/"+pending+"/approve").cookie(cookie).header("X-CSRF-TOKEN",csrf)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DECLARATION_NOT_PENDING"));
  var logs=jdbc.queryForList("SELECT changes FROM audit_log WHERE actor_user_id=? AND action IN ('ABSENCE_CANCELLED','RESIDENT_MOVED_OUT') AND JSON_UNQUOTE(JSON_EXTRACT(changes,'$.id')) IN (?,?)",String.class,adminId,Long.toString(pending),Long.toString(r1));
  assertThat(logs).hasSize(2); assertThat(logs.toString()).contains("MOVE_OUT", "cause").doesNotContain("private reason", "Test Resident");
 }
 @Test void moveOutFromTemporaryAbsenceSucceeds() throws Exception {
  long id=createId(r1); mvc.perform(post("/api/v1/residency-declarations/"+id+"/approve").cookie(cookie).header("X-CSRF-TOKEN",csrf)).andExpect(status().isOk());
  mvc.perform(post("/api/v1/residents/"+r1+"/move-out").cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"effectiveDate\":\""+LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))+"\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.residencyStatus").value("MOVED_OUT"));
  assertThat(jdbc.queryForObject("SELECT status FROM residency_changes WHERE id=?",String.class,id)).isEqualTo("APPROVED");
 }
}
