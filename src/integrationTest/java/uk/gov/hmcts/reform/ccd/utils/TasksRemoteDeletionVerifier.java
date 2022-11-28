package uk.gov.hmcts.reform.ccd.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.util.log.RoleDeletionRecordHolder;
import uk.gov.hmcts.reform.ccd.util.log.TasksDeletionRecordHolder;

import java.util.List;
import javax.inject.Inject;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.ROLE_DELETE;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.TASKS_DELETE;

@Component
public class TasksRemoteDeletionVerifier implements RemoteDeletionVerifier {

    @Inject
    private TasksDeletionRecordHolder tasksDeletionRecordHolder;

    public void verifyRemoteDeletion(final List<Long> caseRefDeletedRoles) {
        caseRefDeletedRoles.forEach(caseRef -> {
            final int tasksDeletionMocks =
                TASKS_DELETE.get(Long.toString(caseRef));

            final int tasksDeletionResults = tasksDeletionRecordHolder
                .getTasksDeletionResults(Long.toString(caseRef));

            assertThat(tasksDeletionResults).isEqualTo(tasksDeletionMocks);
        });
    }
}
