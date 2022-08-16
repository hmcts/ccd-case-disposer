package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.util.FailedToDeleteCaseFamilyHolder;
import uk.gov.hmcts.reform.ccd.util.log.CaseFamiliesFilter;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY_SIMULATION;

@ExtendWith(MockitoExtension.class)
class CaseDeletionResolverTest {

    @Mock
    private CaseFamiliesFilter caseFamiliesFilter;

    @Mock
    private CaseDeletionLoggingService caseDeletionLoggingService;

    @Mock
    private FailedToDeleteCaseFamilyHolder failedToDeleteCaseFamilyHolder;

    @InjectMocks
    private CaseDeletionResolver caseDeletionResolver;

    @Test
    void shouldSimulateCaseDeletion() {
        final List<CaseFamily> linkedFamilies = asList(DELETABLE_CASE_FAMILY_SIMULATION);

        final List<CaseFamily> filteredCases = List.of(DELETABLE_CASE_FAMILY);

        final List<List<Long>> caseRefs = List.of(List.of(2L));

        when(failedToDeleteCaseFamilyHolder.getCaseRefs()).thenReturn(caseRefs);
        when(caseFamiliesFilter.filterSuccessfulCaseFamiliesByCaseRef(linkedFamilies, caseRefs))
                .thenReturn(filteredCases);

        caseDeletionResolver.logCaseDeletion(linkedFamilies);

        verify(caseFamiliesFilter, times(1)).filterSuccessfulCaseFamiliesByCaseRef(linkedFamilies, caseRefs);
        verify(caseFamiliesFilter, times(1)).getDeletableCasesOnly(filteredCases);
        verify(caseFamiliesFilter, times(1)).geSimulationCasesOnly(filteredCases);
        verify(caseDeletionLoggingService, times(1))
                .logCaseFamilies(anyList(), anyList(), anyList());
    }
}