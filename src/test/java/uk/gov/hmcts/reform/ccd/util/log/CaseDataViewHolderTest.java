package uk.gov.hmcts.reform.ccd.util.log;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY_SIMULATION;

class CaseDataViewHolderTest {

    @Test
    void shouldGetSimulationCaseIdsOnly() {
        final List<CaseFamily> deletableCaseFamily = List.of(DELETABLE_CASE_FAMILY);
        final List<CaseFamily> simulationCaseFamily = Arrays.asList(DELETABLE_CASE_FAMILY_SIMULATION);

        final List<CaseDataView> caseDataViews = new ArrayList<>();

        new CaseDataViewBuilder().buildCaseDataViewList(deletableCaseFamily, caseDataViews, true);
        new CaseDataViewBuilder().buildCaseDataViewList(simulationCaseFamily, caseDataViews, false);

        final CaseDataViewHolder caseDataViewHolder = new CaseDataViewHolder();
        caseDataViewHolder.setUpData(caseDataViews);

        final Set<Long> expectedIds = Sets.newHashSet(DELETABLE_CASE_FAMILY_SIMULATION.getRootCase().getId(),
                DELETABLE_CASE_FAMILY_SIMULATION.getLinkedCases().get(0).getId(),
                DELETABLE_CASE_FAMILY_SIMULATION.getLinkedCases().get(1).getId());

        assertThat(caseDataViewHolder.getSimulatedCaseIds())
                .isNotNull()
                .containsExactlyInAnyOrderElementsOf(expectedIds);
    }
}