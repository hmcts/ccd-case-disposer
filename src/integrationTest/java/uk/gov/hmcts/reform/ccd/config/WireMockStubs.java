package uk.gov.hmcts.reform.ccd.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.google.gson.Gson;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.data.am.QueryResponse;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostRequest;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostResponse;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.data.tm.DeleteCaseTasksAction;
import uk.gov.hmcts.reform.ccd.data.tm.DeleteTasksRequest;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static feign.form.ContentProcessor.CONTENT_TYPE_HEADER;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.DOCUMENT_DELETE;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.HEARINGS_DELETE;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.LAU_QUERY;
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
        ResponseDefinitionBuilder response = aResponse()
            .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
            .withStatus(201);

        String body = new Gson().toJson(new DeleteTasksRequest(new DeleteCaseTasksAction("${json-unit.any-string}")));
        wireMockServer.stubFor(post(urlPathMatching(TASKS_DELETE_PATH))
            .withRequestBody(equalToJson(body))
            .willReturn(response));

        TASKS_DELETE.forEach((key, value) ->
                wireMockServer.stubFor(post(urlPathMatching(TASKS_DELETE_PATH))
                        .withRequestBody(
                            equalToJson(new Gson().toJson(new DeleteTasksRequest(new DeleteCaseTasksAction(key))))
                        )
                        .willReturn(aResponse()
                            .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                            .withStatus(value))));
    }

    private void setupLauStub(final WireMockServer wireMockServer) {
        LAU_QUERY.entrySet().forEach(entry ->
                wireMockServer.stubFor(post(urlPathMatching(LAU_SAVE_PATH))
                        .withRequestBody(containing(entry.getKey()))
                        .willReturn(aResponse()
                                .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                .withBody(new Gson()
                                        .toJson(entry.getValue()))
                                .withStatus(201))));
    }

    private void setupDeleteDocumentsStub(final WireMockServer wireMockServer) {
        DOCUMENT_DELETE.entrySet().forEach(entry ->
                wireMockServer.stubFor(post(urlPathMatching(DOCUMENTS_DELETE_PATH))
                        .withRequestBody(equalToJson(new Gson()
                                .toJson(new DocumentsDeletePostRequest(entry.getKey()))))
                        .willReturn(aResponse()
                                .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                .withBody(new Gson()
                                        .toJson(entry.getValue()))
                                .withStatus(200))));
    }

    private void setupDeleteRolesStub(final WireMockServer wireMockServer) {
        ROLE_DELETE.entrySet().forEach(entry ->
                wireMockServer.stubFor(post(urlPathMatching(ROLES_DELETE_PATH))
                        .withRequestBody(equalToJson(new Gson()
                                .toJson(new RoleAssignmentsPostRequest(entry.getKey()))))
                        .willReturn(aResponse()
                                .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                .withStatus(entry.getValue()))));
    }

    private void setupQueryRolesStub(final WireMockServer wireMockServer) {
        ROLE_QUERY.entrySet().forEach(entry ->
               wireMockServer.stubFor(post(urlPathMatching(ROLES_QUERY_PATH))
                          .withRequestBody(equalToJson(new Gson()
                          .toJson(new RoleAssignmentsPostRequest(entry.getKey()))))
                          .willReturn(aResponse()
                                          .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                          .withBody(new Gson()
                                                        .toJson(roleAssignmentsResponse))
                                          .withStatus(entry.getValue()))));
    }

    private void setupHearingsStub(final WireMockServer wireMockServer) {
        HEARINGS_DELETE.entrySet().forEach(entry ->
               wireMockServer.stubFor(delete(urlPathMatching(HEARINGS_DELETE_PATH))
                                          .withRequestBody(equalToJson("[\"" + entry.getKey() + "\"]"))
                                          .willReturn(aResponse().withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                                          .withBody(new Gson().toJson(entry.getValue()))
                                                          .withStatus(entry.getValue()))));
    }


}
