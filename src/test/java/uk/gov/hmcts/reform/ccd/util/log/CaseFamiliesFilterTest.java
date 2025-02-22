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

        final List<CaseFamily> caseFamilies = asList(DELETABLE_CASE_FAMILY,
                DELETABLE_CASE_FAMILY_SIMULATION);

        final List<CaseFamily> caseFamiliesSimulation = caseFamiliesFilter.geSimulationCasesOnly(caseFamilies);

        assertThat(caseFamiliesSimulation).hasSize(1);

        assertThat(caseFamiliesSimulation.getFirst().getRootCase().getId())
                .isEqualTo(DELETABLE_CASE_FAMILY_SIMULATION.getRootCase().getId());
        assertThat(caseFamiliesSimulation.getFirst().getRootCase().getCaseType())
                .isEqualTo(DELETABLE_CASE_FAMILY_SIMULATION.getRootCase().getCaseType());


        assertThat(caseFamiliesSimulation.getFirst().getLinkedCases().getFirst().getId())
                .isEqualTo(DELETABLE_CASE_FAMILY_SIMULATION.getLinkedCases().getFirst().getId());
        assertThat(caseFamiliesSimulation.getFirst().getLinkedCases().getFirst().getCaseType())
                .isEqualTo(DELETABLE_CASE_FAMILY_SIMULATION.getLinkedCases().getFirst().getCaseType());

        assertThat(caseFamiliesSimulation.getFirst().getLinkedCases().get(1).getId())
                .isEqualTo(DELETABLE_CASE_FAMILY_SIMULATION.getLinkedCases().get(1).getId());
        assertThat(caseFamiliesSimulation.getFirst().getLinkedCases().get(1).getCaseType())
                .isEqualTo(DELETABLE_CASE_FAMILY_SIMULATION.getLinkedCases().get(1).getCaseType());
    }

    @Test
    void shouldFilterDeletableCasesOnly() {
        doReturn(of(DELETABLE_CASE_TYPE)).when(parameterResolver)
                .getDeletableCaseTypes();

        final List<CaseFamily> caseFamilies = asList(DELETABLE_CASE_FAMILY,
                DELETABLE_CASE_FAMILY_SIMULATION);

        final List<CaseFamily> deletableCasesOnly = caseFamiliesFilter.getDeletableCasesOnly(caseFamilies);

        assertThat(deletableCasesOnly).hasSize(1);

        assertThat(deletableCasesOnly.getFirst().getRootCase().getId())
                .isEqualTo(DELETABLE_CASE_FAMILY.getRootCase().getId());
        assertThat(deletableCasesOnly.getFirst().getRootCase().getCaseType())
                .isEqualTo(DELETABLE_CASE_FAMILY.getRootCase().getCaseType());


        assertThat(deletableCasesOnly.getFirst().getLinkedCases().getFirst().getId())
                .isEqualTo(DELETABLE_CASE_FAMILY.getLinkedCases().getFirst().getId());
        assertThat(deletableCasesOnly.getFirst().getLinkedCases().getFirst().getCaseType())
                .isEqualTo(DELETABLE_CASE_FAMILY.getLinkedCases().getFirst().getCaseType());

        assertThat(deletableCasesOnly.getFirst().getLinkedCases().get(1).getId())
                .isEqualTo(DELETABLE_CASE_FAMILY.getLinkedCases().get(1).getId());
        assertThat(deletableCasesOnly.getFirst().getLinkedCases().get(1).getCaseType())
                .isEqualTo(DELETABLE_CASE_FAMILY.getLinkedCases().get(1).getCaseType());
    }
}
