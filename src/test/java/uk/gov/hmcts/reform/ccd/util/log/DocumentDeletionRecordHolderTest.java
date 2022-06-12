package uk.gov.hmcts.reform.ccd.util.log;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DocumentDeletionRecordHolderTest {

    @Test
    void shouldHoldCaseDocumentsDeletionResults() {
        final DocumentDeletionRecordHolder documentDeletionRecordHolder = new DocumentDeletionRecordHolder();

        final CaseDocumentsDeletionResults caseDocumentsDeletionBeforeCaseRefMapping_1 =
                new CaseDocumentsDeletionResults(1, 2);
        final CaseDocumentsDeletionResults caseDocumentsDeletionBeforeCaseRefMapping_2 =
                new CaseDocumentsDeletionResults(3, 4);

        documentDeletionRecordHolder.setCaseDocumentsDeletionResults("123",
                caseDocumentsDeletionBeforeCaseRefMapping_1);
        documentDeletionRecordHolder.setCaseDocumentsDeletionResults("456",
                caseDocumentsDeletionBeforeCaseRefMapping_2);

        final CaseDocumentsDeletionResults caseDocumentsDeletionResults_1 =
                documentDeletionRecordHolder.getCaseDocumentsDeletionResults("123");
        final CaseDocumentsDeletionResults caseDocumentsDeletionResults_2 =
                documentDeletionRecordHolder.getCaseDocumentsDeletionResults("456");

        assertThat(caseDocumentsDeletionBeforeCaseRefMapping_1.getCaseDocumentsFound())
                .isEqualTo(caseDocumentsDeletionResults_1.getCaseDocumentsFound());
        assertThat(caseDocumentsDeletionBeforeCaseRefMapping_1.getMarkedForDeletion())
                .isEqualTo(caseDocumentsDeletionResults_1.getMarkedForDeletion());

        assertThat(caseDocumentsDeletionBeforeCaseRefMapping_2.getCaseDocumentsFound())
                .isEqualTo(caseDocumentsDeletionResults_2.getCaseDocumentsFound());
        assertThat(caseDocumentsDeletionBeforeCaseRefMapping_2.getMarkedForDeletion())
                .isEqualTo(caseDocumentsDeletionResults_2.getMarkedForDeletion());
    }
}