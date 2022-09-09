package uk.gov.hmcts.reform.ccd.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.util.log.SimulatedCaseDataViewHolder;

import java.util.Map;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class SimulationTestUtils {

    @Inject
    private SimulatedCaseDataViewHolder simulatedCaseDataViewHolder;

    public void verifyDatabaseDeletionSimulation(final Map<String, Integer> endStateNumberOfDatastoreRecords) {
        final int totalNumberOfExpectedSimulationRecords = endStateNumberOfDatastoreRecords.values()
                .stream()
                .mapToInt(i -> i.intValue())
                .sum();

        assertThat(totalNumberOfExpectedSimulationRecords)
                .isEqualTo(simulatedCaseDataViewHolder.getSimulatedCaseIds().size());
    }
}