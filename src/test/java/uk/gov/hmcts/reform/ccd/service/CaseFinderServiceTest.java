package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.data.model.Retainability;
import uk.gov.hmcts.reform.ccd.fixture.TestData;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.ccd.data.model.Retainability.INDETERMINATE;
import static uk.gov.hmcts.reform.ccd.data.model.Retainability.RETAIN;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA03_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA04_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA05_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA06_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA07_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA08_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA2_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA4_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA5_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_TODAY_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_R10;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_R100;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_R101;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_R11;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_R12;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_R13;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.NON_DELETABLE_CASE_DATA_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.service.CaseFinderService.INTERSECTION_FUNCTION;

@ExtendWith(MockitoExtension.class)
class CaseFinderServiceTest {
    @Mock
    private ParameterResolver parameterResolver;
    @Mock
    private CaseFamilyTreeService caseFamilyTreeService;
    @InjectMocks
    private CaseFinderService caseFinderService;

    private static final Set<Long> SET_A = Set.of(0L, 2L, 3L, 4L);
    private static final Set<Long> SET_B = Set.of(3L, 0L, 4L, 5L, 6L);
    private static final Set<Long> SET_C = Set.of(1L);

    @Test
    @DisplayName("Partition by retainability when no retainable case families present")
    void testPartitionByRetainabilityScenario1() {
        final List<CaseFamily> caseFamilies = List.of(new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL, emptyList()));

        doReturn(List.of(TestData.DELETABLE_CASE_TYPE)).when(parameterResolver).getAllDeletableCaseTypes();

        final Map<Enum<Retainability>, List<CaseFamily>> partitioned = caseFinderService
                .partitionByRetainability(caseFamilies);

