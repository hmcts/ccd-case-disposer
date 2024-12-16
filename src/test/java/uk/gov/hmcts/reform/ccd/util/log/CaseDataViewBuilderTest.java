package uk.gov.hmcts.reform.ccd.util.log;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE_SIMULATION;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.FAILED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_STATE;

class CaseDataViewBuilderTest {

    @ParameterizedTest
    @MethodSource("provideCaseTypeAndProcessedState")
    void shouldCreateCaseDataViewForDeletableCases(String caseType, String state) {
        final List<CaseData> deletableCaseFamily = List.of(
            CaseData.builder().id(1L).caseType(caseType).reference(11L).build(),
            CaseData.builder().id(2L).caseType(caseType).reference(22L).build()
        );

        final List<CaseDataView> caseDataViews = new ArrayList<>();

        new CaseDataViewBuilder().buildCaseDataViewList(deletableCaseFamily, caseDataViews, state);

        assertThat(caseDataViews).hasSize(2);
        assertThat(caseDataViews.getFirst().getCaseType()).isEqualTo(caseType);
        assertThat(caseDataViews.getFirst().getCaseRef()).isEqualTo(11L);
        assertThat(caseDataViews.getFirst().getState()).isEqualTo(state);
    }

    private static Stream<Arguments> provideCaseTypeAndProcessedState() {
        return Stream.of(
            Arguments.of(DELETABLE_CASE_TYPE, DELETED_STATE),
            Arguments.of(DELETABLE_CASE_TYPE_SIMULATION, SIMULATED_STATE),
            Arguments.of(DELETABLE_CASE_TYPE, FAILED_STATE)
        );
    }
}
