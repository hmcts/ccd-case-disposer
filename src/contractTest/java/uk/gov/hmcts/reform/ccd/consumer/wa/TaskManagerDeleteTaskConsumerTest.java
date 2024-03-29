package uk.gov.hmcts.reform.ccd.consumer.wa;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.SpringBootContractBaseTest;

public class TaskManagerDeleteTaskConsumerTest extends SpringBootContractBaseTest {

    private static final String WA_URL = "/task";
    private static final String WA_DELETE_TASK = WA_URL + "/delete";

    @Pact(provider = "wa_task_management_api_delete_task_by_id", consumer = "wa_task_management_api")
    public RequestResponsePact executeDeleteTaskById201(PactDslWithProvider builder) {

        return builder
            .given("delete a task using case reference id")
            .uponReceiving("Request to delete")
            .path(WA_DELETE_TASK)
            .method(HttpMethod.POST.toString())
            .body(deleteTaskWithRequest(), String.valueOf(ContentType.JSON))
            .matchHeader(SERVICE_AUTHORIZATION, SERVICE_AUTH_TOKEN)
            .willRespondWith()
            .status(HttpStatus.CREATED.value())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeDeleteTaskById201", pactVersion = PactSpecVersion.V3)
    void testDeleteTaskByTaskId201(MockServer mockServer) {

        RestAssured
            .given()
            .headers(getHttpHeaders())
            .contentType(ContentType.JSON)
            .body(deleteTaskWithRequest())
            .post(mockServer.getUrl() + WA_DELETE_TASK)
            .then()
            .statusCode(201);

    }

    private String deleteTaskWithRequest() {
        return """
            {
              "deleteCaseTasksAction": {
                "caseRef": "1234567890123456"
              }
            }
            """;
    }
}
