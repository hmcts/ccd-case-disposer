package uk.gov.hmcts.reform.ccd;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostRequest;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.data.lau.ActionLog;
import uk.gov.hmcts.reform.ccd.data.lau.CaseActionPostRequestResponse;
import uk.gov.hmcts.reform.ccd.data.tm.DeleteCaseTasksAction;
import uk.gov.hmcts.reform.ccd.data.tm.DeleteTasksRequest;
import uk.gov.hmcts.reform.ccd.service.remote.clients.DocumentClient;
import uk.gov.hmcts.reform.ccd.service.remote.clients.HearingClient;
import uk.gov.hmcts.reform.ccd.service.remote.clients.LauClient;
import uk.gov.hmcts.reform.ccd.service.remote.clients.RoleAssignmentClient;
import uk.gov.hmcts.reform.ccd.service.remote.clients.TasksClient;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_DOCUMENT_PATH;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_HEARINGS_PATH;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_ROLE_PATH;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_TASKS_PATH;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.LAU_SAVE_PATH;

@SpringBootTest
@ActiveProfiles("integrationtest")
@ComponentScan({"uk.gov.hmcts.reform.ccd"})
@TestPropertySource(properties = {
    "remote.document.store.host=http://localhost:8080",
    "remote.role.assignment.host=http://localhost:8080",
    "remote.log.and.audit.host=http://localhost:8080",
    "remote.tasks.host=http://localhost:8080",
    "remote.hearing.host=http://localhost:8080",
})
class TestClientRetry {
    protected static final WireMockServer WIREMOCK_SERVER = new WireMockServer(8080);

    static {
        if (!WIREMOCK_SERVER.isRunning()) {
            WIREMOCK_SERVER.start();
        }

        WIREMOCK_SERVER.stubFor(post(urlPathMatching(DELETE_DOCUMENT_PATH))
                                    .willReturn(aResponse()
                                                    .withStatus(501)
                                                    .withBody("Document Delete : Not Implemented")));

        WIREMOCK_SERVER.stubFor(post(urlPathMatching(DELETE_ROLE_PATH))
                                    .willReturn(aResponse()
                                                    .withStatus(502)
                                                    .withBody("Role Assignment Delete : Bad gateway")));

        WIREMOCK_SERVER.stubFor(post(urlPathMatching(LAU_SAVE_PATH))
                                    .willReturn(aResponse()
                                                    .withStatus(503)
                                                    .withBody("Lau Save : Service Unavailable")));

        WIREMOCK_SERVER.stubFor(post(urlPathMatching(DELETE_TASKS_PATH))
                                    .willReturn(aResponse()
                                                    .withStatus(504)
                                                    .withBody("Task Delete : Gateway Timeout")));

        WIREMOCK_SERVER.stubFor(delete(urlPathMatching(DELETE_HEARINGS_PATH))
                                    .willReturn(aResponse()
                                                    .withStatus(502)
                                                    .withBody("Document Delete : Bad gateway")));

    }

    @Autowired
    private DocumentClient documentClient;

    @Autowired
    private RoleAssignmentClient roleAssignmentClient;

    @Autowired
    private TasksClient tasksClient;

    @Autowired
    private LauClient lauClient;

    @Autowired
    private HearingClient hearingClient;

    @Test
    void testFeignDocumentClientRetry() {


        DocumentsDeletePostRequest request = new DocumentsDeletePostRequest("12345");

        try {
            documentClient.deleteDocument("serviceAuthHeader", request);
        } catch (Exception e) {
            Assertions.assertEquals("Feign Client Service unavailable: 501, Reason : Not Implemented", e.getMessage());
        }
        WIREMOCK_SERVER.verify(3, postRequestedFor(urlPathMatching(DELETE_DOCUMENT_PATH)));
    }

    @Test
    void testFeignRoleAssignmentDeleteRetry() {

        RoleAssignmentsPostRequest request = new RoleAssignmentsPostRequest("12345");

        try {
            roleAssignmentClient.deleteRoleAssignment("serviceAuthHeader","authHeader", request);
        } catch (Exception e) {
            Assertions.assertEquals("Feign Client Service unavailable: 502, Reason : Bad Gateway", e.getMessage());
        }
        WIREMOCK_SERVER.verify(3, postRequestedFor(urlPathMatching(DELETE_ROLE_PATH)));
    }

    @Test
    void testFeignLauClientRetry() {
        CaseActionPostRequestResponse request = new CaseActionPostRequestResponse(
            ActionLog.builder()
                .userId("1")
                .caseAction("DELETE")
                .caseTypeId("FT_MasterCaseType")
                .caseRef("12345")
                .caseJurisdictionId("BEFTA_MASTER")
                .timestamp("2021-08-23T22:20:05.023Z")
                .build());

        try {
            lauClient.postLauAudit("serviceAuthHeader", request);
        } catch (Exception e) {
            Assertions.assertEquals("Feign Client Service unavailable: 503, Reason : Service Unavailable",
                                    e.getMessage());
        }
        WIREMOCK_SERVER.verify(3, postRequestedFor(urlPathMatching(LAU_SAVE_PATH)));
    }

    @Test
    void testFeignTaskDeleteRetry() {
        DeleteTasksRequest request = new DeleteTasksRequest(new DeleteCaseTasksAction("12345"));

        try {
            tasksClient.deleteTasks("serviceAuthHeader","authHeader",  request);
        } catch (Exception e) {
            Assertions.assertEquals("Feign Client Service unavailable: 504, Reason : Gateway Timeout", e.getMessage());
        }
        WIREMOCK_SERVER.verify(3, postRequestedFor(urlPathMatching(DELETE_TASKS_PATH)));
    }

    @Test
    void testHearingDeleteRetry() {
        List<String> request = new ArrayList<>();
        request.add("3456");

        try {
            hearingClient.deleteHearing("authHeader", "serviceAuthHeader", request);
        } catch (Exception e) {
            Assertions.assertEquals("Feign Client Service unavailable: 502, Reason : Bad Gateway", e.getMessage());
        }
        WIREMOCK_SERVER.verify(3, deleteRequestedFor(urlPathMatching(DELETE_HEARINGS_PATH)));
    }
}

