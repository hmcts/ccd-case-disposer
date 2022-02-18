package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkPrimaryKey;
import uk.gov.hmcts.reform.ccd.data.es.CaseDataElasticsearchOperations;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.fixture.CaseLinkEntityBuilder;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_ENTITY_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.INDEX_NAME_PATTERN;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_ENTITY_10;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_ENTITY_11;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.YESTERDAY;

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

    private static final String EXPECTED_INDEX = String.format(INDEX_NAME_PATTERN, DELETABLE_CASE_TYPE);

    @Test
    void testDeleteCaseWithNoLinkedCases() {
        final CaseData caseData = new CaseData(1L, 1L, DELETABLE_CASE_TYPE, YESTERDAY, 1L, null);
        final CaseFamily caseFamily = new CaseFamily(caseData, emptyList());

        doNothing().when(caseEventRepository).deleteByCaseDataId(anyLong());
        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(anyLong());
        doNothing().when(caseDataRepository).delete(any(CaseDataEntity.class));
        doNothing().when(caseDataElasticsearchOperations).deleteByReference(anyString(), anyLong());
        doReturn(INDEX_NAME_PATTERN).when(parameterResolver).getCasesIndexNamePattern();

        underTest.deleteCase(caseFamily);

        verify(caseLinkRepository, never()).deleteById(any(CaseLinkPrimaryKey.class));
        verify(caseEventRepository).deleteByCaseDataId(anyLong());
        verify(caseDataRepository).findById(1L);
        verify(caseDataRepository).delete(DELETABLE_CASE_ENTITY_WITH_PAST_TTL);
        verify(caseDataElasticsearchOperations).deleteByReference(EXPECTED_INDEX, 1L);
    }

    @Test
    void testDeleteCaseWithLinkedCases() {
        final CaseData caseData = new CaseData(1L, 1L, DELETABLE_CASE_TYPE, YESTERDAY, 1L, null);
        final CaseData linkedCaseData1 = new CaseData(10L, 10L, DELETABLE_CASE_TYPE, YESTERDAY, 1L, caseData);
        final CaseData linkedCaseData2 = new CaseData(11L, 11L, DELETABLE_CASE_TYPE, YESTERDAY, 1L, caseData);

        final CaseFamily caseFamily = new CaseFamily(caseData, List.of(linkedCaseData1, linkedCaseData2));

        List.of(1L, 10L, 11L)
            .forEach(caseId -> {
                doNothing().when(caseEventRepository).deleteByCaseDataId(caseId);
                    doReturn(
                        Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL),
                        Optional.of(LINKED_CASE_ENTITY_10),
                        Optional.of(LINKED_CASE_ENTITY_11)
                    ).when(caseDataRepository).findById(caseId);
                }
            );
        doNothing().when(caseDataRepository).delete(any(CaseDataEntity.class));
        doNothing().when(caseDataElasticsearchOperations).deleteByReference(anyString(), anyLong());
        doReturn(INDEX_NAME_PATTERN).when(parameterResolver).getCasesIndexNamePattern();

        underTest.deleteCase(caseFamily);

        List.of(1L, 10L, 11L).forEach(caseId -> {
            verify(caseEventRepository).deleteByCaseDataId(caseId);
            verify(caseDataRepository).findById(caseId);
        });
        verify(caseDataRepository, times(3)).delete(any(CaseDataEntity.class));
        verify(caseDataElasticsearchOperations).deleteByReference(EXPECTED_INDEX, 1L);
        verify(caseDataElasticsearchOperations).deleteByReference(EXPECTED_INDEX, 10L);
        verify(caseDataElasticsearchOperations).deleteByReference(EXPECTED_INDEX, 11L);
    }

    @Test
    void testDeleteLinkedCases() {
        final CaseData caseData = new CaseData(1L, 1L, DELETABLE_CASE_TYPE, YESTERDAY, 1L, null);
        final CaseData linkedCaseData1 = new CaseData(10L, 10L, DELETABLE_CASE_TYPE, YESTERDAY, 1L, caseData);
        final CaseData linkedCaseData2 = new CaseData(11L, 11L, DELETABLE_CASE_TYPE, YESTERDAY, 1L, caseData);

        final CaseLinkEntity caseLinkEntity1 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L).build();
        final CaseLinkEntity caseLinkEntity2 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 11L).build();

        doReturn(Optional.of(caseLinkEntity1), Optional.of(caseLinkEntity2))
            .when(caseLinkRepository).findById(any(CaseLinkPrimaryKey.class));
        doNothing().when(caseLinkRepository).delete(any(CaseLinkEntity.class));

        underTest.deleteLinkedCases(List.of(linkedCaseData1, linkedCaseData2));

        verify(caseLinkRepository).findById(new CaseLinkPrimaryKey(1L, 10L));
        verify(caseLinkRepository).findById(new CaseLinkPrimaryKey(1L, 11L));
        verify(caseLinkRepository, times(2)).delete(any(CaseLinkEntity.class));
    }
}
