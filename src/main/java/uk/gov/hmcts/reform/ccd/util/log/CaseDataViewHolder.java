package uk.gov.hmcts.reform.ccd.util.log;

import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Named;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_STATE;

@Named
public class CaseDataViewHolder {
    private List<CaseDataView> simulatedCaseDataViews;

    public void setUpData(final List<CaseDataView> caseDataViews) {
        this.simulatedCaseDataViews = getCaseCaseDataViewByState(caseDataViews, SIMULATED_STATE);
    }

    public Set<Long> getSimulatedCaseIds() {
        final Set<Long> deletableCaseIds = new HashSet<>();
        simulatedCaseDataViews.forEach(deletedCaseDataView ->
                deletableCaseIds.addAll(Stream.of(List.of(deletedCaseDataView.getCaseRef()),
                                deletedCaseDataView.getLinkedCaseIds())
                        .flatMap(Collection::stream)
                        .collect(toSet())));
        return deletableCaseIds;
    }

    private List<CaseDataView> getCaseCaseDataViewByState(final List<CaseDataView> caseDataViews,
                                                          final String state) {
        return caseDataViews.stream()
                .filter(caseDataView -> caseDataView.getState().equals(state))
                .collect(toList());
    }
}
