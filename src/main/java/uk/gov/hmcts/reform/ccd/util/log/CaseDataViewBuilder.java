package uk.gov.hmcts.reform.ccd.util.log;

import lombok.Getter;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.util.List;
import javax.inject.Named;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Named
@Getter
public class CaseDataViewBuilder {

    public void buildCaseDataViewList(final List<CaseFamily> caseFamilies,
                                      final List<CaseDataView> caseDataViews,
                                      final String state) {
        caseFamilies.forEach(family -> {
            final List<Long> linkedCaseIds = family.getLinkedCases().stream()
                    .map(value -> value.getReference())
                    .collect(toList());
            //Add the root case
            caseDataViews.add(new CaseDataView(family.getRootCase().getCaseType(),
                    family.getRootCase().getReference(),
                    state,
                    linkedCaseIds));
            //Add linked cases
            family.getLinkedCases().forEach(linkedCase -> caseDataViews.add(new CaseDataView(linkedCase.getCaseType(),
                    linkedCase.getReference(),
                    state,
                    emptyList())));
        });
    }
}
