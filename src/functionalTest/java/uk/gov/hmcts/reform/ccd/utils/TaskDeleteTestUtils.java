package uk.gov.hmcts.reform.ccd.utils;

import jakarta.inject.Inject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.util.log.TasksDeletionRecordHolder;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.with;

@Component
public class TaskDeleteTestUtils {

    @Inject
    private TasksDeletionRecordHolder tasksDeletionRecordHolder;

    public void verifyTasksDeletion(final List<Long>  deletableRowIds) {
        deletableRowIds.forEach(caseRef -> {
            with().await()
                .untilAsserted(() -> {
                    final int taskDeletionActualResults = tasksDeletionRecordHolder
                        .getTasksDeletionResults(Long.toString(caseRef));

                    assertThat(taskDeletionActualResults).isEqualTo(HttpStatus.CREATED.value());
                });
        });
    }

}
