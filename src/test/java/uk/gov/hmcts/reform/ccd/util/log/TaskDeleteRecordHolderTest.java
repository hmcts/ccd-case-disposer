package uk.gov.hmcts.reform.ccd.util.log;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TaskDeleteRecordHolderTest {

    @Test
    void shouldHoldTasksDeletionResults() {
        final TasksDeletionRecordHolder taskDeletionRecordHolder = new TasksDeletionRecordHolder();

        final Integer caseTasksDeletionResult_1 = 201;
        final Integer caseTasksDeletionResult_2 = 403;

        taskDeletionRecordHolder.setCaseTasksDeletionResults("123",caseTasksDeletionResult_1);
        taskDeletionRecordHolder.setCaseTasksDeletionResults("456", caseTasksDeletionResult_2);

        final int tasksDeletionResults_1 = taskDeletionRecordHolder.getTasksDeletionResults("123");
        final int tasksDeletionResults_2 = taskDeletionRecordHolder.getTasksDeletionResults("456");

        assertThat(tasksDeletionResults_1).isEqualTo(caseTasksDeletionResult_1);
        assertThat(tasksDeletionResults_2).isEqualTo(caseTasksDeletionResult_2);

    }
}
