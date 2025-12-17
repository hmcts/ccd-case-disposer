package uk.gov.hmcts.reform.ccd.util.log;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

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

    @Test
    void snapshotShouldReturnCurrentState() {
        HearingDeletionRecordHolder holder = new HearingDeletionRecordHolder();
        holder.setHearingDeletionResults("case1", 200);
        holder.setHearingDeletionResults("case2", 404);

        Map<String, Integer> snapshot = holder.snapshot();

        Assertions.assertThat(snapshot).hasSize(2);
        Assertions.assertThat(snapshot).containsEntry("case1", 200);
        Assertions.assertThat(snapshot).containsEntry("case2", 404);
    }

    @Test
    void clearShouldRemoveAllEntries() {
        HearingDeletionRecordHolder holder = new HearingDeletionRecordHolder();
        holder.setHearingDeletionResults("case1", 200);
        holder.setHearingDeletionResults("case2", 404);

        holder.clear();

        Assertions.assertThat(holder.snapshot()).isEmpty();
    }

}
