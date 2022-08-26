package uk.gov.hmcts.reform.ccd.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.gson.Gson;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.data.am.QueryResponse;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsRequest;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsResponse;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static feign.form.ContentProcessor.CONTENT_TYPE_HEADER;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.DOCUMENT_DELETE;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.ROLE_DELETE;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.ROLE_QUERY;

@Configuration
public class WireMockStubs {

    private static final String JSON_RESPONSE = "application/json;charset=UTF-8";

    private static final String DOCUMENTS_DELETE_PATH = "/documents/delete";
    private static final String ROLES_DELETE_PATH = "/am/role-assignments/query/delete";
    private static final String ROLES_QUERY_PATH = "/am/role-assignments/query";

    private RoleAssignmentsResponse roleAssignmentsResponse = new RoleAssignmentsResponse();

    public void setUpStubs(final WireMockServer wireMockServer) {
        roleAssignmentsResponse.setRoleAssignmentResponse(Collections.singletonList(new QueryResponse()));
        setupDeleteDocumentsStub(wireMockServer);
        setupDeleteRolesStub(wireMockServer);
        setupQueryRolesStub(wireMockServer);
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
                                .toJson(new RoleAssignmentsRequest(entry.getKey()))))
                        .willReturn(aResponse()
                                .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                .withStatus(200))));
    }

    private void setupQueryRolesStub(final WireMockServer wireMockServer) {
        ROLE_QUERY.entrySet().forEach(entry ->
               wireMockServer.stubFor(post(urlPathMatching(ROLES_QUERY_PATH))
                          .withRequestBody(equalToJson(new Gson()
                          .toJson(new RoleAssignmentsRequest(entry.getKey()))))
                          .willReturn(aResponse()
                                          .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                          .withBody(new Gson()
                                                        .toJson(roleAssignmentsResponse))
                                          .withStatus(200))));
    }
}
