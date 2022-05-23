package uk.gov.hmcts.reform.ccd.util.log;

import lombok.Getter;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Named;

@Named
@Getter
public class DocumentDeletionRecordHolder {
    private List<Map<String, CaseDocumentsDeletionResults>> documentDeleteRecordHolderList = new ArrayList<>();

    public void setCaseDocumentsDeletionResults(final String caseRef,
                                                final CaseDocumentsDeletionResults caseDocumentsDeletionResults) {
        documentDeleteRecordHolderList.add(Map.of(caseRef, caseDocumentsDeletionResults));
    }

    public CaseDocumentsDeletionResults getCaseDocumentsDeletionResults(final String caseRef) {
        if (!documentDeleteRecordHolderList.isEmpty()) {
            final Optional<Map<String, CaseDocumentsDeletionResults>> deletionResultsMap =
                    documentDeleteRecordHolderList.stream()
                    .filter(documentHolderEntry -> documentHolderEntry.containsKey(caseRef))
                    .findFirst();
            if (deletionResultsMap.isPresent()) {
                return deletionResultsMap.get().get(caseRef);
            }
        }
        return null;
    }
}
