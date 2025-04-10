package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.fixture.CaseLinkEntityBuilder;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_ENTITY2_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_ENTITY_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_ENTITY_WITH_TODAY_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE_SIMULATION;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_ENTITY_10;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_ENTITY_11;

@ExtendWith(MockitoExtension.class)
class CaseFamilyServiceTest {
    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private CaseLinkRepository caseLinkRepository;

    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private CaseFamilyService caseFamilyService;

    @Test
    @DisplayName("Get case family when two cases are linked to one and the same case.")
    void testGetCaseFamiliesScenario1() {
        final List<CaseDataEntity> expiredCases = List.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL, LINKED_CASE_ENTITY_10);
        final CaseLinkEntity caseLink1to10 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L).build();
        final CaseLinkEntity caseLink2to10 = new CaseLinkEntityBuilder(2L, DELETABLE_CASE_TYPE, 10L).build();

        doReturn(List.of(DELETABLE_CASE_TYPE, DELETABLE_CASE_TYPE_SIMULATION))
            .when(parameterResolver).getAllDeletableCaseTypes();
        doReturn(expiredCases).when(caseDataRepository).findExpiredCases(anyList());
        doReturn(List.of(caseLink1to10, caseLink2to10)).when(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);

        List<CaseDataEntity> caseEntities = new ArrayList<>(expiredCases);
        caseEntities.add(DELETABLE_CASE_ENTITY_WITH_TODAY_TTL);
        doReturn(caseEntities).when(caseDataRepository).findAllById(anySet());

        final List<CaseFamily> caseFamilies = caseFamilyService.getCaseFamilies();

        assertThat(caseFamilies)
                .isNotEmpty()
                .hasSize(1)
                .satisfies(items -> {
                    final CaseFamily caseFamily1 = items.getFirst();
                    List<Long> ids = caseFamily1.linkedCases().stream().map(CaseData::id).toList();
                    assertThat(ids).containsExactlyInAnyOrder(1L, 2L, 10L);
                });
    }

    @Test
    @DisplayName("Get cases family when one case is linked to two other cases.")
    void testGetCaseFamiliesScenario2() {
        final List<CaseDataEntity> expiredCases = List.of(
                DELETABLE_CASE_ENTITY_WITH_PAST_TTL,
                LINKED_CASE_ENTITY_10,
                LINKED_CASE_ENTITY_11
        );
        final CaseLinkEntity caseLink10 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L).build();
        final CaseLinkEntity caseLink11 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 11L).build();

        doReturn(List.of(DELETABLE_CASE_TYPE, DELETABLE_CASE_TYPE_SIMULATION))
                .when(parameterResolver).getAllDeletableCaseTypes();
        doReturn(expiredCases).when(caseDataRepository).findExpiredCases(anyList());
        doReturn(List.of(caseLink10, caseLink11)).when(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        doReturn(expiredCases).when(caseDataRepository).findAllById(anySet());

        final List<CaseFamily> caseFamilies = caseFamilyService.getCaseFamilies();

        assertThat(caseFamilies)
                .isNotEmpty()
                .singleElement()
                .satisfies(item -> {
                    List<CaseData> cases = item.linkedCases();
                    List<Long> ids = cases.stream().map(CaseData::id).toList();
                    assertThat(ids).containsExactlyInAnyOrder(1L, 10L, 11L);
                });
    }

    @Test
    @DisplayName("Get cases when a linked case is linked to another case.")
    void testGetCaseFamiliesScenario3() {
        final List<CaseDataEntity> expiredCases = List.of(
            DELETABLE_CASE_ENTITY_WITH_PAST_TTL
        );

        final List<CaseDataEntity> linkedCases = List.of(
                DELETABLE_CASE_ENTITY_WITH_PAST_TTL,
                DELETABLE_CASE_ENTITY2_WITH_PAST_TTL,
                LINKED_CASE_ENTITY_10,
                LINKED_CASE_ENTITY_11
        );
        final CaseLinkEntity caseLink1to10 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L).build();
        final CaseLinkEntity caseLink1000to10 = new CaseLinkEntityBuilder(1000L, DELETABLE_CASE_TYPE, 10L).build();
        final CaseLinkEntity caseLink10to11 = new CaseLinkEntityBuilder(10L, DELETABLE_CASE_TYPE, 11L).build();

        doReturn(List.of(DELETABLE_CASE_TYPE, DELETABLE_CASE_TYPE_SIMULATION))
                .when(parameterResolver).getAllDeletableCaseTypes();
        doReturn(expiredCases).when(caseDataRepository).findExpiredCases(anyList());
        doReturn(List.of(caseLink1to10)).when(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        doReturn(List.of(caseLink1to10, caseLink1000to10, caseLink10to11))
            .when(caseLinkRepository).findByCaseIdOrLinkedCaseId(10L);

        doReturn(linkedCases).when(caseDataRepository).findAllById(anySet());

        final List<CaseFamily> caseFamilies = caseFamilyService.getCaseFamilies();

        assertThat(caseFamilies)
                .isNotEmpty()
                .hasSize(1)
                .satisfies(items -> {
                    List<Long> ids = items.getFirst().linkedCases().stream().map(CaseData::id).toList();
                    assertThat(ids).containsExactlyInAnyOrder(1L, 10L, 1000L, 11L);
                });
    }


    //@Test
    //@DisplayName("Get root nodes throws an exception when a case cannot be found by id.")
    //void testGetRootNodesScenario4() {
    //    final List<CaseDataEntity> expiredCases = List.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL, LINKED_CASE_ENTITY_10);
    //    final CaseLinkEntity caseLink = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L)
    //            .build();

    //    doReturn(List.of(DELETABLE_CASE_TYPE, DELETABLE_CASE_TYPE_SIMULATION))
    //            .when(parameterResolver).getAllDeletableCaseTypes();
    //    doReturn(expiredCases).when(caseDataRepository).findExpiredCases(anyList());
    //    doReturn(emptyList()).when(caseLinkRepository).findByLinkedCaseId(1L);
    //    doReturn(List.of(caseLink)).when(caseLinkRepository).findByLinkedCaseId(10L);
    //    doReturn(Optional.empty()).when(caseDataRepository).findById(1L);

    //    assertThatExceptionOfType(CaseDataNotFound.class)
    //            .isThrownBy(() -> caseFamilyService.getRootNodes())
    //            .withMessage("Case data for case_id=1 is not found");
    //}

    @Test
    @DisplayName("Get case family when the family consists of a single case with no linked cases.")
    void testGetCaseFamiliesScenario4() {
        doReturn(List.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL.getCaseType()))
            .when(parameterResolver).getAllDeletableCaseTypes();

        doReturn(List.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findExpiredCases(anyList());
        doReturn(emptyList()).when(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        doReturn(List.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findAllById(Set.of(1L));

        final CaseFamily caseFamily = caseFamilyService.getCaseFamilies().getFirst();

        assertThat(caseFamily)
                .isNotNull()
                .satisfies(item -> {
                    assertThat(item.linkedCases()).hasSize(1);
                    assertThat(item.linkedCases().getFirst().id()).isEqualTo(1L);
                });
    }

    /*@Test
    @DisplayName("Get case family when the family consists of a root node with one other family member.")
    void testBuildCaseFamilyScenario2() {
        final CaseLinkEntity caseLink10 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L)
                .build();

        doReturn(List.of(caseLink10)).when(caseLinkRepository).findByCaseId(1L);
        doReturn(emptyList()).when(caseLinkRepository).findByCaseId(10L);
        doReturn(Optional.of(LINKED_CASE_ENTITY_10)).when(caseDataRepository).findById(10L);

        final CaseFamily caseFamily = caseFamilyService.buildCaseFamily(DELETABLE_CASE_ENTITY_WITH_PAST_TTL);

        assertThat(caseFamily)
                .isNotNull()
                .satisfies(item -> {
                    assertThat(item.getRootCase().getId()).isEqualTo(1L);
                    assertThat(item.linkedCases())
                            .singleElement()
                            .satisfies(member -> {
                                assertThat(member.id()).isEqualTo(10L);
                            });
                });
    }

    @Test
    @DisplayName("Get case family when the family consists of a root node with multiple family members.")
    void testBuildCaseFamilyScenario3() {
        final CaseLinkEntity caseLink10 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L)
                .build();
        final CaseLinkEntity caseLink11 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 11L)
                .build();
        doReturn(List.of(caseLink10, caseLink11)).when(caseLinkRepository).findByCaseId(1L);
        doReturn(emptyList()).when(caseLinkRepository).findByCaseId(10L);
        doReturn(emptyList()).when(caseLinkRepository).findByCaseId(11L);
        doReturn(Optional.of(LINKED_CASE_ENTITY_10)).when(caseDataRepository).findById(10L);
        doReturn(Optional.of(LINKED_CASE_ENTITY_11)).when(caseDataRepository).findById(11L);

        final CaseFamily caseFamily = caseFamilyService.buildCaseFamily(DELETABLE_CASE_ENTITY_WITH_PAST_TTL);

        assertThat(caseFamily)
                .isNotNull()
                .satisfies(item -> {
                    assertThat(item.getRootCase().getId()).isEqualTo(1L);
                    assertThat(item.linkedCases())
                            .hasSize(2)
                            .satisfies(members -> {
                                final CaseData member1 = members.get(0);
                                final CaseData member2 = members.get(1);

                                assertThat(member1.id()).isEqualTo(10L);
                                assertThat(member2.id()).isEqualTo(11L);
                            });
                });
    }

    @Test
    @DisplayName("Get case family when the family consists of a root node with multi-linked family members.")
    void testBuildCaseFamilyScenario4() {
        final CaseLinkEntity caseLink10 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L)
                .build();
        final CaseLinkEntity caseLink11_1 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 11L)
                .build();
        final CaseLinkEntity caseLink11_10 = new CaseLinkEntityBuilder(10L, DELETABLE_CASE_TYPE, 11L)
                .build();
        final CaseLinkEntity caseLink100 = new CaseLinkEntityBuilder(10L, DELETABLE_CASE_TYPE, 100L)
                .build();
        doReturn(List.of(caseLink10, caseLink11_1)).when(caseLinkRepository).findByCaseId(1L);
        doReturn(List.of(caseLink11_10, caseLink100)).when(caseLinkRepository).findByCaseId(10L);
        doReturn(emptyList()).when(caseLinkRepository).findByCaseId(11L);
        doReturn(emptyList()).when(caseLinkRepository).findByCaseId(100L);
        doReturn(Optional.of(LINKED_CASE_ENTITY_10)).when(caseDataRepository).findById(10L);
        doReturn(Optional.of(LINKED_CASE_ENTITY_11)).when(caseDataRepository).findById(11L);
        doReturn(Optional.of(LINKED_CASE_ENTITY_100)).when(caseDataRepository).findById(100L);

        final CaseFamily caseFamily = caseFamilyService.buildCaseFamily(DELETABLE_CASE_ENTITY_WITH_PAST_TTL);

        assertThat(caseFamily)
                .isNotNull()
                .satisfies(item -> {
                    assertThat(item.getRootCase().getId()).isEqualTo(1L);
                    assertThat(item.linkedCases())
                            .hasSize(3)
                            .satisfies(members -> {
                                final CaseData member1 = members.get(0);
                                final CaseData member2 = members.get(1);
                                final CaseData member3 = members.get(2);

                                assertThat(member1.id()).isEqualTo(10L);
                                assertThat(member2.id()).isEqualTo(11L);
                                assertThat(member3.id()).isEqualTo(100L);
                            });
                });
    }

    @Test
    @DisplayName("Get case families.")
    void testGetCaseFamilies() {
        final List<CaseDataEntity> expiredCases = List.of(
                DELETABLE_CASE_ENTITY_WITH_PAST_TTL,
                DELETABLE_CASE_ENTITY2_WITH_PAST_TTL,
                LINKED_CASE_ENTITY_10,
                LINKED_CASE_ENTITY_11
        );

        final CaseLinkEntity caseLink10_1 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L)
                .build();
        final CaseLinkEntity caseLink10_2 = new CaseLinkEntityBuilder(2L, DELETABLE_CASE_TYPE, 10L)
                .build();
        final CaseLinkEntity caseLink11_1 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 11L)
                .build();
        final CaseLinkEntity caseLink11_10 = new CaseLinkEntityBuilder(10L, DELETABLE_CASE_TYPE, 11L)
                .build();
        final CaseLinkEntity caseLink100 = new CaseLinkEntityBuilder(10L, DELETABLE_CASE_TYPE, 100L)
                .build();

        doReturn(List.of(DELETABLE_CASE_TYPE, DELETABLE_CASE_TYPE_SIMULATION))
                .when(parameterResolver).getAllDeletableCaseTypes();
        doReturn(expiredCases).when(caseDataRepository).findExpiredCases(anyList());
        doReturn(emptyList()).when(caseLinkRepository).findByLinkedCaseId(1L);
        doReturn(emptyList()).when(caseLinkRepository).findByLinkedCaseId(2L);
        doReturn(emptyList()).when(caseLinkRepository).findByLinkedCaseId(1000L);
        doReturn(List.of(caseLink10_1, caseLink10_2)).when(caseLinkRepository).findByLinkedCaseId(10L);
        doReturn(List.of(caseLink11_1, caseLink11_10)).when(caseLinkRepository).findByLinkedCaseId(11L);

        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);
        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_TODAY_TTL)).when(caseDataRepository).findById(2L);

        doReturn(List.of(caseLink10_1, caseLink11_1)).when(caseLinkRepository).findByCaseId(1L);
        doReturn(emptyList()).when(caseLinkRepository).findByCaseId(1000L);
        doReturn(List.of(caseLink10_2)).when(caseLinkRepository).findByCaseId(2L);
        doReturn(List.of(caseLink11_10, caseLink100)).when(caseLinkRepository).findByCaseId(10L);
        doReturn(emptyList()).when(caseLinkRepository).findByCaseId(11L);
        doReturn(emptyList()).when(caseLinkRepository).findByCaseId(100L);

        doReturn(Optional.of(LINKED_CASE_ENTITY_10)).when(caseDataRepository).findById(10L);
        doReturn(Optional.of(LINKED_CASE_ENTITY_11)).when(caseDataRepository).findById(11L);
        doReturn(Optional.of(LINKED_CASE_ENTITY_100)).when(caseDataRepository).findById(100L);

        final List<CaseFamily> caseFamilies = caseFamilyService.getCaseFamilies();

        assertThat(caseFamilies)
                .isNotEmpty()
                .hasSize(3)
                .satisfies(items -> {
                    final CaseFamily caseFamily1 = items.get(0);
                    final List<Long> familyMembers1 = caseFamily1.linkedCases().stream()
                            .map(CaseData::id)
                            .collect(Collectors.toUnmodifiableList());

                    final CaseFamily caseFamily2 = items.get(1);
                    final List<Long> familyMembers2 = caseFamily2.linkedCases().stream()
                            .map(CaseData::id)
                            .collect(Collectors.toUnmodifiableList());

                    final CaseFamily caseFamily3 = items.get(2);
                    final List<Long> familyMembers3 = caseFamily3.linkedCases().stream()
                            .map(CaseData::id)
                            .collect(Collectors.toUnmodifiableList());

                    assertThat(caseFamily1.getRootCase().getId()).isEqualTo(1L);
                    assertThat(familyMembers1).hasSize(3).hasSameElementsAs(List.of(10L, 11L, 100L));

                    assertThat(caseFamily2.getRootCase().getId()).isEqualTo(2L);
                    assertThat(familyMembers2).hasSize(3).hasSameElementsAs(List.of(10L, 11L, 100L));

                    assertThat(caseFamily3.getRootCase().getId()).isEqualTo(1000L);
                    assertThat(familyMembers3).isEmpty();
                });
    }*/

    @Test
    @DisplayName("Get case families when cases are cyclically linked.")
    void testGetCaseFamiliesScenario7() {
        final List<CaseDataEntity> expiredCases = List.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL);

        final CaseLinkEntity caseLink1to10 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L).build();
        final CaseLinkEntity caseLink10to1 = new CaseLinkEntityBuilder(10L, DELETABLE_CASE_TYPE, 1L).build();

        doReturn(List.of(DELETABLE_CASE_TYPE)).when(parameterResolver).getAllDeletableCaseTypes();
        doReturn(expiredCases).when(caseDataRepository).findExpiredCases(anyList());
        doReturn(List.of(caseLink1to10, caseLink10to1)).when(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        doReturn(List.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL, LINKED_CASE_ENTITY_10))
            .when(caseDataRepository).findAllById(Set.of(1L, 10L));

        final CaseFamily caseFamily = caseFamilyService.getCaseFamilies().getFirst();

        assertThat(caseFamily)
                .satisfies(items -> {
                    List<CaseData> cases = items.linkedCases();
                    List<Long> ids = cases.stream().map(CaseData::id).toList();
                    assertThat(ids).containsExactlyInAnyOrder(1L, 10L);
                });
    }

    @Test
    @DisplayName("Get case families when a case is linked back to itself.")
    void testGetCaseFamiliesScenario8() {
        final List<CaseDataEntity> expiredCases = List.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL);

        final CaseLinkEntity caseLink1to1 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 1L).build();

        doReturn(List.of(DELETABLE_CASE_TYPE)).when(parameterResolver).getAllDeletableCaseTypes();
        doReturn(expiredCases).when(caseDataRepository).findExpiredCases(anyList());
        doReturn(List.of(caseLink1to1)).when(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        doReturn(List.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findAllById(Set.of(1L));

        final CaseFamily caseFamily = caseFamilyService.getCaseFamilies().getFirst();

        assertThat(caseFamily)
                .satisfies(family -> {
                    final List<Long> familyMembers = caseFamily.linkedCases().stream()
                            .map(CaseData::id)
                            .toList();
                    assertThat(familyMembers).singleElement().isEqualTo(1L);
                });
    }

    /*@Test
    void testThatNullPointerExceptionIsRaisedWhenNullCaseDataEntityIsPassedToBuildCaseFamily() {
        // GIVEN
        final CaseDataEntity caseNode = null;

        // WHEN/THEN
        assertThatNullPointerException().isThrownBy(() -> caseFamilyService.buildCaseFamily(caseNode));
    }*/

}
