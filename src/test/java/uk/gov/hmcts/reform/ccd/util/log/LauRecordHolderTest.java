package uk.gov.hmcts.reform.ccd.util.log;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LauRecordHolderTest {

    @Test
    void shouldAddCaseRef() {
        final LauRecordHolder lauRecordHolder = new LauRecordHolder();
        lauRecordHolder.addLauCaseRef("123");
        assertThat(lauRecordHolder.getLauCaseRefList().size()).isEqualTo(1);
    }


    @Test
    void shouldReturnSnapshotOfCurrentCaseRefs() {
        LauRecordHolder holder = new LauRecordHolder();
        holder.addLauCaseRef("111");
        holder.addLauCaseRef("222");

        assertThat(holder.snapshot()).containsExactlyInAnyOrder("111", "222");
    }

    @Test
    void shouldClearAllCaseRefs() {
        LauRecordHolder holder = new LauRecordHolder();
        holder.addLauCaseRef("333");
        holder.addLauCaseRef("444");

        holder.clear();

        assertThat(holder.getLauCaseRefList()).isEmpty();
        assertThat(holder.snapshot()).isEmpty();
    }
}