        assertThat(partitioned)
                .isNotEmpty()
                .satisfies(item -> {
                    final List<CaseFamily> caseFamilies1 = item.get(RETAIN);
                    final List<CaseFamily> caseFamilies2 = item.get(INDETERMINATE);

                    assertThat(caseFamilies1).isEmpty();
                    assertThat(caseFamilies2)
                            .isNotEmpty()
                            .singleElement()
                            .satisfies(caseFamily -> assertThat(caseFamily.getRootCase().getId()).isEqualTo(1L));
                });
    }

    @ParameterizedTest
    @MethodSource("provideScenario2Params")
    @DisplayName("Partition by retainability when only retainable case families present")
    void testPartitionByRetainabilityScenario2(final CaseData ancestorCase, final Long caseId) {
        final List<CaseFamily> caseFamilies = List.of(new CaseFamily(ancestorCase, emptyList()));

        final Map<Enum<Retainability>, List<CaseFamily>> partitioned =
                caseFinderService.partitionByRetainability(caseFamilies);

        assertThat(partitioned)
                .isNotEmpty()
                .satisfies(item -> {
                    final List<CaseFamily> caseFamilies1 = item.get(RETAIN);
                    final List<CaseFamily> caseFamilies2 = item.get(INDETERMINATE);

                    assertThat(caseFamilies1)
                            .isNotEmpty()
                            .singleElement()
                            .satisfies(caseFamily -> assertThat(caseFamily.getRootCase().getId()).isEqualTo(caseId));
                    assertThat(caseFamilies2).isEmpty();
                });
    }

    private static Stream<Arguments> provideScenario2Params() {
        return Stream.of(
                Arguments.of(DELETABLE_CASE_DATA_WITH_TODAY_TTL, 2L),
                Arguments.of(NON_DELETABLE_CASE_DATA_WITH_PAST_TTL, 21L)
        );
    }

    @Test
    @DisplayName("Partition by retainability when both retainable case families"
            + " and indeterminate case families are present")
    void testPartitionByRetainabilityScenario3() {
        final List<CaseData> linkedCases1 = List.of(LINKED_CASE_DATA_R10, LINKED_CASE_DATA_R100);
        final List<CaseData> linkedCases2 = List.of(LINKED_CASE_DATA_R11);
        final CaseFamily caseFamily1 = new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL, linkedCases1);
        final CaseFamily caseFamily2 = new CaseFamily(DELETABLE_CASE_DATA2_WITH_PAST_TTL, linkedCases2);
        final List<CaseFamily> caseFamilies = List.of(caseFamily1, caseFamily2);

        doReturn(List.of(TestData.DELETABLE_CASE_TYPE)).when(parameterResolver).getAllDeletableCaseTypes();

        final Map<Enum<Retainability>, List<CaseFamily>> partitioned =
                caseFinderService.partitionByRetainability(caseFamilies);

        assertThat(partitioned)
                .isNotEmpty()
                .satisfies(item -> {
                    final List<CaseFamily> caseFamilies1 = item.get(RETAIN);
                    final List<CaseFamily> caseFamilies2 = item.get(INDETERMINATE);

                    assertThat(caseFamilies1)
                            .isNotEmpty()
                            .singleElement()
                            .satisfies(caseFamily -> assertThat(caseFamily.getRootCase().getId()).isEqualTo(1L));
                    assertThat(caseFamilies2)
                            .isNotEmpty()
                            .singleElement()
                            .satisfies(caseFamily -> assertThat(caseFamily.getRootCase().getId()).isEqualTo(1000L));
                });
    }

    @ParameterizedTest
    @MethodSource("provideNoIntersectSets")
    void testIntersectionResultingInEmptySet(final Set<Long> setA, final Set<Long> setB) {
        final Set<Long> intersection = INTERSECTION_FUNCTION.apply(setA, setB);

        assertThat(intersection)
                .isEmpty();
    }

    private static Stream<Arguments> provideNoIntersectSets() {
        return Stream.of(
                Arguments.of(SET_A, emptySet()),
                Arguments.of(emptySet(), SET_A),
                Arguments.of(SET_C, SET_A),
                Arguments.of(SET_A, SET_C)
        );
    }

    @ParameterizedTest
    @MethodSource("provideIntersectSets")
    void testIntersectionResultingSetSizeAndValues(final Set<Long> setA, final Set<Long> setB) {
        final Set<Long> expectedSet = Set.of(0L, 3L, 4L);

        final Set<Long> intersection = INTERSECTION_FUNCTION.apply(setA, setB);

        assertThat(intersection)
                .isNotEmpty()
                .hasSize(3)
                .hasSameElementsAs(expectedSet);
    }

    private static Stream<Arguments> provideIntersectSets() {
        return Stream.of(
                Arguments.of(SET_A, SET_B),
                Arguments.of(SET_B, SET_A)
        );
    }

    @Test
    void testFindRetainableCaseFamilies() {
        final List<CaseData> linkedCases1 = List.of(LINKED_CASE_DATA_R10, LINKED_CASE_DATA_R100);
        final List<CaseData> linkedCases2 = List.of(LINKED_CASE_DATA_R11);
        final List<CaseData> linkedCases3 = List.of(LINKED_CASE_DATA_R12);
        final List<CaseData> linkedCases4 = List.of(LINKED_CASE_DATA_R10, LINKED_CASE_DATA_R11);
        final List<CaseData> linkedCases5 = List.of(LINKED_CASE_DATA_R101);
        final List<CaseData> linkedCases6 = List.of(LINKED_CASE_DATA_R10);
        final List<CaseData> linkedCases7 = List.of(LINKED_CASE_DATA_R12);
        final List<CaseData> linkedCases8 = List.of(LINKED_CASE_DATA_R11, LINKED_CASE_DATA_R12);
        final List<CaseData> linkedCases9 = emptyList();
        final List<CaseData> linkedCases10 = List.of(LINKED_CASE_DATA_R13);
        final CaseFamily caseFamily1 = new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL, linkedCases1);
        final CaseFamily caseFamily2 = new CaseFamily(DELETABLE_CASE_DATA2_WITH_PAST_TTL, linkedCases2);
        final CaseFamily caseFamily3 = new CaseFamily(DELETABLE_CASE_DATA03_WITH_PAST_TTL, linkedCases3);
        final CaseFamily caseFamily4 = new CaseFamily(DELETABLE_CASE_DATA04_WITH_PAST_TTL, linkedCases4);
        final CaseFamily caseFamily5 = new CaseFamily(DELETABLE_CASE_DATA05_WITH_PAST_TTL, linkedCases5);
        final CaseFamily caseFamily6 = new CaseFamily(DELETABLE_CASE_DATA06_WITH_PAST_TTL, linkedCases6);
        final CaseFamily caseFamily7 = new CaseFamily(DELETABLE_CASE_DATA07_WITH_PAST_TTL, linkedCases7);
        final CaseFamily caseFamily8 = new CaseFamily(DELETABLE_CASE_DATA08_WITH_PAST_TTL, linkedCases8);
        final CaseFamily caseFamily9 = new CaseFamily(DELETABLE_CASE_DATA4_WITH_PAST_TTL, linkedCases9);
        final CaseFamily caseFamily10 = new CaseFamily(DELETABLE_CASE_DATA5_WITH_PAST_TTL, linkedCases10);

        final List<CaseFamily> caseFamilies1 = List.of(caseFamily2, caseFamily3, caseFamily4, caseFamily6,
                caseFamily7, caseFamily8, caseFamily9, caseFamily10);
        final List<CaseFamily> caseFamilies2 = List.of(caseFamily1, caseFamily5);

        final List<CaseFamily> results = caseFinderService.findRetainableCaseFamilies(caseFamilies1, caseFamilies2);

        assertThat(results)
                .isNotEmpty()
                .satisfies(caseFamilies -> {
                    final List<Long> actualCaseIds = caseFamilies.stream()
                            .map(caseFamily -> Stream.concat(Stream.of(caseFamily.getRootCase()),
                                    caseFamily.getLinkedCases().stream()))
                            .map(entityStream -> entityStream.collect(Collectors.toUnmodifiableList()))
                            .flatMap(Collection::stream)
                            .map(CaseData::getId)
                            .collect(Collectors.toUnmodifiableSet())
                            .stream()
                            .collect(Collectors.toUnmodifiableList());

                    assertThat(actualCaseIds)
                            .isNotEmpty()
                            .hasSize(13)
                            .hasSameElementsAs(List.of(1L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 91L, 92L, 100L, 101L, 1000L));
                });
    }

    @Test
    void testFindCasesDueDeletion() {
        final List<CaseData> linkedCases = List.of(LINKED_CASE_DATA_R13);
        final CaseFamily caseFamily1 = new CaseFamily(DELETABLE_CASE_DATA4_WITH_PAST_TTL, emptyList());
        final CaseFamily caseFamily2 = new CaseFamily(DELETABLE_CASE_DATA5_WITH_PAST_TTL, linkedCases);

        final List<CaseFamily> deletableCaseFamilies = List.of(caseFamily1, caseFamily2);

        doReturn(deletableCaseFamilies).when(caseFamilyTreeService).getCaseFamilies();
        doReturn(List.of(TestData.DELETABLE_CASE_TYPE)).when(parameterResolver).getAllDeletableCaseTypes();

        final List<CaseFamily> casesDueDeletion = caseFinderService.findCasesDueDeletion();

        assertThat(casesDueDeletion)
                .isNotEmpty()
                .hasSize(2)
                .satisfies(caseFamilies -> {
                    final CaseFamily resultCaseFamily1 = caseFamilies.get(0);
                    final CaseFamily resultCaseFamily2 = caseFamilies.get(1);

                    assertThat(resultCaseFamily1.getRootCase().getId()).isEqualTo(4L);
                    assertThat(resultCaseFamily1.getLinkedCases()).isEmpty();

                    assertThat(resultCaseFamily2.getRootCase().getId()).isEqualTo(5L);
                    assertThat(resultCaseFamily2.getLinkedCases())
                            .isNotEmpty()
                            .singleElement()
                            .satisfies(caseData -> assertThat(caseData.getId()).isEqualTo(13L));
                });
    }

    @Test
    void testFindCasesDueDeletionShouldReturnEmpty() {
        final List<CaseData> linkedCases = List.of(LINKED_CASE_DATA_R101);
        final CaseFamily caseFamily = new CaseFamily(DELETABLE_CASE_DATA5_WITH_PAST_TTL, linkedCases);

        final List<CaseFamily> deletableCaseFamilies = List.of(caseFamily);

        doReturn(deletableCaseFamilies).when(caseFamilyTreeService).getCaseFamilies();
        doReturn(List.of(TestData.DELETABLE_CASE_TYPE)).when(parameterResolver).getAllDeletableCaseTypes();

        final List<CaseFamily> casesDueDeletion = caseFinderService.findCasesDueDeletion();

        assertThat(casesDueDeletion)
                .isEmpty();
    }

}
