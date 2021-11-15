package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.fixture.CaseLinkEntityBuilder;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_WITH_FUTURE_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_WITH_TODAY_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_10;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_100;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_101;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_11;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.NON_DELETABLE_CASE_WITH_TODAY_TTL;

@ExtendWith(MockitoExtension.class)
class CaseFinderServiceTest {
    @Mock
    private CaseDataRepository caseDataRepository;
    @Mock
    private CaseLinkRepository caseLinkRepository;
    @Mock
    private ParameterResolver parameterResolver;
    @InjectMocks
    private CaseFinderService underTest;

    private final List<String> deletableCaseTypes = List.of(DELETABLE_CASE_TYPE);

    @Test
    void testGetExpiredCases() {
        final List<CaseDataEntity> expiredCases = List.of(DELETABLE_CASE_WITH_PAST_TTL);
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();
        doReturn(expiredCases).when(caseDataRepository).findExpiredCases(deletableCaseTypes);

        final List<CaseDataEntity> actualExpiredCases = underTest.getExpiredCases();

        assertThat(actualExpiredCases)
            .isNotEmpty()
            .hasSize(1);
        verify(parameterResolver).getDeletableCaseTypes();
        verify(caseDataRepository).findExpiredCases(deletableCaseTypes);
    }

    @Test
    void testGetLinkedCases() {
        final List<CaseLinkEntity> caseLinks = List.of(
            new CaseLinkEntityBuilder(10L, DELETABLE_CASE_TYPE, DELETABLE_CASE_WITH_PAST_TTL.getId()).build(),
            new CaseLinkEntityBuilder(11L, DELETABLE_CASE_TYPE, DELETABLE_CASE_WITH_PAST_TTL.getId()).build()
        );
        final List<CaseDataEntity> expectedLinkedCases = List.of(LINKED_CASE_DATA_10, LINKED_CASE_DATA_11);
        doReturn(caseLinks).when(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_PAST_TTL.getId());
        doReturn(expectedLinkedCases).when(caseDataRepository).findAllById(anyList());

        final List<CaseDataEntity> linkedCases = underTest.getLinkedCases(DELETABLE_CASE_WITH_PAST_TTL);

        assertThat(linkedCases)
            .isNotEmpty()
            .hasSameElementsAs(expectedLinkedCases);

        verify(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_PAST_TTL.getId());
        verify(caseDataRepository).findAllById(anyList());
    }

    @Test
    void testGetLinkedCasesWhenNoCasesAreLinked() {
        doReturn(emptyList()).when(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_PAST_TTL.getId());
        doReturn(emptyList()).when(caseDataRepository).findAllById(anyList());

        final List<CaseDataEntity> linkedCases = underTest.getLinkedCases(DELETABLE_CASE_WITH_PAST_TTL);

        assertThat(linkedCases)
            .isEmpty();

        verify(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_PAST_TTL.getId());
        verify(caseDataRepository).findAllById(emptyList());
    }

    @Test
    void testIsCaseDueDeletionWhenCaseIsOfDeletableTypeWithTtlInThePast() {
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final Boolean result = underTest.isCaseDueDeletion(DELETABLE_CASE_WITH_PAST_TTL);

        assertThat(result).isTrue();
        verify(parameterResolver).getDeletableCaseTypes();
    }

    @Test
    void testIsCaseDueDeletionWhenCaseIsOfDeletableTypeAndTtlIsToday() {
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final Boolean result = underTest.isCaseDueDeletion(DELETABLE_CASE_WITH_TODAY_TTL);

        assertThat(result).isFalse();
        verify(parameterResolver).getDeletableCaseTypes();
    }

    @Test
    void testIsCaseDueDeletionWhenCaseIsOfDeletableTypeWithFutureTtl() {
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final Boolean result = underTest.isCaseDueDeletion(DELETABLE_CASE_WITH_FUTURE_TTL);

        assertThat(result).isFalse();
        verify(parameterResolver).getDeletableCaseTypes();
    }

    @Test
    void testIsCaseDueDeletionWhenCaseIsNotOfDeletableType() {
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final Boolean result = underTest.isCaseDueDeletion(NON_DELETABLE_CASE_WITH_TODAY_TTL);

        assertThat(result).isFalse();
        verify(parameterResolver).getDeletableCaseTypes();
    }

    @Test
    void testIsAllDueDeletionWhenListIsEmpty() {
        final Boolean result = underTest.isAllDueDeletion(emptyList());

        assertThat(result).isTrue();
    }

    @Test
    void testIsAllDueDeletionWhenAllCasesAreDeletable() {
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final Boolean result = underTest.isAllDueDeletion(List.of(
            LINKED_CASE_DATA_10,
            LINKED_CASE_DATA_11
        ));

        assertThat(result).isTrue();
        verify(parameterResolver, times(2)).getDeletableCaseTypes();
    }

