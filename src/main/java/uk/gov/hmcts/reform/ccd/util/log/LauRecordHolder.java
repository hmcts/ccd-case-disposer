package uk.gov.hmcts.reform.ccd.util.log;


import jakarta.inject.Named;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Named
@Getter
public class LauRecordHolder {
    private List<String> lauCaseRefList = new ArrayList<>();

    public void addLauCaseRef(final String caseRef) {
        lauCaseRefList.add(caseRef);
    }
}
