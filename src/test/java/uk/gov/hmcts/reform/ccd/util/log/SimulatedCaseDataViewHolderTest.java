//package uk.gov.hmcts.reform.ccd.util.log;
//
//import com.google.common.collect.Sets;
//import org.junit.jupiter.api.Test;
//import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;
//import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Set;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY;
//import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY_SIMULATION;
//import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_STATE;
//import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_STATE;
//
//class SimulatedCaseDataViewHolderTest {
//
//    @Test
//    void shouldGetSimulationCaseIdsOnly() {
//        final List<CaseFamily> deletableCaseFamily = List.of(DELETABLE_CASE_FAMILY);
//        final List<CaseFamily> simulationCaseFamily = Arrays.asList(DELETABLE_CASE_FAMILY_SIMULATION);
//
//        final List<CaseDataView> caseDataViews = new ArrayList<>();
//
//        new CaseDataViewBuilder().buildCaseDataViewList(deletableCaseFamily, caseDataViews, DELETED_STATE);
//        new CaseDataViewBuilder().buildCaseDataViewList(simulationCaseFamily, caseDataViews, SIMULATED_STATE);
//
//        final SimulatedCaseDataViewHolder simulatedCaseDataViewHolder = new SimulatedCaseDataViewHolder();
//        simulatedCaseDataViewHolder.setUpData(caseDataViews);
//
//        final Set<Long> expectedIds = Sets.newHashSet(
//            DELETABLE_CASE_FAMILY_SIMULATION.getRootCase().getId(),
//            DELETABLE_CASE_FAMILY_SIMULATION.getLinkedCases().get(0).getId(),
//            DELETABLE_CASE_FAMILY_SIMULATION.getLinkedCases().get(1).getId());
//
//        assertThat(simulatedCaseDataViewHolder.getSimulatedCaseIds())
//                .isNotNull()
//                .containsExactlyInAnyOrderElementsOf(expectedIds);
//    }
//}
