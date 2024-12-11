package uk.gov.hmcts.reform.ccd.util.log;

import jakarta.inject.Named;
import lombok.Getter;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Named
@Getter
public class CaseDataViewBuilder {

    public void buildCaseDataViewList(final List<CaseData> caseDataList,
                                      final List<CaseDataView> caseDataViews,
                                      final String state) {
        caseDataList.forEach(family -> {
            //Add the root case
            caseDataViews.add(new CaseDataView(family.getCaseType(),
                    family.getReference(),
                    state, null));

        });
    }
}
