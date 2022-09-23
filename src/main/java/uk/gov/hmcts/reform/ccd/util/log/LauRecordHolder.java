package uk.gov.hmcts.reform.ccd.util.log;


import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Named;

@Named
@Getter
public class LauRecordHolder {
    private Set<String> lauCaseRefList = new HashSet<>();

    public void addLauCaseRef(final String caseRef) {
        lauCaseRefList.add(caseRef);
    }
}
