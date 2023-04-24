package uk.gov.hmcts.reform.ccd.utils;

import jakarta.inject.Inject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.util.log.SimulatedCaseDataViewHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class SimulationTestUtils {

    @Inject
    private SimulatedCaseDataViewHolder simulatedCaseDataViewHolder;

    public void verifyDatabaseDeletionSimulation(final List<Long> simulatedEndStateRowIds) {
        assertThat(simulatedEndStateRowIds)
                .isNotNull()
                .containsExactlyInAnyOrderElementsOf(simulatedCaseDataViewHolder.getSimulatedCaseIds());
    }
}
