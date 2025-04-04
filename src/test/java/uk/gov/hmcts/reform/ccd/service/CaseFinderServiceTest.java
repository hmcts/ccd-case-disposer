package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.fixture.TestData;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA4_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA5_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_MISSING_TTL_R102;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_R101;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.LINKED_CASE_DATA_R13;

@ExtendWith(MockitoExtension.class)
class CaseFinderServiceTest {
    @Mock
    private ParameterResolver parameterResolver;
    @Mock
    private CaseFamilyService caseFamilyTreeService;
    @InjectMocks
    private CaseFinderService caseFinderService;

    @Test
    void testFindCasesDueDeletion() {
        final List<CaseData> linkedCases = List.of(DELETABLE_CASE_DATA5_WITH_PAST_TTL, LINKED_CASE_DATA_R13);
        final CaseFamily caseFamily1 = new CaseFamily(List.of(DELETABLE_CASE_DATA4_WITH_PAST_TTL));
        final CaseFamily caseFamily2 = new CaseFamily(linkedCases);

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

                    assertThat(resultCaseFamily1.linkedCases()).hasSize(1);
                    assertThat(resultCaseFamily1.linkedCases().getFirst().id()).isEqualTo(4L);

                    assertThat(resultCaseFamily2.linkedCases())
                            .isNotEmpty()
                            .hasSize(2)
                            .satisfies(cases -> {
                                assertThat(cases.getFirst().id()).isEqualTo(5L);
                                assertThat(cases.get(1).id()).isEqualTo(13L);
                            });
                });
    }

    @Test
    void testFindCasesDueDeletionShouldReturnEmpty() {
        final List<CaseData> linkedCases = List.of(
            DELETABLE_CASE_DATA5_WITH_PAST_TTL,
            LINKED_CASE_DATA_R101,
            LINKED_CASE_DATA_MISSING_TTL_R102);
        final CaseFamily caseFamily = new CaseFamily(linkedCases);

        final List<CaseFamily> deletableCaseFamilies = List.of(caseFamily);

        doReturn(deletableCaseFamilies).when(caseFamilyTreeService).getCaseFamilies();
        doReturn(List.of(TestData.DELETABLE_CASE_TYPE)).when(parameterResolver).getAllDeletableCaseTypes();

        final List<CaseFamily> casesDueDeletion = caseFinderService.findCasesDueDeletion();

        assertThat(casesDueDeletion).isEmpty();
    }

}
