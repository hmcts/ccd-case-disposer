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
        // HRS cases don't have any documents
        if (caseData.getCaseType().equals(parameterResolver.getHearingCaseType())) {
            return;
        }

        final String caseRef = caseData.getReference().toString();
        final DocumentsDeletePostRequest documentsDeleteRequest = new DocumentsDeletePostRequest(caseRef);
        final ResponseEntity<CaseDocumentsDeletionResults> documentsDeleteResponse;

        try {
            documentsDeleteResponse = postDocument(documentsDeleteRequest);
        } catch (final Exception ex) {
            log.error("Error deleting documents for case : {}", caseRef, ex);
            throw new DocumentDeletionException(caseRef, ex);
        }

        logDocumentsDisposal(caseRef, documentsDeleteResponse.getBody());

        if (!documentsDeleteResponse.getStatusCode().is2xxSuccessful()) {
            throw new DocumentDeletionException(documentsDeleteResponse.getStatusCode().value(), caseRef);
        }
    }

    private void logDocumentsDisposal(final String caseRef, final CaseDocumentsDeletionResults deletionResults) {
        try {
            documentDeletionRecordHolder.setCaseDocumentsDeletionResults(caseRef, deletionResults);

            final String message = getLogMessage(deletionResults);

            log.info(
                "{}Case Ref = {} - Documents found = {} - Documents marked for deletion = {}",
                message,
                caseRef,
                deletionResults.getCaseDocumentsFound(),
                deletionResults.getMarkedForDeletion()
            );

        } catch (final JsonParseException jsonParseException) {
            final String errorMessage = "Unable to map json to object document deletion endpoint response due"
                + " to following endpoint response: ".concat(deletionResults.toString());
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

    private ResponseEntity<CaseDocumentsDeletionResults> postDocument(
        final DocumentsDeletePostRequest documentsDeletePostRequest
    ) {
        return documentClient
            .deleteDocument(securityUtil.getServiceAuthorization(), documentsDeletePostRequest);
    }
}
