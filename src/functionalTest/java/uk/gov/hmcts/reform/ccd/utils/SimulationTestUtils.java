package uk.gov.hmcts.reform.ccd.utils;

import jakarta.inject.Inject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class SimulationTestUtils {

    @Inject
    private ProcessedCasesRecordHolder processedCasesRecordHolder;

    public void verifyDatabaseDeletionSimulation(final List<Long> simulatedEndStateRowIds) {
        List<Long> processedRowIds = processedCasesRecordHolder.getSimulatedCases()
            .stream()
            .map(CaseData::reference)
            .toList();

        assertThat(simulatedEndStateRowIds)
            .isNotNull()
            .containsExactlyInAnyOrderElementsOf(processedRowIds);
    }
}
