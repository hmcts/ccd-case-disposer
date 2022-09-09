package uk.gov.hmcts.reform.ccd.util.log;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DataStoreRecordHolderTest {

    @Test
    void shouldAddRecord() {
        final DataStoreRecordHolder dataStoreRecordHolder = new DataStoreRecordHolder();
        dataStoreRecordHolder.addRecord("testCaseType", "123");
        dataStoreRecordHolder.addRecord("testCaseType", "456");

        assertThat(dataStoreRecordHolder.getDatastoreCases().size()).isEqualTo(1);
        assertThat(dataStoreRecordHolder.getDatastoreCases().containsKey("testCaseType")).isTrue();
        assertThat(dataStoreRecordHolder.getDatastoreCases().get("testCaseType").size()).isEqualTo(2);
        assertThat(dataStoreRecordHolder.getDatastoreCases().get("testCaseType").get(0)).isEqualTo("123");
        assertThat(dataStoreRecordHolder.getDatastoreCases().get("testCaseType").get(1)).isEqualTo("456");
    }
}