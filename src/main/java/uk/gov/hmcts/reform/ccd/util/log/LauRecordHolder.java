package uk.gov.hmcts.reform.ccd.util.log;


import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;

@Named
@Getter
public class LauRecordHolder {
    private List<String> lauCaseRefList = new ArrayList<>();

    public void addLauCaseRef(final String caseRef) {
        lauCaseRefList.add(caseRef);
    }
}
