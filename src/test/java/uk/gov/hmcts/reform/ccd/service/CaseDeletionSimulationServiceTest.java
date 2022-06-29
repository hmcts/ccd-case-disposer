package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.util.log.CaseDataViewBuilder;
import uk.gov.hmcts.reform.ccd.util.log.CaseDataViewHolder;
import uk.gov.hmcts.reform.ccd.util.log.TableTextBuilder;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY_SIMULATION;

@ExtendWith(MockitoExtension.class)
class CaseDeletionSimulationServiceTest {

    @Spy
    private TableTextBuilder tableTextBuilder;
    @Spy
    private CaseDataViewBuilder caseDataViewBuilder;
    @Spy
    private CaseDataViewHolder caseDataViewHolder;

    @InjectMocks
    private CaseDeletionSimulationService caseDeletionSimulationService;

    @Test
    void shouldLogCaseFamilies() {

        final List<CaseFamily> deletableCaseFamily = asList(DELETABLE_CASE_FAMILY);

        final List<CaseFamily> simulationCaseFamily = asList(DELETABLE_CASE_FAMILY_SIMULATION);

        caseDeletionSimulationService.logCaseFamilies(deletableCaseFamily, simulationCaseFamily);

        verify(tableTextBuilder, times(1)).buildTextTable(anyList());
        verify(caseDataViewHolder, times(1)).setUpData(anyList());

        verify(caseDataViewBuilder, times(2)).buildCaseDataViewList(anyList(), anyList(), anyBoolean());

        assertThat(caseDataViewHolder.getSimulatedCaseIds())
                .isNotNull()
                .containsExactlyInAnyOrder(30L, 31L);

    }
}