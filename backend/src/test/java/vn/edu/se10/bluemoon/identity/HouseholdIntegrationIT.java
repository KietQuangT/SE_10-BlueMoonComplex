package vn.edu.se10.bluemoon.identity;
import java.util.*;
import java.util.concurrent.*;
import java.math.BigDecimal;
import org.springframework.test.web.servlet.MvcResult;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.AopTestUtils;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class HouseholdIntegrationIT extends AccountIntegrationBase {
 @MockitoSpyBean AuditService audit;
 String householdBody(String code,String apartment,String area){return "{\"householdCode\":\""+code+"\",\"apartmentCode\":\""+apartment+"\",\"areaM2\":"+area+",\"address\":\"private address\"}";}
 org.springframework.test.web.servlet.ResultActions createHousehold(String body) throws Exception {return mvc.perform(post("/api/v1/households").cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content(body));}
 @AfterEach void cleanHouseholds(){jdbc.update("DELETE FROM audit_log WHERE entity_type='Household' AND actor_user_id=?",adminId);jdbc.update("DELETE FROM households WHERE household_code LIKE ?",prefix+"%");}
 @Test void duplicatesCaseInsensitiveAndNoDisclosure(CapturedOutput output) throws Exception {
  String code=prefix+"-code", apartment=prefix+"-apt";
  createHousehold(householdBody(code,apartment,"10.20")).andExpect(status().isCreated());
  for(String body:List.of(householdBody(code,prefix+"-other","10"),householdBody(code.toUpperCase(Locale.ROOT),prefix+"-other","10"),householdBody(prefix+"-other",apartment,"10"),householdBody(prefix+"-other",apartment.toUpperCase(Locale.ROOT),"10")))
   createHousehold(body).andExpect(status().isConflict()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(code)))).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(apartment))));
  assertThat(output.getOut()).doesNotContain(code,apartment);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE action='HOUSEHOLD_CREATED' AND actor_user_id=?",Integer.class,adminId)).isEqualTo(1);
 }
 @Test void concurrentDuplicatesHaveOneWinner(CapturedOutput output) throws Exception {
  String code=prefix+"-race", apartment=prefix+"-raceapt"; ExecutorService pool=Executors.newFixedThreadPool(8); CountDownLatch ready=new CountDownLatch(8), start=new CountDownLatch(1);
  try {
   List<Future<MvcResult>> results=new ArrayList<>();
   for(int i=0;i<8;i++) results.add(pool.submit(()->{ready.countDown(); if(!start.await(10,TimeUnit.SECONDS)) throw new TimeoutException(); return createHousehold(householdBody(code,apartment,"1")).andReturn();}));
   assertThat(ready.await(10,TimeUnit.SECONDS)).isTrue(); start.countDown(); int wins=0;
   for(var future:results){var response=future.get(30,TimeUnit.SECONDS).getResponse(); int status=response.getStatus(); assertThat(status).isIn(201,409); if(status==201) wins++; else assertThat(response.getContentAsString()).doesNotContain(code,apartment);}
   assertThat(wins).isEqualTo(1);
  } finally {start.countDown(); pool.shutdownNow();}
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM households WHERE household_code=?",Integer.class,code)).isEqualTo(1);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE action='HOUSEHOLD_CREATED' AND entity_type='Household' AND entity_id=(SELECT CAST(id AS CHAR) FROM households WHERE household_code=?)",Integer.class,code)).isEqualTo(1);
  assertThat(output.getOut()).doesNotContain(code,apartment);
 }
 long createId(String code,String apartment) throws Exception {
  var result=createHousehold(householdBody(code,apartment,"12.25")).andExpect(status().isCreated()).andReturn();
  return tools.jackson.databind.json.JsonMapper.builder().build().readTree(result.getResponse().getContentAsString()).get("id").asLong();
 }
 @Test void searchPagingPatchAndAudit() throws Exception {
  int initial=jdbc.queryForObject("SELECT COUNT(*) FROM households",Integer.class);
  long first=createId(prefix+"-one",prefix+"-apt1"), second=createId(prefix+"-two",prefix+"-apt2"), third=createId(prefix+"-three",prefix+"-apt3");
  jdbc.update("UPDATE households SET status='INACTIVE' WHERE id=?",second);
  mvc.perform(get("/api/v1/households").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(initial+3));
  for(String[] filter:List.of(new String[]{"householdCode",prefix+"-one"},new String[]{"apartmentCode",prefix+"-apt1"}))
   mvc.perform(get("/api/v1/households").param(filter[0],filter[1]).cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].id").value(first));
  long pageOne=jdbc.queryForObject("SELECT id FROM households ORDER BY id LIMIT 1 OFFSET 1",Long.class);
  mvc.perform(get("/api/v1/households").param("page","1").param("size","1").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(pageOne));
  mvc.perform(get("/api/v1/households").param("page",Integer.toString(initial+1)).param("size","1").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(second));
  mvc.perform(get("/api/v1/households").param("householdCode",prefix+"-two").param("status","INACTIVE").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].id").value(second));
  mvc.perform(get("/api/v1/households").param("householdCode",prefix+"-two").param("status","ACTIVE").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
  mvc.perform(get("/api/v1/households").param("householdCode",prefix+"-three").param("status","ACTIVE").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(third));
  mvc.perform(get("/api/v1/households?status=UNKNOWN").cookie(cookie)).andExpect(status().isBadRequest());
  mvc.perform(get("/api/v1/households?size=101").cookie(cookie)).andExpect(status().isBadRequest());
  mvc.perform(patch("/api/v1/households/"+first).cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{}")).andExpect(status().isBadRequest());
  mvc.perform(patch("/api/v1/households/"+first).cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"householdCode\":\"changed\",\"apartmentCode\":\"changed\",\"areaM2\":15,\"address\":\"new private address\"}")).andExpect(status().isOk());
  var row=jdbc.queryForMap("SELECT area_m2, household_code, apartment_code FROM households WHERE id=?",first);
  assertThat((BigDecimal)row.get("area_m2")).isEqualByComparingTo("15.00");
  assertThat(row.get("household_code")).isEqualTo(prefix+"-one"); assertThat(row.get("apartment_code")).isEqualTo(prefix+"-apt1");
  var changes=jdbc.queryForList("SELECT changes FROM audit_log WHERE entity_type='Household' AND actor_user_id=?",String.class,adminId); assertThat(changes).hasSize(4); for(String change:changes) assertThat(change.toLowerCase(Locale.ROOT)).doesNotContain("address","email","phone","password","identitynumber");
 }
 @Test void updateAuditFailureRollsBackHousehold() throws Exception {
  long id=createId(prefix+"-rollback",prefix+"-rollbackapt");
  AuditService target=AopTestUtils.getUltimateTargetObject(audit);
  doThrow(new IllegalStateException()).when(target).record(anyLong(),eq("HOUSEHOLD_UPDATED"),anyString(),anyString(),anyMap());
  mvc.perform(patch("/api/v1/households/"+id).cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"areaM2\":15}")).andExpect(status().isInternalServerError());
  assertThat(jdbc.queryForObject("SELECT area_m2 FROM households WHERE id=?",BigDecimal.class,id)).isEqualByComparingTo("12.25");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE action='HOUSEHOLD_UPDATED' AND entity_type='Household' AND entity_id=?",Integer.class,Long.toString(id))).isZero();
 }
 @Test void invalidDataAndAuditFailureRollBack() throws Exception {
  for(String area:List.of("0","-1","1.001","null","100000000")) createHousehold(householdBody(prefix+"-bad",prefix+"-apt",area)).andExpect(status().isBadRequest());
  createHousehold(householdBody("x".repeat(51),prefix+"-apt","1")).andExpect(status().isBadRequest());
  createHousehold(householdBody(prefix+"-bad","x".repeat(51),"1")).andExpect(status().isBadRequest());
  createHousehold(householdBody(prefix+"-bad",prefix+"-apt","1").replace("private address","x".repeat(256))).andExpect(status().isBadRequest());
  AuditService target=AopTestUtils.getUltimateTargetObject(audit); doThrow(new IllegalStateException()).when(target).record(anyLong(),eq("HOUSEHOLD_CREATED"),anyString(),anyString(),anyMap());
  createHousehold(householdBody(prefix+"-failure",prefix+"-apt","1")).andExpect(status().isInternalServerError());
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM households WHERE household_code=?",Integer.class,prefix+"-failure")).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=? AND entity_type='Household'",Integer.class,adminId)).isZero();
 }
}
