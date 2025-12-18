package uk.gov.hmcts.reform.ccd.util.log;

import jakarta.inject.Named;
import lombok.Getter;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Named
@Getter
public class DocumentDeletionRecordHolder {
    private ConcurrentMap<String, CaseDocumentsDeletionResults> documentsDeletionByCaseRef = new ConcurrentHashMap<>();

    public void setCaseDocumentsDeletionResults(String caseRef, CaseDocumentsDeletionResults deletionResult) {
        documentsDeletionByCaseRef.put(caseRef, deletionResult);
    }

    public CaseDocumentsDeletionResults getCaseDocumentsDeletionResults(final String caseRef) {
        return documentsDeletionByCaseRef.getOrDefault(caseRef, null);
    }
}
