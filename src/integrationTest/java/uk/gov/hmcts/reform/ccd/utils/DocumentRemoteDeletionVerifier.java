package uk.gov.hmcts.reform.ccd.utils;


import jakarta.inject.Inject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.util.log.DocumentDeletionRecordHolder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.DOCUMENT_DELETE;

@Component
public class DocumentRemoteDeletionVerifier implements
    RemoteDeletionVerifier<Map<String, CaseDocumentsDeletionResults>> {

    @Inject
    private DocumentDeletionRecordHolder documentDeletionRecordHolder;

    @Override
    public Map<String, CaseDocumentsDeletionResults> snapshot() {
        return documentDeletionRecordHolder.snapshot();
    }

    @Override
    public void clear() {
        documentDeletionRecordHolder.clear();
    }

    public void assertDeletionResults(Map<String, CaseDocumentsDeletionResults> snapshot,
        List<Long> caseRefDeletedDocuments) {

        caseRefDeletedDocuments.forEach(caseRef -> {
            String caseRefStr = caseRef.toString();
            CaseDocumentsDeletionResults expected = DOCUMENT_DELETE.get(caseRefStr);
            if (expected != null) {
                CaseDocumentsDeletionResults actual = snapshot.get(caseRefStr);
                assertThat(actual)
                    .as("Document deletion results missing for caseRef %s", caseRefStr)
                    .isNotNull();

                assertThat(actual.getCaseDocumentsFound())
                    .as("caseDocumentsFound mismatch for caseRef %s", caseRefStr)
                    .isEqualTo(expected.getCaseDocumentsFound());

                assertThat(actual.getMarkedForDeletion())
                    .as("markedForDeletion mismatch for caseRef %s", caseRefStr)
                    .isEqualTo(expected.getMarkedForDeletion());
            }
        });
    }
}