    @Test
    void testIsAllDueDeletionWhenNonDeletableCaseTypeIsIncluded() {
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final Boolean result = underTest.isAllDueDeletion(List.of(
            LINKED_CASE_DATA_100,
            LINKED_CASE_DATA_11
        ));

        assertThat(result).isFalse();
        verify(parameterResolver).getDeletableCaseTypes();
    }

    @Test
    void testIsAllDueDeletionWhenAnUnexpiredCaseIsIncluded() {
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final Boolean result = underTest.isAllDueDeletion(List.of(
            LINKED_CASE_DATA_10,
            LINKED_CASE_DATA_101
        ));

        assertThat(result).isFalse();
        verify(parameterResolver, times(2)).getDeletableCaseTypes();
    }

    @Test
    void testFindDeletableCandidatesWithNoLinkedCases() {
        final List<CaseDataEntity> expiredCases = List.of(
            DELETABLE_CASE_WITH_PAST_TTL,
            DELETABLE_CASE_WITH_TODAY_TTL
        );
        final List<CaseLinkEntity> caseLinks = List.of(
            new CaseLinkEntityBuilder(10L, DELETABLE_CASE_TYPE, DELETABLE_CASE_WITH_PAST_TTL.getId()).build(),
            new CaseLinkEntityBuilder(101L, DELETABLE_CASE_TYPE, DELETABLE_CASE_WITH_PAST_TTL.getId()).build()
        );

        doReturn(expiredCases).when(caseDataRepository).findExpiredCases(deletableCaseTypes);
        doReturn(caseLinks).when(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_PAST_TTL.getId());
        doReturn(emptyList()).when(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_TODAY_TTL.getId());
        doReturn(List.of(LINKED_CASE_DATA_10, LINKED_CASE_DATA_101))
            .when(caseDataRepository).findAllById(List.of(10L, 101L));
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final List<CaseDataEntity> deletableCandidates = underTest.findDeletableCandidates();

        assertThat(deletableCandidates)
            .isNotEmpty()
            .hasSameElementsAs(List.of(DELETABLE_CASE_WITH_TODAY_TTL));
        verify(caseDataRepository).findExpiredCases(deletableCaseTypes);
        verify(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_PAST_TTL.getId());
        verify(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_TODAY_TTL.getId());
        verify(caseDataRepository).findAllById(List.of(10L, 101L));
        verify(parameterResolver, times(3)).getDeletableCaseTypes();
    }

    @Test
    void testFindDeletableCandidates2() {
        final List<CaseDataEntity> expiredCases = List.of(
            DELETABLE_CASE_WITH_PAST_TTL,
            DELETABLE_CASE_WITH_TODAY_TTL
        );
        final List<CaseLinkEntity> caseLinks1 = List.of(
            new CaseLinkEntityBuilder(10L, DELETABLE_CASE_TYPE, DELETABLE_CASE_WITH_PAST_TTL.getId()).build(),
            new CaseLinkEntityBuilder(101L, DELETABLE_CASE_TYPE, DELETABLE_CASE_WITH_PAST_TTL.getId()).build()
        );
        final List<CaseLinkEntity> caseLinks2 = List.of(
            new CaseLinkEntityBuilder(10L, DELETABLE_CASE_TYPE, DELETABLE_CASE_WITH_TODAY_TTL.getId()).build(),
            new CaseLinkEntityBuilder(11L, DELETABLE_CASE_TYPE, DELETABLE_CASE_WITH_TODAY_TTL.getId()).build()
        );

        doReturn(expiredCases).when(caseDataRepository).findExpiredCases(deletableCaseTypes);
        doReturn(caseLinks1).when(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_PAST_TTL.getId());
        doReturn(caseLinks2).when(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_TODAY_TTL.getId());
        doReturn(List.of(LINKED_CASE_DATA_10, LINKED_CASE_DATA_101))
            .when(caseDataRepository).findAllById(List.of(10L, 101L));
        doReturn(List.of(LINKED_CASE_DATA_10, LINKED_CASE_DATA_11))
            .when(caseDataRepository).findAllById(List.of(10L, 11L));
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final List<CaseDataEntity> deletableCandidates = underTest.findDeletableCandidates();

        assertThat(deletableCandidates)
            .isNotEmpty()
            .hasSameElementsAs(List.of(DELETABLE_CASE_WITH_TODAY_TTL, LINKED_CASE_DATA_10, LINKED_CASE_DATA_11));
        verify(caseDataRepository).findExpiredCases(deletableCaseTypes);
        verify(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_PAST_TTL.getId());
        verify(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_TODAY_TTL.getId());
        verify(caseDataRepository).findAllById(List.of(10L, 101L));
        verify(caseDataRepository).findAllById(List.of(10L, 11L));
        verify(parameterResolver, times(5)).getDeletableCaseTypes();
    }

}
