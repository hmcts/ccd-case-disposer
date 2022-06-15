package uk.gov.hmcts.reform.ccd.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.util.log.RoleDeletionRecordHolder;

import java.util.List;
import javax.inject.Inject;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.ROLE_DELETE;

@Component
public class RoleDeleteIntegrationTestUtils {

    @Inject
    private RoleDeletionRecordHolder roleDeletionRecordHolder;

    public void verifyRoleAssignmentDeletion(final List<Long> caseRefDeletedRoles) {
        caseRefDeletedRoles.forEach(caseRef -> {
            final int caseRolesDeletionMocks =
                ROLE_DELETE.get(Long.toString(caseRef));

            final int caseRolesDeletionActualResults = roleDeletionRecordHolder
                .getCaseRolesDeletionResults(Long.toString(caseRef));

            assertThat(caseRolesDeletionActualResults).isEqualTo(caseRolesDeletionMocks);
        });
    }
}
