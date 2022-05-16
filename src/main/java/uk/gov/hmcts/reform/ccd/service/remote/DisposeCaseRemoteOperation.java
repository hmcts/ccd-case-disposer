package uk.gov.hmcts.reform.ccd.service.remote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.exception.DocumentDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Slf4j
@Qualifier("DisposeCaseRemoteOperation")
public class DisposeCaseRemoteOperation {

    private final SecurityUtil securityUtil;

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    private final ParameterResolver parameterResolver;

    @Autowired
    public DisposeCaseRemoteOperation(@Lazy final SecurityUtil securityUtil,
                                    @Qualifier("httpClientDispose") final HttpClient httpClient,
                                    @Qualifier("SimpleObjectMapper") final ObjectMapper objectMapper,
                                    final ParameterResolver parameterResolver) {
        this.securityUtil = securityUtil;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.parameterResolver = parameterResolver;
    }

    public void postDocumentsDelete(String caseRef) {

        try {
            String dmCaseDocumentsDeleteUrl = parameterResolver.getDocumentsDeleteUrl();

            DocumentsDeletePostRequest documentsDeleteRequest = new DocumentsDeletePostRequest(caseRef);

            String requestBody = objectMapper.writeValueAsString(documentsDeleteRequest);

            HttpResponse<String> documentsDeleteResponse = postDisposeRequest(dmCaseDocumentsDeleteUrl, requestBody);

            logDocumentsDisposal(documentsDeleteRequest, documentsDeleteResponse);

        } catch (Exception ex) {
            final String errorMessage = String.format("Error deleting documents for case : %s", caseRef);
            log.error(errorMessage, ex);
            Thread.currentThread().interrupt();
            throw new DocumentDeletionException(errorMessage, ex);
        }

    }

    private HttpResponse<String> postDisposeRequest(String url, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("ServiceAuthorization", securityUtil.getServiceAuthorization())
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    }

    private void logDocumentsDisposal(DocumentsDeletePostRequest documentsDeleteRequest,
                                      HttpResponse<String> documentsDeleteResponse) throws JsonProcessingException {

        String message;

        CaseDocumentsDeletionResults documentsDeletionResults =
            objectMapper.readValue(documentsDeleteResponse.body(), CaseDocumentsDeletionResults.class);

        if (documentsDeletionResults.getCaseDocumentsFound() != documentsDeletionResults.getMarkedForDeletion()) {
            message = "Case Documents Deletion ANOMALY: ";
        } else {
            message = "Case Documents Deletion CONFIRMATION: ";
        }

        log.info(message + "Case Ref = {} - Documents found = {} - Documented Marked for deletion = {}",
                 documentsDeleteRequest.getCaseRef(),
                 documentsDeletionResults.getCaseDocumentsFound(),
                 documentsDeletionResults.getMarkedForDeletion());
    }

}
