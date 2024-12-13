package uk.gov.hmcts.reform.ccd.util;


import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessedCasesRecordHolderTest {

    @Test
    void shouldAddProcessedCase() {
        // given
        ProcessedCasesRecordHolder processedCasesRecordHolder = new ProcessedCasesRecordHolder();
        CaseData caseData = CaseData.builder().reference(1L).build();

        // when
        processedCasesRecordHolder.addProcessedCase(caseData);

        // then
        assertThat(processedCasesRecordHolder.getProcessedCases()).contains(caseData);
    }

    @Test
    void shouldAddFailedToDeleteCaseRef() {
        // given
        ProcessedCasesRecordHolder processedCasesRecordHolder = new ProcessedCasesRecordHolder();
        CaseData caseData = CaseData.builder().reference(1L).build();

        // when
        processedCasesRecordHolder.addFailedToDeleteCaseRef(caseData);

        // then
        assertThat(processedCasesRecordHolder.getFailedToDeleteCaseRefs()).contains(1L);
    }
}
