package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.DisplayName;
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
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.exception.CaseDataNotFound;
import uk.gov.hmcts.reform.ccd.fixture.CaseLinkEntityBuilder;
import uk.gov.hmcts.reform.ccd.fixture.TestData;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_ENTITY2_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_ENTITY_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_ENTITY_WITH_TODAY_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_ENTITY_10;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_ENTITY_100;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_ENTITY_11;

@ExtendWith(MockitoExtension.class)
class CaseFamilyTreeServiceTest {
    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private CaseLinkRepository caseLinkRepository;

    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private CaseFamilyTreeService underTest;

    @Test
    @DisplayName("Get root nodes when two cases are linked to one and the same case.")
    void testGetRootNodesScenario1() {
        final List<CaseDataEntity> expiredCases = List.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL, LINKED_CASE_ENTITY_10);
        final CaseLinkEntity caseLink10_1 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L)
            .build();
        final CaseLinkEntity caseLink10_2 = new CaseLinkEntityBuilder(2L, DELETABLE_CASE_TYPE, 10L)
            .build();

        doReturn(List.of(TestData.DELETABLE_CASE_TYPE)).when(parameterResolver).getDeletableCaseTypes();
        doReturn(expiredCases).when(caseDataRepository).findExpiredCases(anyList());
        doReturn(emptyList()).when(caseLinkRepository).findByLinkedCaseId(1L);
        doReturn(List.of(caseLink10_1, caseLink10_2)).when(caseLinkRepository).findByLinkedCaseId(10L);
        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);
        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_TODAY_TTL)).when(caseDataRepository).findById(2L);

        final List<CaseDataEntity> parentNodes = underTest.getRootNodes();

        assertThat(parentNodes)
            .isNotEmpty()
            .hasSize(2)
            .satisfies(items -> {
                final CaseDataEntity caseDataEntity1 = items.get(0);
                final CaseDataEntity caseDataEntity2 = items.get(1);

                assertThat(caseDataEntity1.getId()).isEqualTo(1L);
                assertThat(caseDataEntity2.getId()).isEqualTo(2L);
            });
    }

    @Test
    @DisplayName("Get root nodes when one case is linked to two other cases.")
    void testGetRootNodesScenario2() {
        final List<CaseDataEntity> expiredCases = List.of(
            DELETABLE_CASE_ENTITY_WITH_PAST_TTL,
            LINKED_CASE_ENTITY_10,
            LINKED_CASE_ENTITY_11
        );
        final CaseLinkEntity caseLink10 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L)
            .build();
        final CaseLinkEntity caseLink11 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 11L)
            .build();

        doReturn(List.of(TestData.DELETABLE_CASE_TYPE)).when(parameterResolver).getDeletableCaseTypes();
        doReturn(expiredCases).when(caseDataRepository).findExpiredCases(anyList());
        doReturn(emptyList()).when(caseLinkRepository).findByLinkedCaseId(1L);
        doReturn(List.of(caseLink10)).when(caseLinkRepository).findByLinkedCaseId(10L);
        doReturn(List.of(caseLink11)).when(caseLinkRepository).findByLinkedCaseId(11L);
        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);

        final List<CaseDataEntity> parentNodes = underTest.getRootNodes();

        assertThat(parentNodes)
            .isNotEmpty()
            .singleElement()
            .satisfies(item -> assertThat(item.getId()).isEqualTo(1L));
    }

    @Test
    @DisplayName("Get root nodes when a linked case is linked to another case.")
    void testGetRootNodesScenario3() {
        final List<CaseDataEntity> expiredCases = List.of(
            DELETABLE_CASE_ENTITY_WITH_PAST_TTL,
            DELETABLE_CASE_ENTITY2_WITH_PAST_TTL,
            LINKED_CASE_ENTITY_10,
            LINKED_CASE_ENTITY_11
        );
        final CaseLinkEntity caseLink10_1 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L)
            .build();
        final CaseLinkEntity caseLink10_2 = new CaseLinkEntityBuilder(1000L, DELETABLE_CASE_TYPE, 10L)
            .build();
        final CaseLinkEntity caseLink11 = new CaseLinkEntityBuilder(10L, DELETABLE_CASE_TYPE, 11L)
            .build();

        doReturn(List.of(TestData.DELETABLE_CASE_TYPE)).when(parameterResolver).getDeletableCaseTypes();
        doReturn(expiredCases).when(caseDataRepository).findExpiredCases(anyList());
        doReturn(emptyList()).when(caseLinkRepository).findByLinkedCaseId(1L);
        doReturn(emptyList()).when(caseLinkRepository).findByLinkedCaseId(1000L);
        doReturn(List.of(caseLink10_1, caseLink10_2)).when(caseLinkRepository).findByLinkedCaseId(10L);
        doReturn(List.of(caseLink11)).when(caseLinkRepository).findByLinkedCaseId(11L);
        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);
        doReturn(Optional.of(LINKED_CASE_ENTITY_10)).when(caseDataRepository).findById(10L);
        doReturn(Optional.of(DELETABLE_CASE_ENTITY2_WITH_PAST_TTL)).when(caseDataRepository).findById(1000L);

        final List<CaseDataEntity> parentNodes = underTest.getRootNodes();

        assertThat(parentNodes)
            .isNotEmpty()
            .hasSize(2)
            .satisfies(items -> {
                final CaseDataEntity caseDataEntity1 = items.get(0);
                final CaseDataEntity caseDataEntity2 = items.get(1);

                assertThat(caseDataEntity1.getId()).isEqualTo(1L);
                assertThat(caseDataEntity2.getId()).isEqualTo(1000L);
            });
    }


    @Test
    @DisplayName("Get root nodes throws an exception when a case cannot be found by id.")
    void testGetRootNodesScenario4() {
        final List<CaseDataEntity> expiredCases = List.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL, LINKED_CASE_ENTITY_10);
        final CaseLinkEntity caseLink = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L)
            .build();

        doReturn(List.of(TestData.DELETABLE_CASE_TYPE)).when(parameterResolver).getDeletableCaseTypes();
        doReturn(expiredCases).when(caseDataRepository).findExpiredCases(anyList());
        doReturn(emptyList()).when(caseLinkRepository).findByLinkedCaseId(1L);
        doReturn(List.of(caseLink)).when(caseLinkRepository).findByLinkedCaseId(10L);
        doReturn(Optional.empty()).when(caseDataRepository).findById(1L);

        assertThatExceptionOfType(CaseDataNotFound.class)
            .isThrownBy(() -> underTest.getRootNodes())
            .withMessage("Case data for case_id=1 is not found");
    }

    @Test
    @DisplayName("Get case family when the family consists of a root node with no members.")
    void testBuildCaseFamilyScenario1() {
        doReturn(emptyList()).when(caseLinkRepository).findByCaseId(1L);

        final CaseFamily caseFamily = underTest.buildCaseFamily(DELETABLE_CASE_ENTITY_WITH_PAST_TTL);

        assertThat(caseFamily)
            .isNotNull()
            .satisfies(item -> {
                assertThat(item.getRootAncestor().getId()).isEqualTo(1L);
                assertThat(item.getFamilyMembers()).isEmpty();
            });
    }

    @Test
    @DisplayName("Get case family when the family consists of a root node with one other family member.")
    void testBuildCaseFamilyScenario2() {
        final CaseLinkEntity caseLink10 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L)
            .build();

        doReturn(List.of(caseLink10)).when(caseLinkRepository).findByCaseId(1L);
        doReturn(emptyList()).when(caseLinkRepository).findByCaseId(10L);
        doReturn(Optional.of(LINKED_CASE_ENTITY_10)).when(caseDataRepository).findById(10L);

        final CaseFamily caseFamily = underTest.buildCaseFamily(DELETABLE_CASE_ENTITY_WITH_PAST_TTL);

        assertThat(caseFamily)
            .isNotNull()
            .satisfies(item -> {
                assertThat(item.getRootAncestor().getId()).isEqualTo(1L);
                assertThat(item.getFamilyMembers())
                    .singleElement()
                    .satisfies(member -> {
                        assertThat(member.getId()).isEqualTo(10L);
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

        final CaseFamily caseFamily = underTest.buildCaseFamily(DELETABLE_CASE_ENTITY_WITH_PAST_TTL);

        assertThat(caseFamily)
            .isNotNull()
            .satisfies(item -> {
                assertThat(item.getRootAncestor().getId()).isEqualTo(1L);
                assertThat(item.getFamilyMembers())
                    .hasSize(2)
                    .satisfies(members -> {
                        final CaseData member1 = members.get(0);
                        final CaseData member2 = members.get(1);

                        assertThat(member1.getId()).isEqualTo(10L);
                        assertThat(member2.getId()).isEqualTo(11L);
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

        final CaseFamily caseFamily = underTest.buildCaseFamily(DELETABLE_CASE_ENTITY_WITH_PAST_TTL);

        assertThat(caseFamily)
            .isNotNull()
            .satisfies(item -> {
                assertThat(item.getRootAncestor().getId()).isEqualTo(1L);
                assertThat(item.getFamilyMembers())
                    .hasSize(3)
                    .satisfies(members -> {
                        final CaseData member1 = members.get(0);
                        final CaseData member2 = members.get(1);
                        final CaseData member3 = members.get(2);

                        assertThat(member1.getId()).isEqualTo(10L);
                        assertThat(member2.getId()).isEqualTo(11L);
                        assertThat(member3.getId()).isEqualTo(100L);
                    });
            });
    }

    @Test
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

        doReturn(List.of(TestData.DELETABLE_CASE_TYPE)).when(parameterResolver).getDeletableCaseTypes();
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

        final List<CaseFamily> caseFamilies = underTest.getCaseFamilies();

        assertThat(caseFamilies)
            .isNotEmpty()
            .hasSize(3)
            .satisfies(items -> {
                final CaseFamily caseFamily1 = items.get(0);
                final List<Long> familyMembers1 = caseFamily1.getFamilyMembers().stream()
                    .map(CaseData::getId)
                    .collect(Collectors.toUnmodifiableList());

                final CaseFamily caseFamily2 = items.get(1);
                final List<Long> familyMembers2 = caseFamily2.getFamilyMembers().stream()
                    .map(CaseData::getId)
                    .collect(Collectors.toUnmodifiableList());

                final CaseFamily caseFamily3 = items.get(2);
                final List<Long> familyMembers3 = caseFamily3.getFamilyMembers().stream()
                    .map(CaseData::getId)
                    .collect(Collectors.toUnmodifiableList());

                assertThat(caseFamily1.getRootAncestor().getId()).isEqualTo(1L);
                assertThat(familyMembers1).hasSize(3).hasSameElementsAs(List.of(10L, 11L, 100L));

                assertThat(caseFamily2.getRootAncestor().getId()).isEqualTo(2L);
                assertThat(familyMembers2).hasSize(3).hasSameElementsAs(List.of(10L, 11L, 100L));

                assertThat(caseFamily3.getRootAncestor().getId()).isEqualTo(1000L);
                assertThat(familyMembers3).isEmpty();
            });
    }

}
