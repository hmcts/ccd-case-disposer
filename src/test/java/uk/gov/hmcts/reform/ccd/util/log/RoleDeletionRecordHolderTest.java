package uk.gov.hmcts.reform.ccd.util.log;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RoleDeletionRecordHolderTest {

    @Test
    void shouldHoldCaseRolesDeletionResults() {
        final RoleDeletionRecordHolder roleDeletionRecordHolder = new RoleDeletionRecordHolder();

        final int caseRolesDeletionBeforeCaseRefMapping_1 = HttpStatus.OK.value();
        final int caseRolesDeletionBeforeCaseRefMapping_2 = HttpStatus.OK.value();

        roleDeletionRecordHolder.setCaseRolesDeletionResults("123",
                                                             caseRolesDeletionBeforeCaseRefMapping_1);
        roleDeletionRecordHolder.setCaseRolesDeletionResults("456",
                                                             caseRolesDeletionBeforeCaseRefMapping_2);

        final int caseRolesDeletionResults_1 =
            roleDeletionRecordHolder.getCaseRolesDeletionResults("123");
        final int caseRolesDeletionResults_2 =
            roleDeletionRecordHolder.getCaseRolesDeletionResults("456");

        assertThat(caseRolesDeletionBeforeCaseRefMapping_1)
            .isEqualTo(caseRolesDeletionResults_1);

        assertThat(caseRolesDeletionBeforeCaseRefMapping_2)
            .isEqualTo(caseRolesDeletionResults_2);
    }
}
