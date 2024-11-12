package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.DocumentDeletionException;
import uk.gov.hmcts.reform.ccd.exception.HearingDeletionException;
import uk.gov.hmcts.reform.ccd.service.remote.clients.DocumentClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.DocumentDeletionRecordHolder;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.HEARING_RECORDINGS_CASE_TYPE;

@Service
@Slf4j
public class DisposeDocumentsRemoteOperation implements DisposeRemoteOperation {

    private final DocumentClient documentClient;
    private DocumentDeletionRecordHolder documentDeletionRecordHolder;
    private final SecurityUtil securityUtil;

    @Autowired
    public DisposeDocumentsRemoteOperation(final DocumentClient documentClient,
                                           final SecurityUtil securityUtil,
                                           final DocumentDeletionRecordHolder documentDeletionRecordHolder) {
        this.documentClient = documentClient;
        this.securityUtil = securityUtil;
        this.documentDeletionRecordHolder = documentDeletionRecordHolder;
    }


    @Override
    public void delete(final CaseData caseData) {
        if (!caseData.getCaseType().equals(HEARING_RECORDINGS_CASE_TYPE)) {
            try {
                final DocumentsDeletePostRequest documentsDeleteRequest =
                    new DocumentsDeletePostRequest(caseData.getReference().toString());

                final ResponseEntity<CaseDocumentsDeletionResults> documentsDeleteResponse = postDocument(documentsDeleteRequest);
                if (!documentsDeleteResponse.getStatusCode().is2xxSuccessful()){
                    final String errorMessage = String
                        .format("Unexpected response code %d while deleting documents for case: %s",
                                documentsDeleteResponse.getStatusCode(), caseData.getReference());

                    throw new HearingDeletionException(errorMessage);
                }

                logDocumentsDisposal(documentsDeleteRequest, documentsDeleteResponse.getBody());

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
                message + "Case Ref = {} - Documents found = {} - Documents marked for deletion = {}",
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


    ResponseEntity<CaseDocumentsDeletionResults> postDocument(final DocumentsDeletePostRequest documentsDeletePostRequest) {
        return documentClient
            .deleteDocument(securityUtil.getServiceAuthorization(), documentsDeletePostRequest);
    }
}
