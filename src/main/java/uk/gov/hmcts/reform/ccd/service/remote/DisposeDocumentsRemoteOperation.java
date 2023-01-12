package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.DocumentDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.DocumentDeletionRecordHolder;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_DOCUMENT_PATH;

@Service
@Slf4j
public class DisposeDocumentsRemoteOperation implements DisposeRemoteOperation {

    private final ParameterResolver parameterResolver;

    private final RestClientBuilder restClientBuilder;
    private final Gson gson = new Gson();
    private DocumentDeletionRecordHolder documentDeletionRecordHolder;

    @Autowired
    public DisposeDocumentsRemoteOperation(final RestClientBuilder restClientBuilder,
                                           final ParameterResolver parameterResolver,
                                           final DocumentDeletionRecordHolder documentDeletionRecordHolder) {
        this.restClientBuilder = restClientBuilder;
        this.parameterResolver = parameterResolver;
        this.documentDeletionRecordHolder = documentDeletionRecordHolder;
    }

    @Override
    public void delete(final CaseData caseData) {
        try {
            final DocumentsDeletePostRequest documentsDeleteRequest =
                    new DocumentsDeletePostRequest(caseData.getReference().toString());

            final String requestBody = gson.toJson(documentsDeleteRequest);

            final String documentsDeleteResponse = postDocument(requestBody);

            logDocumentsDisposal(documentsDeleteRequest, documentsDeleteResponse);

        } catch (final Exception ex) {
            final String errorMessage = String.format("Error deleting documents for case : %s",
                    caseData.getReference().toString());
            log.error(errorMessage, ex);
            throw new DocumentDeletionException(errorMessage, ex);
        }
    }


    private void logDocumentsDisposal(final DocumentsDeletePostRequest documentsDeleteRequest,
                                      final String documentsDeleteResponse) {
        try {
            final CaseDocumentsDeletionResults documentsDeletionResults =
                    gson.fromJson(documentsDeleteResponse, CaseDocumentsDeletionResults.class);

            documentDeletionRecordHolder.setCaseDocumentsDeletionResults(documentsDeleteRequest.getCaseRef(),
                    documentsDeletionResults);

            final String message = getLogMessage(documentsDeletionResults);

            log.info(message + "Case Ref = {} - Documents found = {} - Documents marked for deletion = {}",
                    documentsDeleteRequest.getCaseRef(),
                    documentsDeletionResults.getCaseDocumentsFound(),
                    documentsDeletionResults.getMarkedForDeletion());

        } catch (final JsonParseException jsonParseException) {
            final String errorMessage = "Unable to map json to object document deletion endpoint response due"
                    + " to following endpoint response: ".concat(documentsDeleteResponse);
            log.error(errorMessage);
            throw new DocumentDeletionException(errorMessage);
        }

    }

    private String getLogMessage(final CaseDocumentsDeletionResults documentsDeletionResults) {
        if (!documentsDeletionResults.getCaseDocumentsFound()
                .equals(documentsDeletionResults.getMarkedForDeletion())) {
            return "Case Documents Deletion ANOMALY: ";
        }
        return "Case Documents Deletion CONFIRMATION: ";
    }

    @Async
    String postDocument(final String requestBody) {
        return restClientBuilder
                .postRequestWithServiceAuthHeader(parameterResolver.getDocumentStoreHost(),
                        DELETE_DOCUMENT_PATH,
                        requestBody);
    }
}
