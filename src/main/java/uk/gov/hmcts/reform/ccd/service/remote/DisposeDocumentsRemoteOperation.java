package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.JsonParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.DocumentDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.remote.clients.DocumentClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.DocumentDeletionRecordHolder;


@Service
@Slf4j
@RequiredArgsConstructor
public class DisposeDocumentsRemoteOperation implements DisposeRemoteOperation {

    private final DocumentClient documentClient;
    private final DocumentDeletionRecordHolder documentDeletionRecordHolder;
    private final SecurityUtil securityUtil;
    private final ParameterResolver parameterResolver;

    @Override
    public void delete(final CaseData caseData) {
        if (!caseData.getCaseType().equals(parameterResolver.getHearingCaseType())) {
            try {
                final DocumentsDeletePostRequest documentsDeleteRequest =
                    new DocumentsDeletePostRequest(caseData.getReference().toString());

                final ResponseEntity<CaseDocumentsDeletionResults> documentsDeleteResponse =
                    postDocument(documentsDeleteRequest);

                logDocumentsDisposal(documentsDeleteRequest, documentsDeleteResponse.getBody());

                if (!documentsDeleteResponse.getStatusCode().is2xxSuccessful()) {
                    final String errorMessage = String
                        .format("Unexpected response code %d while deleting documents for case: %s",
                                documentsDeleteResponse.getStatusCode().value(), caseData.getReference());

                    throw new DocumentDeletionException(errorMessage);
                }

            } catch (final Exception ex) {
                final String errorMessage = String.format(
                    "Error deleting documents for case : %s", caseData.getReference().toString());
                log.error(errorMessage, ex);
                throw new DocumentDeletionException(errorMessage, ex);
            }
        }
    }


    private void logDocumentsDisposal(final DocumentsDeletePostRequest documentsDeleteRequest,
                                      final CaseDocumentsDeletionResults documentsDeletionResults) {
        try {
            documentDeletionRecordHolder.setCaseDocumentsDeletionResults(
                documentsDeleteRequest.getCaseRef(),
                documentsDeletionResults
            );

            final String message = getLogMessage(documentsDeletionResults);

            log.info(
                "{}Case Ref = {} - Documents found = {} - Documents marked for deletion = {}",
                message,
                documentsDeleteRequest.getCaseRef(),
                documentsDeletionResults.getCaseDocumentsFound(),
                documentsDeletionResults.getMarkedForDeletion()
            );

        } catch (final JsonParseException jsonParseException) {
            final String errorMessage = "Unable to map json to object document deletion endpoint response due"
                + " to following endpoint response: ".concat(documentsDeletionResults.toString());
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


    private ResponseEntity<CaseDocumentsDeletionResults> postDocument(final DocumentsDeletePostRequest
                                                                          documentsDeletePostRequest) {
        return documentClient
            .deleteDocument(securityUtil.getServiceAuthorization(), documentsDeletePostRequest);
    }
}
