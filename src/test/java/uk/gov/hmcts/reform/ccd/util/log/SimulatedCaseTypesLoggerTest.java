package uk.gov.hmcts.reform.ccd.util.log;


import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class SimulatedCaseTypesLoggerTest {

    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private SimulatedCaseTypesLogger simulatedCaseTypesLogger;

    private LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(SimulatedCaseTypesLogger.class);
    }


    @Test
    void shouldLogSimulatedCaseTypeInAppInsights() {
        // Given
        CaseData rootCase = mock(CaseData.class);
        when(rootCase.getReference()).thenReturn(12345L);
        when(rootCase.getCaseType()).thenReturn("TestCaseType");

        CaseFamily caseFamily = mock(CaseFamily.class);
        when(caseFamily.getRootCase()).thenReturn(rootCase);
        when(caseFamily.getLinkedCases()).thenReturn(List.of());

        List<CaseFamily> caseFamilies = List.of(caseFamily);

        when(parameterResolver.getDeletableCaseTypesSimulation()).thenReturn(List.of("TestCaseType"));

        simulatedCaseTypesLogger.logSimulatedCaseTypeInAppInsights(caseFamilies);

        assertTrue(logCaptor.getInfoLogs().contains("Simulated case type: TestCaseType, Case refs: [12345]"));
    }


    @Test
    void shouldNotLogSimulatedCaseTypeInAppInsights() {
        final CaseFamily caseFamily = mock(CaseFamily.class);
        final List<CaseFamily> caseFamilies = List.of(caseFamily);

        when(parameterResolver.getDeletableCaseTypesSimulation()).thenReturn(Collections.emptyList());

        simulatedCaseTypesLogger.logSimulatedCaseTypeInAppInsights(caseFamilies);

        assertFalse(logCaptor.getInfoLogs().contains("Simulated case type: TestCaseType, Case refs: [12345]"));
    }
}



