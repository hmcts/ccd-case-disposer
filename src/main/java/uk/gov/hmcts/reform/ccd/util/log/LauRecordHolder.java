package uk.gov.hmcts.reform.ccd.util.log;


import jakarta.inject.Named;
import lombok.Getter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Named
@Getter
public class LauRecordHolder {
    private Set<String> lauCaseRefList = ConcurrentHashMap.newKeySet();

    public void addLauCaseRef(final String caseRef) {
        lauCaseRefList.add(caseRef);
    }
}
