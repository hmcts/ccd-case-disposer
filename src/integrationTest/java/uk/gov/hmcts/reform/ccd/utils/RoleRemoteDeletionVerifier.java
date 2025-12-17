package uk.gov.hmcts.reform.ccd.utils;

import jakarta.inject.Inject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.util.log.RoleDeletionRecordHolder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.ROLE_DELETE;

@Component
public class RoleRemoteDeletionVerifier implements RemoteDeletionVerifier<Map<String, Integer>> {

    @Inject
    private RoleDeletionRecordHolder roleDeletionRecordHolder;

    @Override
    public Map<String, Integer> snapshot() {
        return roleDeletionRecordHolder.snapshot();
    }

    @Override
    public void clear() {
        roleDeletionRecordHolder.clear();
    }

    /**
     * Verifies role deletion results using a SNAPSHOT.
     * Safe to call inside Awaitility.
     */
    public void assertDeletionResults(Map<String, Integer> snapshot,
        List<Long> caseRefDeletedRoles) {

        caseRefDeletedRoles.forEach(caseRef -> {
            String caseRefStr = caseRef.toString();
            int expected = ROLE_DELETE.getOrDefault(caseRefStr, 200);
            Integer actual = snapshot.get(caseRefStr);
            assertThat(actual)
                .as("Role deletion result missing for caseRef %s", caseRefStr)
                .isNotNull();

            assertThat(actual)
                .as("Role deletion status mismatch for caseRef %s", caseRefStr)
                .isEqualTo(expected);
        });
    }
}
