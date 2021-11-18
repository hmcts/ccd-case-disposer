package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkPrimaryKey;
import uk.gov.hmcts.reform.ccd.data.es.CaseDataElasticsearchOperations;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.RetentionStatus;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
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
    private CaseLinkRepository caseLinkRepository;
    @Mock
    private CaseDataElasticsearchOperations caseDataElasticsearchOperations;
    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private CaseDeletionService underTest;

    @Test
    void testDeleteCaseWithNoLinkedCases() {
        final String expectedIndex = String.format(INDEX_NAME_PATTERN, DELETABLE_CASE_WITH_PAST_TTL.getCaseType());
        final CaseData caseData = new CaseData(
            DELETABLE_CASE_WITH_PAST_TTL.getId(),
            DELETABLE_CASE_WITH_PAST_TTL.getReference(),
            DELETABLE_CASE_WITH_PAST_TTL.getCaseType(),
            emptyList(),
            RetentionStatus.DELETE
        );

        doNothing().when(caseEventRepository).deleteByCaseDataId(anyLong());
        doNothing().when(caseDataRepository).deleteById(anyLong());
        doNothing().when(caseDataElasticsearchOperations).deleteByReference(anyString(), anyLong());
        doReturn(INDEX_NAME_PATTERN).when(parameterResolver).getCasesIndexNamePattern();

        underTest.deleteCase(caseData);

        verify(caseLinkRepository, never()).deleteById(any(CaseLinkPrimaryKey.class));
        verify(caseEventRepository).deleteByCaseDataId(anyLong());
        verify(caseDataRepository).deleteById(DELETABLE_CASE_WITH_PAST_TTL.getId());
        verify(caseDataElasticsearchOperations)
            .deleteByReference(expectedIndex, DELETABLE_CASE_WITH_PAST_TTL.getReference());
    }

    @Test
    void testDeleteCaseWithLinkedCases() {
        final String expectedIndex = String.format(INDEX_NAME_PATTERN, DELETABLE_CASE_WITH_PAST_TTL.getCaseType());
        final CaseData caseData = new CaseData(
            DELETABLE_CASE_WITH_PAST_TTL.getId(),
            DELETABLE_CASE_WITH_PAST_TTL.getReference(),
            DELETABLE_CASE_WITH_PAST_TTL.getCaseType(),
            List.of(10L, 11L),
            RetentionStatus.DELETE
        );

        doNothing().when(caseEventRepository).deleteByCaseDataId(anyLong());
        doNothing().when(caseDataRepository).deleteById(anyLong());
        doNothing().when(caseLinkRepository).deleteById(any(CaseLinkPrimaryKey.class));
        doNothing().when(caseDataElasticsearchOperations).deleteByReference(anyString(), anyLong());
        doReturn(INDEX_NAME_PATTERN).when(parameterResolver).getCasesIndexNamePattern();

        underTest.deleteCase(caseData);

        verify(caseLinkRepository).deleteById(new CaseLinkPrimaryKey(DELETABLE_CASE_WITH_PAST_TTL.getId(), 10L));
        verify(caseLinkRepository).deleteById(new CaseLinkPrimaryKey(DELETABLE_CASE_WITH_PAST_TTL.getId(), 11L));
        verify(caseEventRepository).deleteByCaseDataId(anyLong());
        verify(caseDataRepository).deleteById(DELETABLE_CASE_WITH_PAST_TTL.getId());
        verify(caseDataElasticsearchOperations)
            .deleteByReference(expectedIndex, DELETABLE_CASE_WITH_PAST_TTL.getReference());
    }
}
