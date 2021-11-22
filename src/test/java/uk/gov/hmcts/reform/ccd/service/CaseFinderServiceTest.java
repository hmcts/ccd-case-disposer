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
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.RetentionStatus;
import uk.gov.hmcts.reform.ccd.fixture.CaseLinkEntityBuilder;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE2_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_WITH_FUTURE_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_WITH_TODAY_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_10;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_100;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_101;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_11;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_12;
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
    void testIsAllDueDeletionWhenLinkedCasesListIsEmpty() {
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final Boolean result = underTest.isAllDueDeletion(DELETABLE_CASE_WITH_PAST_TTL, emptyList());

        assertThat(result).isTrue();
        verify(parameterResolver).getDeletableCaseTypes();
    }

    @Test
    void testIsAllDueDeletionWhenLinkedCasesListIsEmpty2() {
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final Boolean result = underTest.isAllDueDeletion(NON_DELETABLE_CASE_WITH_TODAY_TTL, emptyList());

        assertThat(result).isFalse();
        verify(parameterResolver).getDeletableCaseTypes();
    }

    @Test
    void testIsAllDueDeletionWhenAllCasesAreDeletable() {
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final Boolean result = underTest.isAllDueDeletion(DELETABLE_CASE_WITH_PAST_TTL,
                                                          List.of(LINKED_CASE_DATA_10, LINKED_CASE_DATA_11));

        assertThat(result).isTrue();
        verify(parameterResolver, atLeastOnce()).getDeletableCaseTypes();
    }

    @Test
    void testIsAllDueDeletionWhenNonDeletableCaseTypeIsIncluded() {
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final Boolean result = underTest.isAllDueDeletion(DELETABLE_CASE_WITH_PAST_TTL,
                                                          List.of(LINKED_CASE_DATA_100, LINKED_CASE_DATA_11));

        assertThat(result).isFalse();
        verify(parameterResolver, atLeastOnce()).getDeletableCaseTypes();
    }

    @Test
    void testIsAllDueDeletionWhenAnUnexpiredCaseIsIncluded() {
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final Boolean result = underTest.isAllDueDeletion(DELETABLE_CASE_WITH_PAST_TTL,
                                                          List.of(LINKED_CASE_DATA_10, LINKED_CASE_DATA_101));

        assertThat(result).isFalse();
        verify(parameterResolver, atLeastOnce()).getDeletableCaseTypes();
    }

    @Test
    void testFindDeletableCandidatesWithNoLinkedCases() {
        final List<CaseData> expectedCaseDataList = List.of(new CaseData(
            DELETABLE_CASE_WITH_PAST_TTL.getId(),
            DELETABLE_CASE_WITH_PAST_TTL.getReference(),
            DELETABLE_CASE_WITH_PAST_TTL.getCaseType(),
            emptyList(),
            RetentionStatus.DELETE
        ));

        doReturn(List.of(DELETABLE_CASE_WITH_PAST_TTL)).when(caseDataRepository).findExpiredCases(deletableCaseTypes);
        doReturn(emptyList()).when(caseLinkRepository).findAllByLinkedCaseId(List.of(1L));
        doReturn(emptyList()).when(caseDataRepository).findAllById(emptySet());
        doReturn(emptyList()).when(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_PAST_TTL.getId());
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final List<CaseData> deletableCandidates = underTest.findCasesDueDeletion();

        assertThat(deletableCandidates)
            .isNotEmpty()
            .hasSameElementsAs(expectedCaseDataList);
        verify(caseDataRepository).findExpiredCases(deletableCaseTypes);
        verify(caseLinkRepository).findAllByLinkedCaseId(List.of(1L));
        verify(caseDataRepository).findAllById(emptySet());
        verify(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_PAST_TTL.getId());
        verify(parameterResolver, times(2)).getDeletableCaseTypes();
    }

    @Test
    void testFindDeletableCandidatesWithLinkedCases() {
        final List<CaseDataEntity> expiredCases = List.of(
            DELETABLE_CASE_WITH_PAST_TTL,
            DELETABLE_CASE2_WITH_PAST_TTL,
            LINKED_CASE_DATA_10,
            LINKED_CASE_DATA_11,
            LINKED_CASE_DATA_12
        );
        final CaseLinkEntity caseLink10 = new CaseLinkEntityBuilder(DELETABLE_CASE2_WITH_PAST_TTL.getId(),
                                                                    DELETABLE_CASE_TYPE,
                                                                    10L)
            .build();
        final CaseLinkEntity caseLink11 = new CaseLinkEntityBuilder(DELETABLE_CASE2_WITH_PAST_TTL.getId(),
                                                                    DELETABLE_CASE_TYPE,
                                                                    11L)
            .build();
        final CaseLinkEntity caseLink12 = new CaseLinkEntityBuilder(DELETABLE_CASE_WITH_PAST_TTL.getId(),
                                                                    DELETABLE_CASE_TYPE,
                                                                    12L)
            .build();
        final CaseLinkEntity caseLink101 = new CaseLinkEntityBuilder(DELETABLE_CASE_WITH_PAST_TTL.getId(),
                                                                     DELETABLE_CASE_TYPE,
                                                                     101L)
            .build();
        final List<CaseLinkEntity> caseLinks1 = List.of(caseLink12, caseLink101);
        final List<CaseLinkEntity> caseLinks2 = List.of(caseLink10, caseLink11);

        final List<CaseData> expectedCaseDataList = List.of(
            new CaseData(
                DELETABLE_CASE2_WITH_PAST_TTL.getId(),
                DELETABLE_CASE2_WITH_PAST_TTL.getReference(),
                DELETABLE_CASE2_WITH_PAST_TTL.getCaseType(),
                List.of(10L, 11L),
                RetentionStatus.DELETE
            ),
            new CaseData(
                LINKED_CASE_DATA_10.getId(),
                LINKED_CASE_DATA_10.getReference(),
                LINKED_CASE_DATA_10.getCaseType(),
                emptyList(),
                RetentionStatus.DELETE
            ),
            new CaseData(
                LINKED_CASE_DATA_11.getId(),
                LINKED_CASE_DATA_11.getReference(),
                LINKED_CASE_DATA_11.getCaseType(),
                emptyList(),
                RetentionStatus.DELETE
            )
        );

        doReturn(expiredCases).when(caseDataRepository).findExpiredCases(deletableCaseTypes);
        doReturn(List.of(caseLink10, caseLink11, caseLink12, caseLink101)).when(caseLinkRepository)
            .findAllByLinkedCaseId(List.of(1L, 1000L, 10L, 11L, 12L));
        doReturn(List.of(DELETABLE_CASE_WITH_PAST_TTL, DELETABLE_CASE2_WITH_PAST_TTL))
            .when(caseDataRepository).findAllById(Set.of(1L, 1000L));
        doReturn(caseLinks1).when(caseLinkRepository).findByCaseId(1L);
        doReturn(caseLinks2).when(caseLinkRepository).findByCaseId(1000L);
        List.of(10L, 11L, 12L).forEach(x -> doReturn(emptyList()).when(caseLinkRepository).findByCaseId(x));
        doReturn(List.of(LINKED_CASE_DATA_12, LINKED_CASE_DATA_101))
            .when(caseDataRepository).findAllById(List.of(12L, 101L));
        doReturn(List.of(LINKED_CASE_DATA_10, LINKED_CASE_DATA_11))
            .when(caseDataRepository).findAllById(List.of(10L, 11L));
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final List<CaseData> deletableCandidates = underTest.findCasesDueDeletion();

        assertThat(deletableCandidates)
            .isNotEmpty()
            .hasSameElementsAs(expectedCaseDataList);
        verify(caseDataRepository).findExpiredCases(deletableCaseTypes);
        verify(caseLinkRepository).findAllByLinkedCaseId(List.of(1L, 1000L, 10L, 11L, 12L));
        verify(caseDataRepository).findAllById(Set.of(1L, 1000L));
        verify(caseLinkRepository).findByCaseId(1L);
        verify(caseLinkRepository).findByCaseId(1000L);
        List.of(10L, 11L, 12L).forEach(x -> verify(caseLinkRepository).findByCaseId(x));
        verify(caseDataRepository).findAllById(List.of(12L, 101L));
        verify(caseDataRepository).findAllById(List.of(10L, 11L));
        verify(parameterResolver, times(10)).getDeletableCaseTypes();
    }

    @Test
    void testFindDeletableCandidatesWithLinkedCasesWhenCaseIsNotDueDeletionButLinkedCaseDueDeletion() {
        final List<CaseLinkEntity> caseLinks = List.of(
            new CaseLinkEntityBuilder(
                DELETABLE_CASE_WITH_TODAY_TTL.getId(),
                LINKED_CASE_DATA_10.getCaseType(),
                LINKED_CASE_DATA_10.getId()
            )
                .build()
        );

        doReturn(List.of(LINKED_CASE_DATA_10)).when(caseDataRepository).findExpiredCases(deletableCaseTypes);
        doReturn(caseLinks).when(caseLinkRepository).findAllByLinkedCaseId(List.of(LINKED_CASE_DATA_10.getId()));
        doReturn(List.of(DELETABLE_CASE_WITH_TODAY_TTL)).when(caseDataRepository).findAllById(Set.of(2L));
        doReturn(caseLinks).when(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_TODAY_TTL.getId());
        doReturn(List.of(LINKED_CASE_DATA_10)).when(caseDataRepository).findAllById(List.of(10L));
        doReturn(deletableCaseTypes).when(parameterResolver).getDeletableCaseTypes();

        final List<CaseData> deletableCandidates = underTest.findCasesDueDeletion();

        assertThat(deletableCandidates)
            .isEmpty();
        verify(caseDataRepository).findExpiredCases(deletableCaseTypes);
        verify(caseLinkRepository).findAllByLinkedCaseId(List.of(LINKED_CASE_DATA_10.getId()));
        verify(caseDataRepository).findAllById(Set.of(2L));
        verify(caseLinkRepository).findByCaseId(DELETABLE_CASE_WITH_TODAY_TTL.getId());
        verify(caseDataRepository).findAllById(List.of(10L));
        verify(parameterResolver, times(3)).getDeletableCaseTypes();
    }

}
