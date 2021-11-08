package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.ApplicationParameters;
import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.es.CaseDataElasticsearchOperations;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.INDEX_NAME_PATTERN;

@ExtendWith(MockitoExtension.class)
class CaseDeletionServiceTest {
    @Mock
    private CaseDataRepository caseDataRepository;
    @Mock
    private CaseEventRepository caseEventRepository;
    @Mock
    private CaseDataElasticsearchOperations caseDataElasticsearchOperations;
    @Mock
    private ApplicationParameters parameters;

    @InjectMocks
    private CaseDeletionService underTest;

    @Test
    void testDeleteExpiredCases() {
        final String expectedIndex = String.format(INDEX_NAME_PATTERN, DELETABLE_CASE_WITH_PAST_TTL.getCaseType());

        doNothing().when(caseEventRepository).deleteByCaseDataId(anyLong());
        doNothing().when(caseDataRepository).deleteById(anyLong());
        doNothing().when(caseDataElasticsearchOperations).deleteByReference(anyString(), anyLong());
        doReturn(INDEX_NAME_PATTERN).when(parameters).getCasesIndexNamePattern();

        underTest.deleteCase(DELETABLE_CASE_WITH_PAST_TTL);

        verify(caseEventRepository).deleteByCaseDataId(anyLong());
        verify(caseDataRepository).deleteById(DELETABLE_CASE_WITH_PAST_TTL.getId());
        verify(caseDataElasticsearchOperations)
            .deleteByReference(expectedIndex, DELETABLE_CASE_WITH_PAST_TTL.getReference());
    }
}
