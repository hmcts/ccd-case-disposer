package uk.gov.hmcts.reform.ccd.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_ENTITY2_WITH_PAST_TTL;

class CaseFamilyUtilTest {
    private final CaseData caseData1 = new CaseData(
        DELETABLE_CASE_DATA_WITH_PAST_TTL.getId(),
        DELETABLE_CASE_DATA_WITH_PAST_TTL.getReference(),
        DELETABLE_CASE_DATA_WITH_PAST_TTL.getCaseType(),
        DELETABLE_CASE_DATA_WITH_PAST_TTL.getJurisdiction(),
        DELETABLE_CASE_DATA_WITH_PAST_TTL.getResolvedTtl(),
        DELETABLE_CASE_DATA_WITH_PAST_TTL.getId(),
        null
    );
    private final CaseData caseData2 = new CaseData(
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getId(),
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getReference(),
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getCaseType(),
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getJurisdiction(),
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getResolvedTtl(),
        DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getId(),
        caseData1
    );

//    @Test
//    void testShouldFlattenCaseFamilies() {
//        final List<CaseFamily> caseFamilies = List.of(new CaseFamily(caseData1, List.of(caseData2)));
//
//        final List<CaseData> result = FLATTEN_CASE_FAMILIES_FUNCTION.apply(caseFamilies);
//
//        assertThat(result)
//            .isNotEmpty()
//            .containsExactlyInAnyOrder(caseData1, caseData2);
//    }

//    @Test
//    void testShouldReturnOnlyRootCaseWhenNoLinkedCasesPresent() {
//        final List<CaseFamily> caseFamilies = List.of(new CaseFamily(caseData1, emptyList()));
//
//        final List<List<CaseData>> result = POTENTIAL_MULTI_FAMILY_CASE_AGGREGATOR_FUNCTION.apply(caseFamilies);
//
//        assertThat(result.getFirst())
//            .singleElement()
//            .isEqualTo(caseData1);
//    }

//    @Test
//    void testResultShouldNotContainRootCaseWhenLinkedCasesPresent() {
//        final List<CaseFamily> caseFamilies = List.of(new CaseFamily(caseData1, List.of(caseData2)));
//
//        final List<List<CaseData>> result = POTENTIAL_MULTI_FAMILY_CASE_AGGREGATOR_FUNCTION.apply(caseFamilies);
//
//        assertThat(result.getFirst())
//            .singleElement()
//            .isEqualTo(caseData2);
//    }
}
