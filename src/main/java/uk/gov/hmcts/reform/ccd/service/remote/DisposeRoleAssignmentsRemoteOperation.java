package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.exception.RoleAssignmentDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.RoleDeletionRecordHolder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_ROLE_PATH;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.SERVICE_AUTHORISATION_HEADER;

@Service
@Slf4j
@Qualifier("DisposeRoleAssignmentsRemoteOperation")
public class DisposeRoleAssignmentsRemoteOperation {

    private static final Logger logger = LoggerFactory.getLogger(DisposeDocumentsRemoteOperation.class);

    private final SecurityUtil securityUtil;

    private final HttpClient httpClient;

    private final ParameterResolver parameterResolver;

    private final Gson gson = new Gson();

    private RoleDeletionRecordHolder roleDeletionRecordHolder;

    @Autowired
    public DisposeRoleAssignmentsRemoteOperation(@Lazy final SecurityUtil securityUtil,
                                                 @Qualifier("httpClientDispose") final HttpClient httpClient,
                                                 final ParameterResolver parameterResolver,
                                                 final RoleDeletionRecordHolder roleDeletionRecordHolder) {
        this.securityUtil = securityUtil;
        this.httpClient = httpClient;
        this.parameterResolver = parameterResolver;
        this.roleDeletionRecordHolder = roleDeletionRecordHolder;
    }

    public void postRoleAssignmentsDelete(String caseRef) {

        try {
            logger.info("Inside the Dispose Documents Remote Operation method");

            String amCaseRoleAssignmentsDeleteUrl = parameterResolver.getRoleAssignmentsHost() + DELETE_ROLE_PATH;

            RoleAssignmentsDeletePostRequest roleAssignmentsDeleteRequest =
                new RoleAssignmentsDeletePostRequest(caseRef);

            String requestBody = gson.toJson(roleAssignmentsDeleteRequest);

            HttpResponse<String> roleAssignmentsDeleteResponse =
                postDisposeRequest(amCaseRoleAssignmentsDeleteUrl, requestBody);

            logRoleAssignmentsDisposal(caseRef, roleAssignmentsDeleteResponse);

        } catch (Exception ex) {
            final String errorMessage = String.format("Error deleting role assignments for case : %s", caseRef);
            log.error(errorMessage, ex);
            Thread.currentThread().interrupt();
            throw new RoleAssignmentDeletionException(errorMessage, ex);
        }

    }

    private HttpResponse<String> postDisposeRequest(String url, String body) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORISATION_HEADER, securityUtil.getServiceAuthorization())
            .header(AUTHORISATION_HEADER, securityUtil.getIdamClientToken())
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    }

    private void logRoleAssignmentsDisposal(String caseRef, HttpResponse<String> roleAssignmentsDeleteResponse) {

        roleDeletionRecordHolder.setCaseRolesDeletionResults(caseRef, roleAssignmentsDeleteResponse.statusCode());

    }

}
