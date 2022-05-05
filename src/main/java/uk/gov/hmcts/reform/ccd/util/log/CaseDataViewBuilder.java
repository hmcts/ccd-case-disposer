package uk.gov.hmcts.reform.ccd.util.log;

import lombok.Getter;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.util.List;
import javax.inject.Named;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_STATE;

@Named
@Getter
public class CaseDataViewBuilder {

    public void buildCaseDataViewList(final List<CaseFamily> caseFamilies,
                                      final List<CaseDataView> caseDataViews,
                                      final boolean isDeletable) {
        caseFamilies.forEach(family -> {
            final List<Long> linkedCaseIds = family.getLinkedCases().stream()
                    .map(value -> value.getId())
                    .collect(toList());
            //Add the root case
            caseDataViews.add(new CaseDataView(family.getRootCase().getCaseType(),
                    family.getRootCase().getId(),
                    isDeletable ? DELETED_STATE : SIMULATED_STATE,
                    linkedCaseIds));
            //Add linked cases
            family.getLinkedCases().forEach(linkedCase -> caseDataViews.add(new CaseDataView(linkedCase.getCaseType(),
                    linkedCase.getId(),
                    isDeletable ? DELETED_STATE : SIMULATED_STATE,
                    emptyList())));
        });
    }
}
