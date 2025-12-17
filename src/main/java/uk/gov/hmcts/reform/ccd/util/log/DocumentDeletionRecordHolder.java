package uk.gov.hmcts.reform.ccd.util.log;

import jakarta.inject.Named;
import lombok.Getter;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Named
@Getter
public class DocumentDeletionRecordHolder {

    private final ConcurrentMap<String, CaseDocumentsDeletionResults>
        documentDeletionResults = new ConcurrentHashMap<>();

    public void setCaseDocumentsDeletionResults(
        final String caseRef,
        final CaseDocumentsDeletionResults caseDocumentsDeletionResults) {
        documentDeletionResults.put(caseRef, caseDocumentsDeletionResults);
    }

    public CaseDocumentsDeletionResults getCaseDocumentsDeletionResults(
        final String caseRef) {
        return documentDeletionResults.get(caseRef);
    }

    public Map<String, CaseDocumentsDeletionResults> snapshot() {
        return Map.copyOf(documentDeletionResults);
    }

    public void clear() {
        documentDeletionResults.clear();
    }
}
