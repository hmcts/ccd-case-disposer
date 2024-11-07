package uk.gov.hmcts.reform.ccd.util.log;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HearingDeletionRecordHolderTest {


    @Test
    void shouldHoldCaseHearingDeletionResults() {
        final HearingDeletionRecordHolder hearingDeletionRecordHolder = new HearingDeletionRecordHolder();

        final int caseRolesDeletionBeforeCaseRefMapping_1 = HttpStatus.OK.value();
        final int caseRolesDeletionBeforeCaseRefMapping_2 = HttpStatus.OK.value();

        hearingDeletionRecordHolder.setHearingDeletionResults(
            "123",
            caseRolesDeletionBeforeCaseRefMapping_1
        );
        hearingDeletionRecordHolder.setHearingDeletionResults(
            "456",
            caseRolesDeletionBeforeCaseRefMapping_2
        );

        final int caseRolesDeletionResults_1 =
            hearingDeletionRecordHolder.getHearingDeletionResults("123");
        final int caseRolesDeletionResults_2 =
            hearingDeletionRecordHolder.getHearingDeletionResults("456");

        assertThat(caseRolesDeletionBeforeCaseRefMapping_1)
            .isEqualTo(caseRolesDeletionResults_1);

        assertThat(caseRolesDeletionBeforeCaseRefMapping_2)
            .isEqualTo(caseRolesDeletionResults_2);
    }

}
