package uk.gov.hmcts.reform.ccd.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.google.gson.Gson;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.data.am.QueryResponse;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostRequest;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostResponse;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.data.lau.ActionLog;
import uk.gov.hmcts.reform.ccd.data.lau.CaseActionPostRequestResponse;
import uk.gov.hmcts.reform.ccd.data.tm.DeleteCaseTasksAction;
import uk.gov.hmcts.reform.ccd.data.tm.DeleteTasksRequest;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static feign.form.ContentProcessor.CONTENT_TYPE_HEADER;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.DOCUMENT_DELETE;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.HEARINGS_DELETE;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.LAU_SAVE;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.ROLE_DELETE;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.ROLE_QUERY;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.TASKS_DELETE;

@Configuration
public class WireMockStubs {

    private static final String JSON_RESPONSE = "application/json;charset=UTF-8";

    private static final String DOCUMENTS_DELETE_PATH = "/documents/delete";
    private static final String ROLES_DELETE_PATH = "/am/role-assignments/query/delete";
    private static final String ROLES_QUERY_PATH = "/am/role-assignments/query";
    private static final String LAU_SAVE_PATH = "/audit/caseAction";
    private static final String TASKS_DELETE_PATH = "/task/delete";
    private static final String HEARINGS_DELETE_PATH = "/delete";


    private RoleAssignmentsPostResponse roleAssignmentsResponse = new RoleAssignmentsPostResponse();

    public void setUpStubs(final WireMockServer wireMockServer) {
        roleAssignmentsResponse.setRoleAssignmentResponse(Collections.singletonList(new QueryResponse()));
        setupDeleteDocumentsStub(wireMockServer);
        setupDeleteRolesStub(wireMockServer);
        setupQueryRolesStub(wireMockServer);
        setupLauStub(wireMockServer);
        setupTasksStub(wireMockServer);
        setupHearingsStub(wireMockServer);
    }

    private void setupTasksStub(final WireMockServer wireMockServer) {
        String body = new Gson().toJson(new DeleteTasksRequest(new DeleteCaseTasksAction("${json-unit.any-string}")));
        wireMockServer.stubFor(post(urlPathMatching(TASKS_DELETE_PATH))
            .withRequestBody(equalToJson(body))
            .willReturn(buildResponseDefinition(201)));

        TASKS_DELETE.forEach((key, value) ->
                wireMockServer.stubFor(post(urlPathMatching(TASKS_DELETE_PATH))
                        .withRequestBody(
                            equalToJson(new Gson().toJson(new DeleteTasksRequest(new DeleteCaseTasksAction(key))))
                        )
                        .willReturn(buildResponseDefinition(value))));
    }

    private void setupLauStub(final WireMockServer wireMockServer) {
        ActionLog actionLogExample = ActionLog.builder()
            .caseAction("DELETE")
            .caseRef("${json-unit.any-string}")
            .caseJurisdictionId("BEFTA_MASTER")
            .build();
        String body = new Gson().toJson(new CaseActionPostRequestResponse(actionLogExample));

        // Return request body in response
        wireMockServer.stubFor(post(urlPathMatching(LAU_SAVE_PATH))
            .withRequestBody(equalToJson(body, true, true))
            .willReturn(
                buildResponseDefinition(201, "{{request.body}}")
                    .withTransformers("response-template")
            ));


        LAU_SAVE.forEach((key, value) -> {
            actionLogExample.setCaseRef(key);
            String specificBody = new Gson().toJson(new CaseActionPostRequestResponse(actionLogExample));

            wireMockServer.stubFor(
                post(urlPathMatching(LAU_SAVE_PATH))
                    .withRequestBody(equalToJson(specificBody, true, true))
                    .willReturn(buildResponseDefinition(value)));
        });
    }

    private void setupDeleteDocumentsStub(final WireMockServer wireMockServer) {
        String body = new Gson().toJson(new DocumentsDeletePostRequest("${json-unit.any-string}"));
        wireMockServer.stubFor(post(urlPathMatching(DOCUMENTS_DELETE_PATH))
            .withRequestBody(equalToJson(body))
            .willReturn(buildResponseDefinition(
                200,
                new Gson().toJson(new CaseDocumentsDeletionResults(8, 8)))));

        DOCUMENT_DELETE.forEach((key, value) ->
                wireMockServer.stubFor(post(urlPathMatching(DOCUMENTS_DELETE_PATH))
                        .withRequestBody(equalToJson(new Gson().toJson(new DocumentsDeletePostRequest(key))))
                        .willReturn(buildResponseDefinition(200, new Gson().toJson(value)))));
    }

    private void setupDeleteRolesStub(final WireMockServer wireMockServer) {
        String body = new Gson().toJson(new RoleAssignmentsPostRequest("${json-unit.any-string}"));
        wireMockServer.stubFor(post(urlPathMatching(ROLES_DELETE_PATH))
            .withRequestBody(equalToJson(body))
            .willReturn(buildResponseDefinition(200)));

        ROLE_DELETE.forEach((key, value) ->
               wireMockServer.stubFor(post(urlPathMatching(ROLES_DELETE_PATH))
                      .withRequestBody(equalToJson(new Gson().toJson(new RoleAssignmentsPostRequest(key))))
                      .willReturn(buildResponseDefinition(value))));
    }

    private void setupQueryRolesStub(final WireMockServer wireMockServer) {
        String body = new Gson().toJson(new RoleAssignmentsPostRequest("${json-unit.any-string}"));
        wireMockServer.stubFor(post(urlPathMatching(ROLES_QUERY_PATH))
            .withRequestBody(equalToJson(body))
            .willReturn(buildResponseDefinition(200)));

        ROLE_QUERY.forEach((key, value) ->
                 wireMockServer.stubFor(post(urlPathMatching(ROLES_QUERY_PATH))
                         .withRequestBody(equalToJson(new Gson().toJson(new RoleAssignmentsPostRequest(key))))
                         .willReturn(buildResponseDefinition(value))));
    }

    private void setupHearingsStub(final WireMockServer wireMockServer) {
        String body = "[\"${json-unit.any-string}\"]";
        wireMockServer.stubFor(delete(urlPathMatching(HEARINGS_DELETE_PATH))
            .withRequestBody(equalToJson(body))
            .willReturn(buildResponseDefinition(204)));

        HEARINGS_DELETE.forEach((key, value) ->
               wireMockServer.stubFor(delete(urlPathMatching(HEARINGS_DELETE_PATH))
                          .withRequestBody(equalToJson("[\"" + key + "\"]"))
                          .willReturn(buildResponseDefinition(value))));
    }

    private ResponseDefinitionBuilder buildResponseDefinition(int status) {
        return buildResponseDefinition(status, null);
    }

    private ResponseDefinitionBuilder buildResponseDefinition(int status, String body) {
        return aResponse()
            .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
            .withBody(body)
            .withStatus(status);
    }


}
