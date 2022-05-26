package uk.gov.hmcts.reform.ccd.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.util.log.CaseDataViewHolder;

import java.util.List;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class SimulationTestUtils {

    @Inject
    private CaseDataViewHolder caseDataViewHolder;

    public void verifyDatabaseDeletionSimulation(final List<Long> simulatedEndStateRowIds) {
        assertThat(simulatedEndStateRowIds)
                .isNotNull()
                .containsExactlyInAnyOrderElementsOf(caseDataViewHolder.getSimulatedCaseIds());
    }
}