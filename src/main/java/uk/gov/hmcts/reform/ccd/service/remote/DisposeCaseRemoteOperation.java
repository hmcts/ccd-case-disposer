package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.Gson;
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
import uk.gov.hmcts.reform.ccd.util.log.DocumentDeletionRecordHolder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_DOCUMENT_PATH;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.SERVICE_AUTHORISATION_HEADER;

@Service
@Slf4j
@Qualifier("DisposeCaseRemoteOperation")
public class DisposeCaseRemoteOperation {

    private final SecurityUtil securityUtil;

    private final HttpClient httpClient;

    private final ParameterResolver parameterResolver;

    private DocumentDeletionRecordHolder documentDeletionRecordHolder;
    private final Gson gson = new Gson();


    @Autowired
    public DisposeCaseRemoteOperation(@Lazy final SecurityUtil securityUtil,
                                      @Qualifier("httpClientDispose") final HttpClient httpClient,
                                      final ParameterResolver parameterResolver,
                                      final DocumentDeletionRecordHolder documentDeletionRecordHolder) {
        this.securityUtil = securityUtil;
        this.httpClient = httpClient;
        this.parameterResolver = parameterResolver;
        this.documentDeletionRecordHolder = documentDeletionRecordHolder;
    }

    public void postDocumentsDelete(final String caseRef) {
        try {
            final String dmCaseDocumentsDeleteUrl = parameterResolver.getDocumentStoreHost() + DELETE_DOCUMENT_PATH;

            final DocumentsDeletePostRequest documentsDeleteRequest = new DocumentsDeletePostRequest(caseRef);

            final String requestBody = gson.toJson(documentsDeleteRequest);

            final HttpResponse<String> documentsDeleteResponse = postDisposeRequest(dmCaseDocumentsDeleteUrl,
                    requestBody);

            logDocumentsDisposal(documentsDeleteRequest, documentsDeleteResponse);

        } catch (final Exception ex) {
            final String errorMessage = String.format("Error deleting documents for case : %s", caseRef);
            log.error(errorMessage, ex);
            Thread.currentThread().interrupt();
            throw new DocumentDeletionException(errorMessage, ex);
        }
    }

    private HttpResponse<String> postDisposeRequest(final String url, final String body) throws IOException,
            InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header(SERVICE_AUTHORISATION_HEADER, securityUtil.getServiceAuthorization())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    }

    private void logDocumentsDisposal(final DocumentsDeletePostRequest documentsDeleteRequest,
                                      final HttpResponse<String> documentsDeleteResponse) {

        final CaseDocumentsDeletionResults documentsDeletionResults =
                gson.fromJson(documentsDeleteResponse.body(), CaseDocumentsDeletionResults.class);

        documentDeletionRecordHolder.setCaseDocumentsDeletionResults(documentsDeleteRequest.getCaseRef(),
                documentsDeletionResults);

        final String message = getLogMessage(documentsDeletionResults);

        log.info(message + "Case Ref = {} - Documents found = {} - Documented Marked for deletion = {}",
                documentsDeleteRequest.getCaseRef(),
                documentsDeletionResults.getCaseDocumentsFound(),
                documentsDeletionResults.getMarkedForDeletion());
    }

    private String getLogMessage(final CaseDocumentsDeletionResults documentsDeletionResults) {
        if (!documentsDeletionResults.getCaseDocumentsFound()
                .equals(documentsDeletionResults.getMarkedForDeletion())) {
            return "Case Documents Deletion ANOMALY: ";
        }
        return "Case Documents Deletion CONFIRMATION: ";
    }
}
