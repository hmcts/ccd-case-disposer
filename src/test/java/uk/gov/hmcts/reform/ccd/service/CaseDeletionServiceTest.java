package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.ApplicationParameters;
import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.es.CaseDataElasticsearchOperations;
import uk.gov.hmcts.reform.ccd.fixture.CaseLinkEntityBuilder;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.CASE_DATA_YESTERDAY;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.CASE_TYPE;
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
    private ApplicationParameters parameters;

    @InjectMocks
    private CaseDeletionService underTest;

    @Test
    void testGetExpiredCases() {
        final List<String> deletableCaseTypes = emptyList();
        final List<CaseDataEntity> expiredCases = List.of(CASE_DATA_YESTERDAY);
        doReturn(deletableCaseTypes).when(parameters).getDeletableCaseTypes();
        doReturn(expiredCases).when(caseDataRepository).findExpiredCases(deletableCaseTypes);

        final List<CaseDataEntity> actualExpiredCases = underTest.getExpiredCases();

        assertThat(actualExpiredCases)
            .isNotEmpty()
            .hasSize(1);
        verify(caseDataRepository).findExpiredCases(deletableCaseTypes);
    }

    @Test
    void testDeleteExpiredCases() {
        final String expectedIndex = String.format(INDEX_NAME_PATTERN, CASE_DATA_YESTERDAY.getCaseType());

        doNothing().when(caseEventRepository).deleteByCaseDataId(anyLong());
        doNothing().when(caseDataRepository).deleteById(anyLong());
        doNothing().when(caseDataElasticsearchOperations).deleteByReference(anyString(), anyLong());
        doReturn(INDEX_NAME_PATTERN).when(parameters).getCasesIndexNamePattern();

        underTest.deleteCase(CASE_DATA_YESTERDAY);

        verify(caseEventRepository).deleteByCaseDataId(anyLong());
        verify(caseDataRepository).deleteById(CASE_DATA_YESTERDAY.getId());
        verify(caseDataElasticsearchOperations).deleteByReference(expectedIndex, CASE_DATA_YESTERDAY.getReference());
    }

    @Test
    void testGetLinkedCases() {
        final List<CaseLinkEntity> caseLinks = List.of(
            new CaseLinkEntityBuilder(CASE_DATA_YESTERDAY.getId(), CASE_TYPE, 10L).build(),
            new CaseLinkEntityBuilder(CASE_DATA_YESTERDAY.getId(), CASE_TYPE, 11L).build()
        );
        doReturn(caseLinks).when(caseLinkRepository).findByCaseId(CASE_DATA_YESTERDAY.getId());

        final Map<CaseDataEntity, List<CaseLinkEntity>> linkedCases = underTest.getLinkedCases(CASE_DATA_YESTERDAY);

        assertThat(linkedCases)
            .isNotEmpty()
            .satisfies(result -> assertThat(result).containsValues(caseLinks));
    }
}
