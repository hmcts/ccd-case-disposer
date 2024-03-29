package uk.gov.hmcts.reform.ccd.utils;


import jakarta.inject.Inject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.util.log.DocumentDeletionRecordHolder;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.DOCUMENT_DELETE;

@Component
public class DocumentRemoteDeletionVerifier implements RemoteDeletionVerifier {

    @Inject
    private DocumentDeletionRecordHolder documentDeletionRecordHolder;

    public void verifyRemoteDeletion(final List<Long> caseRefDeletedDocuments) {
        caseRefDeletedDocuments.forEach(caseRef -> {
            final CaseDocumentsDeletionResults caseDocumentsDeletionMocks =
                    DOCUMENT_DELETE.get(Long.toString(caseRef));

            final CaseDocumentsDeletionResults caseDocumentsDeletionActualResults = documentDeletionRecordHolder
                    .getCaseDocumentsDeletionResults(Long.toString(caseRef));

            assertThat(caseDocumentsDeletionActualResults).isNotNull();
            assertThat(caseDocumentsDeletionActualResults.getCaseDocumentsFound())
                    .isEqualTo(caseDocumentsDeletionMocks.getCaseDocumentsFound());
            assertThat(caseDocumentsDeletionActualResults.getMarkedForDeletion())
                    .isEqualTo(caseDocumentsDeletionMocks.getMarkedForDeletion());

        });
    }
}
