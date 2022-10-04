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
}