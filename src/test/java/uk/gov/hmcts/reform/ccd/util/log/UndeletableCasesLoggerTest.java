package uk.gov.hmcts.reform.ccd.util.log;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class UndeletableCasesLoggerTest {

    @Mock
    ProcessedCasesRecordHolder casesRecordHolder;

    @InjectMocks
    UndeletableCasesLogger undeletableCasesLogger;

    @Test
    void logUndeletableCasesShouldNotModifyPassedParameters() {
        List<CaseFamily> caseFamilies = List.of(
            new CaseFamily(makeCase(1),
            List.of(makeCase(2), makeCase(3))));
        List<CaseFamily> deletableCaseFamilies = List.of(
            new CaseFamily(makeCase(1),
            List.of(makeCase(2), makeCase(3))));

        undeletableCasesLogger.logUndeletableCases(caseFamilies, deletableCaseFamilies);

        assertThat(caseFamilies)
            .containsExactly(new CaseFamily(makeCase(1), List.of(makeCase(2), makeCase(3))));
        assertThat(deletableCaseFamilies)
            .containsExactly(new CaseFamily(makeCase(1), List.of(makeCase(2), makeCase(3))));
    }

    @Test
    void logUndeletableCasesShouldLogNonDeletableCases() {
        List<CaseFamily> caseFamilies = List.of(
            new CaseFamily(makeCase(1),
            List.of(makeCase(2), makeCase(3))));
        List<CaseFamily> deletableCaseFamilies = List.of();

        undeletableCasesLogger.logUndeletableCases(caseFamilies, deletableCaseFamilies);

        verify(casesRecordHolder, times(3)).addNonDeletableCase(any(CaseData.class));
    }

    @Test
    void logUndeletableCasesShouldLogMessage(CapturedOutput output) {
        List<CaseFamily> caseFamilies = List.of(
            new CaseFamily(makeCase(1),
                           List.of(makeCase(2), makeCase(3, LocalDate.now()))));
        List<CaseFamily> deletableCaseFamilies = List.of();

        undeletableCasesLogger.logUndeletableCases(caseFamilies, deletableCaseFamilies);
        assertThat(output.getOut()).contains("Not deleting case ref: 1 due to existing link to case(s)");
    }

    @Test
    void logUndeletableCasesWithDuplicateFamilyShouldNotCauseProblems() {
        List<CaseFamily> caseFamilies = List.of(
            new CaseFamily(makeCase(1), List.of(makeCase(2), makeCase(3))),
            // duplicate families, linked cases discarded (only from the first family used)
            new CaseFamily(makeCase(1), List.of(makeCase(3), makeCase(4))),
            new CaseFamily(makeCase(1), List.of(makeCase(5), makeCase(6)))
        );
        List<CaseFamily> deletableCaseFamilies = List.of();

        undeletableCasesLogger.logUndeletableCases(caseFamilies, deletableCaseFamilies);

        verify(casesRecordHolder, times(1)).addNonDeletableCase(makeCase(1));
        verify(casesRecordHolder, times(1)).addNonDeletableCase(makeCase(2));
        verify(casesRecordHolder, times(1)).addNonDeletableCase(makeCase(3));
        verifyNoMoreInteractions(casesRecordHolder);
    }

    private CaseData makeCase(Integer id, LocalDate resolvedTtl) {
        return CaseData.builder()
            .id(id.longValue())
            .reference(id.longValue())
            .resolvedTtl(resolvedTtl)
            .build();
    }

    private CaseData makeCase(Integer id) {
        return makeCase(id, LocalDate.MIN);
    }
}
