//package uk.gov.hmcts.reform.ccd.util.log;
//
//import org.junit.jupiter.api.Test;
//import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;
//import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY;
//import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY_SIMULATION;
//import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_STATE;
//import static uk.gov.hmcts.reform.ccd.util.LogConstants.FAILED_STATE;
//import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_STATE;
//
//class CaseDataViewBuilderTest {
//    @Test
//    void shouldCreateCaseDataViewForDeletableCases() {
//        final List<CaseFamily> deletableCaseFamily = List.of(DELETABLE_CASE_FAMILY);
//
//        final List<CaseDataView> caseDataViews = new ArrayList<>();
//
//        new CaseDataViewBuilder().buildCaseDataViewList(deletableCaseFamily, caseDataViews, DELETED_STATE);
//
//        assertThat(caseDataViews.size()).isEqualTo(4);
//        assertThat(caseDataViews.get(0).getCaseType()).isEqualTo(DELETABLE_CASE_FAMILY.getRootCase().getCaseType());
//        assertThat(caseDataViews.get(0).getCaseRef()).isEqualTo(DELETABLE_CASE_FAMILY.getRootCase().getId());
//        assertThat(caseDataViews.get(0).getState()).isEqualTo(DELETED_STATE);
//        assertThat(caseDataViews.get(0).getLinkedCaseIds().get(0))
//                .isEqualTo(DELETABLE_CASE_FAMILY.getLinkedCases().get(0).getId());
//        assertThat(caseDataViews.get(0).getLinkedCaseIds().get(1))
//                .isEqualTo(DELETABLE_CASE_FAMILY.getLinkedCases().get(1).getId());
//        assertThat(caseDataViews.get(0).getLinkedCaseIds().get(2))
//                .isEqualTo(DELETABLE_CASE_FAMILY.getLinkedCases().get(2).getId());
//    }
//
//    @Test
//    void shouldCreateCaseDataViewForSimulationCases() {
//        final List<CaseFamily> deletableCaseFamilySimulation = List.of(DELETABLE_CASE_FAMILY_SIMULATION);
//
//        final List<CaseDataView> caseDataViews = new ArrayList<>();
//
//        new CaseDataViewBuilder().buildCaseDataViewList(deletableCaseFamilySimulation, caseDataViews, SIMULATED_STATE);
//
//        assertThat(caseDataViews.size()).isEqualTo(3);
//        assertThat(caseDataViews.get(0).getCaseType())
//                .isEqualTo(DELETABLE_CASE_FAMILY_SIMULATION.getRootCase().getCaseType());
//        assertThat(caseDataViews.get(0).getCaseRef()).isEqualTo(DELETABLE_CASE_FAMILY_SIMULATION.getRootCase().getId());
//        assertThat(caseDataViews.get(0).getState()).isEqualTo(SIMULATED_STATE);
//        assertThat(caseDataViews.get(0).getLinkedCaseIds().get(0))
//                .isEqualTo(DELETABLE_CASE_FAMILY_SIMULATION.getLinkedCases().get(0).getId());
//        assertThat(caseDataViews.get(0).getLinkedCaseIds().get(1))
//                .isEqualTo(DELETABLE_CASE_FAMILY_SIMULATION.getLinkedCases().get(1).getId());
//    }
//
//    @Test
//    void shouldCreateCaseDataViewForFailedCases() {
//        final List<CaseFamily> deletableCaseFamily = List.of(DELETABLE_CASE_FAMILY);
//
//        final List<CaseDataView> caseDataViews = new ArrayList<>();
//
//        new CaseDataViewBuilder().buildCaseDataViewList(deletableCaseFamily, caseDataViews, FAILED_STATE);
//
//        assertThat(caseDataViews.size()).isEqualTo(4);
//        assertThat(caseDataViews.get(0).getCaseType()).isEqualTo(DELETABLE_CASE_FAMILY.getRootCase().getCaseType());
//        assertThat(caseDataViews.get(0).getCaseRef()).isEqualTo(DELETABLE_CASE_FAMILY.getRootCase().getId());
//        assertThat(caseDataViews.get(0).getState()).isEqualTo(FAILED_STATE);
//        assertThat(caseDataViews.get(0).getLinkedCaseIds().get(0))
//                .isEqualTo(DELETABLE_CASE_FAMILY.getLinkedCases().get(0).getId());
//        assertThat(caseDataViews.get(0).getLinkedCaseIds().get(1))
//                .isEqualTo(DELETABLE_CASE_FAMILY.getLinkedCases().get(1).getId());
//        assertThat(caseDataViews.get(0).getLinkedCaseIds().get(2))
//                .isEqualTo(DELETABLE_CASE_FAMILY.getLinkedCases().get(2).getId());
//    }
//}
