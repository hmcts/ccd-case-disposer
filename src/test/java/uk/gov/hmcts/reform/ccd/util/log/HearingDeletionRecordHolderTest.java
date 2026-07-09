package uk.gov.hmcts.reform.ccd.util.log;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HearingDeletionRecordHolderTest {


    @Test
    void shouldHoldCaseHearingDeletionResults() {
        final HearingDeletionRecordHolder hearingDeletionRecordHolder = new HearingDeletionRecordHolder();

        final int caseRolesDeletionBeforeCaseRefMapping1 = HttpStatus.OK.value();
        final int caseRolesDeletionBeforeCaseRefMapping2 = HttpStatus.OK.value();

        hearingDeletionRecordHolder.setHearingDeletionResults(
            "123",
            caseRolesDeletionBeforeCaseRefMapping1
        );
        hearingDeletionRecordHolder.setHearingDeletionResults(
            "456",
            caseRolesDeletionBeforeCaseRefMapping2
        );

        final int caseRolesDeletionResults1 =
            hearingDeletionRecordHolder.getHearingDeletionResults("123");
        final int caseRolesDeletionResults2 =
            hearingDeletionRecordHolder.getHearingDeletionResults("456");

        assertThat(caseRolesDeletionBeforeCaseRefMapping1)
            .isEqualTo(caseRolesDeletionResults1);

        assertThat(caseRolesDeletionBeforeCaseRefMapping2)
            .isEqualTo(caseRolesDeletionResults2);
    }

}
