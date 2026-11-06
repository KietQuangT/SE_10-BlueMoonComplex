package vn.edu.se10.bluemoon.identity;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.AopTestUtils;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class ResidentIntegrationIT extends AccountIntegrationBase {
 @MockitoSpyBean AuditService audit;
 long household(String suffix){jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 10, 'ACTIVE', NOW(6), NOW(6))",prefix+suffix,prefix+suffix);return jdbc.queryForObject("SELECT id FROM households WHERE household_code=?",Long.class,prefix+suffix);}
 org.springframework.test.web.servlet.ResultActions add(long id,String identity) throws Exception {return mvc.perform(post("/api/v1/households/"+id+"/residents").cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"fullName\":\"Private Name\",\"identityNumber\":\""+identity+"\"}"));}
 long addId(long id,String identity) throws Exception {return tools.jackson.databind.json.JsonMapper.builder().build().readTree(add(id,identity).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asLong();}
 org.springframework.test.web.servlet.ResultActions move(long id) throws Exception {return mvc.perform(post("/api/v1/residents/"+id+"/move-out").cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"effectiveDate\":\"2020-01-01\",\"reason\":\"private reason\"}"));}
 jakarta.servlet.http.Cookie residentLogin(long household) throws Exception {
  String username=prefix+"-resident";
  jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, household_id, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='RESIDENT'), ?, ?, 'Resident', 'ACTIVE', ?, NOW(6), NOW(6))",username,encoder.encode("test-only-pass"),household);
  var initial=mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();var first=remember(initial.getResponse().getCookie("SESSION"));
  String token=tools.jackson.databind.json.JsonMapper.builder().build().readTree(initial.getResponse().getContentAsString()).get("token").asString();
  return remember(mvc.perform(post("/api/v1/auth/login").cookie(first).header("X-CSRF-TOKEN",token).contentType("application/json").content("{\"username\":\""+username+"\",\"password\":\"test-only-pass\"}")).andExpect(status().isOk()).andReturn().getResponse().getCookie("SESSION"));
 }
 @AfterEach void cleanResidency(){jdbc.update("DELETE FROM audit_log WHERE actor_user_id IN (SELECT id FROM users WHERE username=?)",prefix+"-resident");jdbc.update("DELETE FROM users WHERE username=?",prefix+"-resident");jdbc.update("DELETE FROM residency_changes WHERE household_id IN (SELECT id FROM households WHERE household_code LIKE ?)",prefix+"%");jdbc.update("DELETE FROM residents WHERE household_id IN (SELECT id FROM households WHERE household_code LIKE ?)",prefix+"%");jdbc.update("DELETE FROM households WHERE household_code LIKE ?",prefix+"%");}
 @Test void residentCanOnlySeeOwnHouseholdAndNullHouseholdIsForbidden() throws Exception {
  long a=household("a"),b=household("b"),first=addId(a,prefix+"first"),second=addId(b,prefix+"second");
  long absent=addId(a,prefix+"absent"),moved=addId(a,prefix+"moved");jdbc.update("UPDATE residents SET residency_status='TEMPORARILY_ABSENT' WHERE id=?",absent);jdbc.update("UPDATE residents SET residency_status='MOVED_OUT' WHERE id=?",moved);
  var residentCookie=residentLogin(a);
  mvc.perform(get("/api/v1/households/me/residents").param("householdId",Long.toString(b)).header("householdId",Long.toString(b)).cookie(residentCookie)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(first)).andExpect(jsonPath("$[1].id").value(absent)).andExpect(jsonPath("$[0].identityNumber").doesNotExist());
  mvc.perform(get("/api/v1/households/"+b+"/residents").cookie(residentCookie)).andExpect(status().isForbidden());
  var noHousehold=UsernamePasswordAuthenticationToken.authenticated(new AuthenticatedUser(adminId,"resident","Name","RESIDENT",null,false),null,List.of(new SimpleGrantedAuthority("ROLE_RESIDENT")));
  mvc.perform(get("/api/v1/households/me/residents").with(authentication(noHousehold))).andExpect(status().isForbidden());
 }
 @Test void inactiveHouseholdIsRejected() throws Exception {long h=household("a");jdbc.update("UPDATE households SET status='INACTIVE' WHERE id=?",h);add(h,prefix+"id").andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("HOUSEHOLD_INVALID"));}
 @Test void duplicateRaceAcrossHouseholds(CapturedOutput output) throws Exception {
  long a=household("a"),b=household("b");String identity=prefix+"private";ExecutorService pool=Executors.newFixedThreadPool(8);CountDownLatch ready=new CountDownLatch(8),start=new CountDownLatch(1);
  try {List<Future<org.springframework.test.web.servlet.MvcResult>> futures=new ArrayList<>();for(int i=0;i<8;i++){final long h=i%2==0?a:b;futures.add(pool.submit(()->{ready.countDown();if(!start.await(10,TimeUnit.SECONDS))throw new TimeoutException();return add(h,identity).andReturn();}));}assertThat(ready.await(10,TimeUnit.SECONDS)).isTrue();start.countDown();int wins=0;for(var future:futures){var response=future.get(30,TimeUnit.SECONDS).getResponse();assertThat(response.getStatus()).isIn(201,409);if(response.getStatus()==201)wins++;else assertThat(response.getContentAsString()).doesNotContain(identity);}assertThat(wins).isEqualTo(1);}finally{start.countDown();pool.shutdownNow();assertThat(pool.awaitTermination(30,TimeUnit.SECONDS)).isTrue();}
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM residents WHERE identity_number=?",Integer.class,identity)).isEqualTo(1);assertThat(output.getOut()).doesNotContain(identity);
 }
 @Test void moveOutHistoryFilteringAndReuse() throws Exception {
  long h=household("a"), first=addId(h,prefix+"first"), second=addId(h,prefix+"second"), third=addId(h,prefix+"third");
  long other=household("b"),otherCurrent=addId(other,prefix+"other"),otherMoved=addId(other,prefix+"other-moved");move(otherMoved).andExpect(status().isOk());
  jdbc.update("UPDATE residents SET residency_status='TEMPORARILY_ABSENT' WHERE id=?",second);
  add(h,(prefix+"second").toUpperCase(Locale.ROOT)).andExpect(status().isConflict());
  mvc.perform(get("/api/v1/households/"+h+"/residents").param("status","MOVED_OUT").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
  move(first).andExpect(status().isOk()).andExpect(jsonPath("$.residencyStatus").value("MOVED_OUT"));move(first).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("RESIDENT_ALREADY_MOVED_OUT"));
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM residency_changes WHERE resident_id=? AND household_id=? AND effective_date='2020-01-01' AND change_type='MOVE_OUT' AND status='APPROVED' AND recorded_by=?",Integer.class,first,h,adminId)).isEqualTo(1);
  mvc.perform(get("/api/v1/households/"+h+"/residents").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
  mvc.perform(get("/api/v1/households/"+h+"/residents").param("size","1").param("page","1").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(third));
  mvc.perform(get("/api/v1/households/"+h+"/residents").param("status","MOVED_OUT").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].id").value(first));
  assertThat(jdbc.queryForObject("SELECT reason FROM residency_changes WHERE resident_id=? AND household_id=? AND effective_date='2020-01-01'",String.class,first,h)).isEqualTo("private reason");
  mvc.perform(get("/api/v1/households/"+h+"/residency-changes").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].residentId").value(first)).andExpect(jsonPath("$.content[0].changeType").value("MOVE_OUT")).andExpect(jsonPath("$.content[0].status").value("APPROVED")).andExpect(jsonPath("$.content[0].recordedBy").value(adminId)).andExpect(jsonPath("$.content[0].effectiveDate").value("2020-01-01")).andExpect(jsonPath("$.content[0].reason").doesNotExist());
  var logs=jdbc.queryForList("SELECT changes FROM audit_log WHERE actor_user_id=? AND entity_type='Resident'",String.class,adminId);
  assertThat(logs).hasSize(7); for(String log:logs)assertThat(log).doesNotContain("Private Name",prefix+"first",prefix+"second","private reason");
  add(h,prefix+"first").andExpect(status().isCreated());
  mvc.perform(patch("/api/v1/residents/"+third).cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"identityNumber\":\""+prefix+"second\"}")).andExpect(status().isConflict());
  mvc.perform(patch("/api/v1/residents/"+third).cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"fullName\":\"Changed\"}")).andExpect(status().isOk());
  mvc.perform(patch("/api/v1/residents/"+third).cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"identityNumber\":\""+prefix+"new\"}")).andExpect(status().isOk());
  assertAudit(first,"RESIDENT_CREATED",Set.of("id","householdId","residencyStatus"));assertAudit(first,"RESIDENT_MOVED_OUT",Set.of("id","householdId","effectiveDate"));
  var updates=jdbc.queryForList("SELECT changes FROM audit_log WHERE actor_user_id=? AND entity_type='Resident' AND entity_id=? AND action='RESIDENT_UPDATED' ORDER BY id",String.class,adminId,Long.toString(third));
  assertThat(updates).hasSize(2);var mapper=tools.jackson.databind.json.JsonMapper.builder().build();
  for(String update:updates){assertThat(mapper.readTree(update).propertyNames()).containsExactlyInAnyOrder("fields");assertThat(update).doesNotContain("Changed",prefix+"new");}
  assertThat(mapper.readTree(updates.get(0)).get("fields").asString()).contains("fullName");assertThat(mapper.readTree(updates.get(1)).get("fields").asString()).contains("identityNumber");
 }
 void assertAudit(long id,String action,Set<String> expected) throws Exception {
  var rows=jdbc.queryForList("SELECT changes FROM audit_log WHERE actor_user_id=? AND entity_type='Resident' AND entity_id=? AND action=?",String.class,adminId,Long.toString(id),action);
  assertThat(rows).hasSize(1);assertThat(tools.jackson.databind.json.JsonMapper.builder().build().readTree(rows.get(0)).propertyNames()).containsExactlyInAnyOrderElementsOf(expected);
 }
 @Test void concurrentPatchAndMoveOutUseSameGuardOrder() throws Exception {
  household("guard-fixture");
  // Existing data may contain earlier households: use the actual minimum as the FK parent.
  long min=jdbc.queryForObject("SELECT MIN(id) FROM households",Long.class);ExecutorService pool=Executors.newFixedThreadPool(2);
  try {for(int round=0;round<5;round++){
   String identity=prefix+"lock"+round;jdbc.update("INSERT INTO residents (household_id,full_name,identity_number,residency_status,created_at,updated_at) VALUES (?, 'Fixture', ?, 'RESIDING', NOW(6), NOW(6))",min,identity);
   long id=jdbc.queryForObject("SELECT id FROM residents WHERE household_id=? AND identity_number=?",Long.class,min,identity);
   try {CountDownLatch ready=new CountDownLatch(2),start=new CountDownLatch(1);String next=identity+"x";
    Future<Integer> patch=pool.submit(()->{ready.countDown();if(!start.await(10,TimeUnit.SECONDS))throw new TimeoutException();return mvc.perform(patch("/api/v1/residents/"+id).cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"identityNumber\":\""+next+"\"}")).andReturn().getResponse().getStatus();});
    Future<Integer> move=pool.submit(()->{ready.countDown();if(!start.await(10,TimeUnit.SECONDS))throw new TimeoutException();return move(id).andReturn().getResponse().getStatus();});
    assertThat(ready.await(10,TimeUnit.SECONDS)).isTrue();start.countDown();assertThat(patch.get(30,TimeUnit.SECONDS)).isIn(200,409);assertThat(move.get(30,TimeUnit.SECONDS)).isIn(200,409);
   } finally {jdbc.update("DELETE FROM residency_changes WHERE resident_id=?",id);jdbc.update("DELETE FROM residents WHERE id=?",id);}
  }}finally{pool.shutdownNow();assertThat(pool.awaitTermination(30,TimeUnit.SECONDS)).isTrue();}
 }
 @Test void concurrentMoveOutHasOneWinner() throws Exception {
  long h=household("a"),id=addId(h,prefix+"id");ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch ready=new CountDownLatch(2),start=new CountDownLatch(1);
  try {List<Future<Integer>> futures=new ArrayList<>();for(int i=0;i<2;i++)futures.add(pool.submit(()->{ready.countDown();if(!start.await(10,TimeUnit.SECONDS))throw new TimeoutException();return move(id).andReturn().getResponse().getStatus();}));assertThat(ready.await(10,TimeUnit.SECONDS)).isTrue();start.countDown();assertThat(List.of(futures.get(0).get(30,TimeUnit.SECONDS),futures.get(1).get(30,TimeUnit.SECONDS))).containsExactlyInAnyOrder(200,409);}finally{start.countDown();pool.shutdownNow();assertThat(pool.awaitTermination(30,TimeUnit.SECONDS)).isTrue();}
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM residency_changes WHERE resident_id=?",Integer.class,id)).isEqualTo(1);
 }
 @Test void auditFailuresRollbackAllActions() throws Exception {
  long h=household("a");AuditService target=AopTestUtils.getUltimateTargetObject(audit);
  doThrow(new IllegalStateException()).when(target).record(anyLong(),eq("RESIDENT_CREATED"),anyString(),anyString(),anyMap());add(h,prefix+"bad").andExpect(status().isInternalServerError());verify(target).record(anyLong(),eq("RESIDENT_CREATED"),anyString(),anyString(),anyMap());assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM residents WHERE household_id=?",Integer.class,h)).isZero();assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=? AND action='RESIDENT_CREATED'",Integer.class,adminId)).isZero();reset(target);
  long id=addId(h,prefix+"good");
  doThrow(new IllegalStateException()).when(target).record(anyLong(),eq("RESIDENT_UPDATED"),anyString(),anyString(),anyMap());mvc.perform(patch("/api/v1/residents/"+id).cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"fullName\":\"Changed\"}")).andExpect(status().isInternalServerError());verify(target).record(anyLong(),eq("RESIDENT_UPDATED"),anyString(),anyString(),anyMap());assertThat(jdbc.queryForObject("SELECT full_name FROM residents WHERE id=?",String.class,id)).isEqualTo("Private Name");reset(target);
  doThrow(new IllegalStateException()).when(target).record(anyLong(),eq("RESIDENT_MOVED_OUT"),anyString(),anyString(),anyMap());move(id).andExpect(status().isInternalServerError());verify(target).record(anyLong(),eq("RESIDENT_MOVED_OUT"),anyString(),anyString(),anyMap());assertThat(jdbc.queryForObject("SELECT residency_status FROM residents WHERE id=?",String.class,id)).isEqualTo("RESIDING");assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM residency_changes WHERE resident_id=?",Integer.class,id)).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=? AND action IN ('RESIDENT_UPDATED','RESIDENT_MOVED_OUT')",Integer.class,adminId)).isZero();
 }
}
