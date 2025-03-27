package uk.gov.hmcts.reform.ccd.util.log;

import jakarta.inject.Named;
import lombok.Getter;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;

import java.util.List;

@Named
@Getter
public class CaseDataViewBuilder {

    public void buildCaseDataViewList(final List<CaseData> caseDataList,
                                      final List<CaseDataView> caseDataViews,
                                      final String state) {
        caseDataList.forEach(family ->
            caseDataViews.add(new CaseDataView(family.caseType(), family.reference(), state))
        );
    }
}
