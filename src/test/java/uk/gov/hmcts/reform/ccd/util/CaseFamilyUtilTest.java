package uk.gov.hmcts.reform.ccd.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_ENTITY2_WITH_PAST_TTL;

class CaseFamilyUtilTest {
    private final CaseData caseData1 = new CaseData(
        DELETABLE_CASE_DATA_WITH_PAST_TTL.id(),
        DELETABLE_CASE_DATA_WITH_PAST_TTL.reference(),
        DELETABLE_CASE_DATA_WITH_PAST_TTL.caseType(),
        DELETABLE_CASE_DATA_WITH_PAST_TTL.jurisdiction(),
        DELETABLE_CASE_DATA_WITH_PAST_TTL.resolvedTtl()
    );
    private final CaseData caseData2 = new CaseData(
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getId(),
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getReference(),
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getCaseType(),
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getJurisdiction(),
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getResolvedTtl()
    );

    // copy of caseData2
    private final CaseData caseData3 = new CaseData(
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getId(),
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getReference(),
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getCaseType(),
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getJurisdiction(),
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getResolvedTtl()
    );


    @Test
    void testShouldFlattenCaseFamilies() {
        final List<CaseFamily> caseFamilies = List.of(new CaseFamily(List.of(caseData1, caseData2)));

        final Set<CaseData> result = CaseFamilyUtil.getCaseData(caseFamilies);

        assertThat(result)
            .isNotEmpty()
            .containsExactlyInAnyOrder(caseData1, caseData2);
    }

    @Test
    void testShouldReturnOnlyRootCaseWhenNoLinkedCasesPresent() {
        final List<CaseFamily> caseFamilies = List.of(new CaseFamily(List.of(caseData1)));

        final Set<CaseData> result = CaseFamilyUtil.getCaseData(caseFamilies);

        assertThat(result.stream().toList().getFirst())
            .isEqualTo(caseData1);
    }

    @Test
    void shouldReturnUniqueOnlyCases() {
        final List<CaseFamily> caseFamilies = List.of(new CaseFamily(List.of(caseData1, caseData2, caseData3)));
        final Set<CaseData> result = CaseFamilyUtil.getCaseData(caseFamilies);
        List<CaseData> resultList = result.stream().toList();

        assertThat(resultList)
            .hasSize(2)
            .containsExactlyInAnyOrder(caseData1, caseData2);
    }
}
