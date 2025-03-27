package uk.gov.hmcts.reform.ccd.util.log;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY_SIMULATION;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE_SIMULATION;

@ExtendWith(MockitoExtension.class)
class CaseFamiliesFilterTest {
    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private CaseFamiliesFilter caseFamiliesFilter;

    @Test
    void shouldFilterSimulationCasesOnly() {
        doReturn(of(DELETABLE_CASE_TYPE_SIMULATION)).when(parameterResolver)
                .getDeletableCaseTypesSimulation();

        final List<CaseFamily> caseFamilies = asList(DELETABLE_CASE_FAMILY, DELETABLE_CASE_FAMILY_SIMULATION);

        final List<CaseFamily> caseFamiliesSimulation = caseFamiliesFilter.getSimulationCasesOnly(caseFamilies);

        assertThat(caseFamiliesSimulation).hasSize(1);

        assertThat(caseFamiliesSimulation.getFirst().linkedCases().getFirst().id())
                .isEqualTo(DELETABLE_CASE_FAMILY_SIMULATION.linkedCases().getFirst().id());
        assertThat(caseFamiliesSimulation.getFirst().linkedCases().getFirst().caseType())
                .isEqualTo(DELETABLE_CASE_FAMILY_SIMULATION.linkedCases().getFirst().caseType());

        assertThat(caseFamiliesSimulation.getFirst().linkedCases().get(1).id())
                .isEqualTo(DELETABLE_CASE_FAMILY_SIMULATION.linkedCases().get(1).id());
        assertThat(caseFamiliesSimulation.getFirst().linkedCases().get(1).caseType())
                .isEqualTo(DELETABLE_CASE_FAMILY_SIMULATION.linkedCases().get(1).caseType());
    }

    @Test
    void shouldFilterDeletableCasesOnly() {
        doReturn(of(DELETABLE_CASE_TYPE)).when(parameterResolver)
                .getDeletableCaseTypes();

        final List<CaseFamily> caseFamilies = asList(DELETABLE_CASE_FAMILY, DELETABLE_CASE_FAMILY_SIMULATION);

        final List<CaseFamily> deletableCasesOnly = caseFamiliesFilter.getDeletableCasesOnly(caseFamilies);

        assertThat(deletableCasesOnly).hasSize(1);
        assertThat(deletableCasesOnly.getFirst().linkedCases()).hasSize(4);

        assertThat(deletableCasesOnly.getFirst().linkedCases().getFirst().id())
                .isEqualTo(DELETABLE_CASE_FAMILY.linkedCases().getFirst().id());
        assertThat(deletableCasesOnly.getFirst().linkedCases().getFirst().caseType())
                .isEqualTo(DELETABLE_CASE_FAMILY.linkedCases().getFirst().caseType());

        assertThat(deletableCasesOnly.getFirst().linkedCases().get(1).id())
                .isEqualTo(DELETABLE_CASE_FAMILY.linkedCases().get(1).id());
        assertThat(deletableCasesOnly.getFirst().linkedCases().get(1).caseType())
                .isEqualTo(DELETABLE_CASE_FAMILY.linkedCases().get(1).caseType());
    }
}
