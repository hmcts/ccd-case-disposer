package uk.gov.hmcts.reform.ccd.utils;

import jakarta.inject.Inject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.util.log.TasksDeletionRecordHolder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.TASKS_DELETE;

@Component
public class TasksRemoteDeletionVerifier implements RemoteDeletionVerifier<Map<String, Integer>> {

    @Inject
    private TasksDeletionRecordHolder tasksDeletionRecordHolder;

    @Override
    public Map<String, Integer> snapshot() {
        return tasksDeletionRecordHolder.snapshot();
    }

    @Override
    public void clear() {
        tasksDeletionRecordHolder.clear();
    }

    public void assertDeletionResults(
        Map<String, Integer> snapshot,
        List<Long> caseRefs) {

        caseRefs.forEach(caseRef -> {
            String caseRefStr = caseRef.toString();
            int expected = TASKS_DELETE.getOrDefault(caseRefStr, 201);
            assertThat(snapshot.get(caseRefStr)).isEqualTo(expected);
        });
    }
}
