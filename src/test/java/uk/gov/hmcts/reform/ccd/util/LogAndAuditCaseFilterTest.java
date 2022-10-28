package uk.gov.hmcts.reform.ccd.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY;

class LogAndAuditCaseFilterTest {

    @Test
    void should_Return_Distinct_CaseDataList_From_CaseFamilyList() {
        final List<CaseData> distinctCaseDataFromCaseFamilyList = new LogAndAuditCaseFilter()
                .getDistinctCaseDataFromCaseFamilyList(Arrays.asList(DELETABLE_CASE_FAMILY));

        assertThat(distinctCaseDataFromCaseFamilyList.size()).isEqualTo(2);

        assertThat(distinctCaseDataFromCaseFamilyList.get(0).getId())
                .isEqualTo(DELETABLE_CASE_FAMILY.getRootCase().getId());
        assertThat(distinctCaseDataFromCaseFamilyList.get(1).getId())
                .isEqualTo(DELETABLE_CASE_FAMILY.getLinkedCases().get(0).getId());
    }
}