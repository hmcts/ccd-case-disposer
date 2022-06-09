package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.exception.DocumentDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.DocumentDeletionRecordHolder;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_DOCUMENT_PATH;

@Service
@Slf4j
@Qualifier("DisposeCaseRemoteOperation")
public class DisposeCaseRemoteOperation {

    private final ParameterResolver parameterResolver;

    private final RestClientBuilder restClientBuilder;
    private final Gson gson = new Gson();
    private DocumentDeletionRecordHolder documentDeletionRecordHolder;

    @Autowired
    public DisposeCaseRemoteOperation(final RestClientBuilder restClientBuilder,
                                      final ParameterResolver parameterResolver,
                                      final DocumentDeletionRecordHolder documentDeletionRecordHolder) {
        this.restClientBuilder = restClientBuilder;
        this.parameterResolver = parameterResolver;
        this.documentDeletionRecordHolder = documentDeletionRecordHolder;
    }

    public void postDocumentsDelete(final String caseRef) {
        try {
            final String dmCaseDocumentsDeleteUrl = parameterResolver.getDocumentStoreHost() + DELETE_DOCUMENT_PATH;

            final DocumentsDeletePostRequest documentsDeleteRequest = new DocumentsDeletePostRequest(caseRef);

            final String requestBody = gson.toJson(documentsDeleteRequest);

            final String documentsDeleteResponse = restClientBuilder
                    .postRequest(parameterResolver.getDocumentStoreHost(),
                            DELETE_DOCUMENT_PATH,
                            requestBody);

            logDocumentsDisposal(documentsDeleteRequest, documentsDeleteResponse);

        } catch (final Exception ex) {
            final String errorMessage = String.format("Error deleting documents for case : %s", caseRef);
            log.error(errorMessage, ex);
            Thread.currentThread().interrupt();
            throw new DocumentDeletionException(errorMessage, ex);
        }
    }


    private void logDocumentsDisposal(final DocumentsDeletePostRequest documentsDeleteRequest,
                                      final String documentsDeleteResponse) {

        final CaseDocumentsDeletionResults documentsDeletionResults =
                gson.fromJson(documentsDeleteResponse, CaseDocumentsDeletionResults.class);

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
