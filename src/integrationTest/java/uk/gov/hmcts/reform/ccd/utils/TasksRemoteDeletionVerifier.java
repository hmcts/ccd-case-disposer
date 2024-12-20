package uk.gov.hmcts.reform.ccd.utils;

import jakarta.inject.Inject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.util.log.TasksDeletionRecordHolder;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.TASKS_DELETE;

@Component
public class TasksRemoteDeletionVerifier implements RemoteDeletionVerifier {

    @Inject
    private TasksDeletionRecordHolder tasksDeletionRecordHolder;

    public void verifyRemoteDeletion(final List<Long> caseRefs) {
        caseRefs.forEach(caseRef -> {
            String caseRefStr = Long.toString(caseRef);
            int expectedResult = 201;
            if (TASKS_DELETE.containsKey(caseRefStr)) {
                expectedResult = TASKS_DELETE.get(caseRefStr);
            }

            final int tasksDeletionResults = tasksDeletionRecordHolder
                .getTasksDeletionResults(Long.toString(caseRef));
            assertThat(tasksDeletionResults).isEqualTo(expectedResult);
        });
    }
}
