package uk.gov.hmcts.reform.ccd.util.log;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DocumentDeletionRecordHolderTest {

    @Test
    void shouldHoldCaseDocumentsDeletionResults() {
        final DocumentDeletionRecordHolder documentDeletionRecordHolder = new DocumentDeletionRecordHolder();

        final CaseDocumentsDeletionResults caseDocumentsDeletionBeforeCaseRefMapping1 =
                new CaseDocumentsDeletionResults(1, 2);
        final CaseDocumentsDeletionResults caseDocumentsDeletionBeforeCaseRefMapping2 =
                new CaseDocumentsDeletionResults(3, 4);

        documentDeletionRecordHolder.setCaseDocumentsDeletionResults("123",
                caseDocumentsDeletionBeforeCaseRefMapping1);
        documentDeletionRecordHolder.setCaseDocumentsDeletionResults("456",
                caseDocumentsDeletionBeforeCaseRefMapping2);

        final CaseDocumentsDeletionResults caseDocumentsDeletionResults1 =
                documentDeletionRecordHolder.getCaseDocumentsDeletionResults("123");
        final CaseDocumentsDeletionResults caseDocumentsDeletionResults2 =
                documentDeletionRecordHolder.getCaseDocumentsDeletionResults("456");

        assertThat(caseDocumentsDeletionBeforeCaseRefMapping1.getCaseDocumentsFound())
                .isEqualTo(caseDocumentsDeletionResults1.getCaseDocumentsFound());
        assertThat(caseDocumentsDeletionBeforeCaseRefMapping1.getMarkedForDeletion())
                .isEqualTo(caseDocumentsDeletionResults1.getMarkedForDeletion());

        assertThat(caseDocumentsDeletionBeforeCaseRefMapping2.getCaseDocumentsFound())
                .isEqualTo(caseDocumentsDeletionResults2.getCaseDocumentsFound());
        assertThat(caseDocumentsDeletionBeforeCaseRefMapping2.getMarkedForDeletion())
                .isEqualTo(caseDocumentsDeletionResults2.getMarkedForDeletion());
    }
}
