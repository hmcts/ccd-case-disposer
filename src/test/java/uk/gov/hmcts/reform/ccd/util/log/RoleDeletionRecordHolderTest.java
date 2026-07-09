package uk.gov.hmcts.reform.ccd.util.log;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RoleDeletionRecordHolderTest {

    @Test
    void shouldHoldCaseRolesDeletionResults() {
        final RoleDeletionRecordHolder roleDeletionRecordHolder = new RoleDeletionRecordHolder();

        final int caseRolesDeletionBeforeCaseRefMapping1 = HttpStatus.OK.value();
        final int caseRolesDeletionBeforeCaseRefMapping2 = HttpStatus.OK.value();

        roleDeletionRecordHolder.setCaseRolesDeletionResults("123",
                                                             caseRolesDeletionBeforeCaseRefMapping1);
        roleDeletionRecordHolder.setCaseRolesDeletionResults("456",
                                                             caseRolesDeletionBeforeCaseRefMapping2);

        final int caseRolesDeletionResults1 =
            roleDeletionRecordHolder.getCaseRolesDeletionResults("123");
        final int caseRolesDeletionResults2 =
            roleDeletionRecordHolder.getCaseRolesDeletionResults("456");

        assertThat(caseRolesDeletionBeforeCaseRefMapping1)
            .isEqualTo(caseRolesDeletionResults1);

        assertThat(caseRolesDeletionBeforeCaseRefMapping2)
            .isEqualTo(caseRolesDeletionResults2);
    }
}
