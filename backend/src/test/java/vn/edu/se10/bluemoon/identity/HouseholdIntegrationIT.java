package vn.edu.se10.bluemoon.identity;
import java.util.*;
import java.util.concurrent.*;
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
  String code=prefix+"-race", apartment=prefix+"-raceapt"; ExecutorService pool=Executors.newFixedThreadPool(8); CountDownLatch start=new CountDownLatch(1);
  try {List<Future<Integer>> results=new ArrayList<>(); for(int i=0;i<8;i++) results.add(pool.submit(()->{start.await();return createHousehold(householdBody(code,apartment,"1")).andReturn().getResponse().getStatus();})); start.countDown(); int wins=0; for(var result:results){int status=result.get(); assertThat(status).isIn(201,409); if(status==201) wins++;} assertThat(wins).isEqualTo(1); } finally {pool.shutdownNow();}
  assertThat(output.getOut()).doesNotContain(code,apartment);
 }
 @Test void searchPagingPatchAndAudit() throws Exception {
  var result=createHousehold(householdBody(prefix+"-one",prefix+"-apt","12.25")).andExpect(status().isCreated()).andReturn();
  long id=tools.jackson.databind.json.JsonMapper.builder().build().readTree(result.getResponse().getContentAsString()).get("id").asLong();
  for(String query:List.of("householdCode="+prefix+"-one","apartmentCode="+prefix+"-apt","status=ACTIVE&householdCode="+prefix+"-one")) mvc.perform(get("/api/v1/households?"+query+"&size=1").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(id));
  mvc.perform(get("/api/v1/households?size=101").cookie(cookie)).andExpect(status().isBadRequest());
  mvc.perform(patch("/api/v1/households/"+id).cookie(cookie).header("X-CSRF-TOKEN",csrf).contentType("application/json").content("{\"householdCode\":\"changed\",\"areaM2\":15,\"address\":\"new private address\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.householdCode").value(prefix+"-one"));
  var changes=jdbc.queryForList("SELECT changes FROM audit_log WHERE entity_type='Household' AND actor_user_id=?",String.class,adminId); assertThat(changes).hasSize(2); for(String change:changes) assertThat(change.toLowerCase(Locale.ROOT)).doesNotContain("address","email","phone","password","identitynumber");
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
