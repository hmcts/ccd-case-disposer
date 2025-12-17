package uk.gov.hmcts.reform.ccd.util.log;


import jakarta.inject.Named;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Named
public class LauRecordHolder {
    private final Set<String> lauCaseRefList = ConcurrentHashMap.newKeySet();

    public void addLauCaseRef(final String caseRef) {
        lauCaseRefList.add(caseRef);
    }

    public Set<String> getLauCaseRefList() {
        return Set.copyOf(lauCaseRefList);
    }

    public Set<String> snapshot() {
        return Set.copyOf(lauCaseRefList);
    }

    public void clear() {
        lauCaseRefList.clear();
    }
}
