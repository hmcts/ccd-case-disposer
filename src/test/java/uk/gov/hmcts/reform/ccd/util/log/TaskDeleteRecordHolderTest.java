package uk.gov.hmcts.reform.ccd.util.log;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TaskDeleteRecordHolderTest {

    @Test
    void shouldHoldTasksDeletionResults() {
        final TasksDeletionRecordHolder taskDeletionRecordHolder = new TasksDeletionRecordHolder();

        final Integer caseTasksDeletionResult1 = 201;
        final Integer caseTasksDeletionResult2 = 403;

        taskDeletionRecordHolder.setCaseTasksDeletionResults("123",caseTasksDeletionResult1);
        taskDeletionRecordHolder.setCaseTasksDeletionResults("456", caseTasksDeletionResult2);

        final int tasksDeletionResults1 = taskDeletionRecordHolder.getTasksDeletionResults("123");
        final int tasksDeletionResults2 = taskDeletionRecordHolder.getTasksDeletionResults("456");

        assertThat(tasksDeletionResults1).isEqualTo(caseTasksDeletionResult1);
        assertThat(tasksDeletionResults2).isEqualTo(caseTasksDeletionResult2);

    }
}
