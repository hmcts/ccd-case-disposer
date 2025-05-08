package uk.gov.hmcts.reform.ccd.utils;

import jakarta.inject.Inject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class SimulationIntegrationTestUtils {

    @Inject
    private ProcessedCasesRecordHolder processedCasesRecordHolder;

    public void verifyDatabaseDeletionSimulation(final List<Long> simulatedEndStateRowIds) {
        Set<CaseData> simulatedCases = processedCasesRecordHolder.getSimulatedCases();
        List<Long> simulatedCaseIds = simulatedCases.stream().map(CaseData::reference).toList();
        assertThat(simulatedCaseIds)
                .isNotNull()
                .containsExactlyInAnyOrderElementsOf(simulatedEndStateRowIds);
    }
}
