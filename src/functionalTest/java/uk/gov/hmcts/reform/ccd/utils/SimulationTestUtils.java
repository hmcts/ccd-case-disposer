package uk.gov.hmcts.reform.ccd.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.util.log.SimulatedCaseDataViewHolder;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class SimulationTestUtils {

    @Inject
    private SimulatedCaseDataViewHolder simulatedCaseDataViewHolder;

    public void verifyDatabaseDeletionSimulation() {
        assertThat(simulatedCaseDataViewHolder.getSimulatedCaseIds().size()).isNotZero();
    }
}