package uk.gov.hmcts.reform.ccd.util;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessedCasesRecordHolderTest {

    ProcessedCasesRecordHolder processedCasesRecordHolder;

    @BeforeEach
    void setUp() {
        processedCasesRecordHolder = new ProcessedCasesRecordHolder();
    }

    @Test
    void shouldAddProcessedCase() {
        // given
        CaseData caseDataProcessed1 = CaseData.builder().id(1L).reference(1L).build();
        CaseData caseDataProcessed2 = CaseData.builder().id(2L).reference(2L).build();
        CaseData caseDataFailed3 = CaseData.builder().id(3L).reference(3L).build();
        CaseData caseDataFailed4 = CaseData.builder().id(4L).reference(4L).build();

        // when
        processedCasesRecordHolder.addProcessedCase(caseDataProcessed1);
        processedCasesRecordHolder.addProcessedCase(caseDataProcessed2);
        processedCasesRecordHolder.addProcessedCase(caseDataFailed3);
        processedCasesRecordHolder.addProcessedCase(caseDataFailed4);
        processedCasesRecordHolder.addFailedToDeleteCaseRef(caseDataFailed3);
        processedCasesRecordHolder.addFailedToDeleteCaseRef(caseDataFailed4);

        // then
        assertThat(processedCasesRecordHolder.getSuccessfullyDeletedCases())
            .contains(caseDataProcessed1, caseDataProcessed2);
        assertThat(processedCasesRecordHolder.getFailedToDeleteDeletedCases())
            .contains(caseDataFailed3, caseDataFailed4);
    }

    @Test
    void shouldNotContainDuplicates() {
        CaseData caseDataProcessed1 = CaseData.builder().id(1L).reference(1L).build();
        CaseData caseDataProcessed2 = CaseData.builder().id(1L).reference(1L).build();
        CaseData caseDataFailed3 = CaseData.builder().id(3L).reference(3L).build();
        CaseData caseDataFailed4 = CaseData.builder().id(3L).reference(3L).build();
        CaseData caseDataUndeletable5 = CaseData.builder().id(5L).reference(5L).build();
        CaseData caseDataUndeletable6 = CaseData.builder().id(5L).reference(5L).build();

        // when
        processedCasesRecordHolder.addProcessedCase(caseDataProcessed1);
        processedCasesRecordHolder.addProcessedCase(caseDataProcessed2);
        processedCasesRecordHolder.addProcessedCase(caseDataFailed3);
        processedCasesRecordHolder.addProcessedCase(caseDataFailed4);
        processedCasesRecordHolder.addFailedToDeleteCaseRef(caseDataFailed3);
        processedCasesRecordHolder.addFailedToDeleteCaseRef(caseDataFailed4);
        processedCasesRecordHolder.addNonDeletableCase(caseDataUndeletable5);
        processedCasesRecordHolder.addNonDeletableCase(caseDataUndeletable6);

        // then
        assertThat(processedCasesRecordHolder.getSuccessfullyDeletedCases()).containsOnly(caseDataProcessed1);
        assertThat(processedCasesRecordHolder.getFailedToDeleteDeletedCases()).containsOnly(caseDataFailed3);
    }

    @Test
    void shouldClearState() {
        // given
        CaseData caseDataProcessed1 = CaseData.builder().id(1L).reference(1L).build();
        CaseData caseDataProcessed2 = CaseData.builder().id(2L).reference(2L).build();
        CaseData caseDataFailed3 = CaseData.builder().id(3L).reference(3L).build();
        CaseData caseDataFailed4 = CaseData.builder().id(4L).reference(4L).build();

        // when
        processedCasesRecordHolder.addProcessedCase(caseDataProcessed1);
        processedCasesRecordHolder.addProcessedCase(caseDataProcessed2);
        processedCasesRecordHolder.addProcessedCase(caseDataFailed3);
        processedCasesRecordHolder.addProcessedCase(caseDataFailed4);
        processedCasesRecordHolder.addFailedToDeleteCaseRef(caseDataFailed3);
        processedCasesRecordHolder.addFailedToDeleteCaseRef(caseDataFailed4);
        processedCasesRecordHolder.clearState();

        // then
        assertThat(processedCasesRecordHolder.getSuccessfullyDeletedCases()).isEmpty();
        assertThat(processedCasesRecordHolder.getFailedToDeleteDeletedCases()).isEmpty();
    }
}
