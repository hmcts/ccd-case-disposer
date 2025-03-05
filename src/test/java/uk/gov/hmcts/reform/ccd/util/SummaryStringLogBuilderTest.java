package uk.gov.hmcts.reform.ccd.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.FAILED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_STATE;

class SummaryStringLogBuilderTest {

    @Test
    void shouldReturnDeletionSummaryString() {
        final CaseDataView caseDataView = new CaseDataView("NFD", 111L, DELETED_STATE);
        final CaseDataView caseDataView1 = new CaseDataView("Caveat", 333L, DELETED_STATE);
        final CaseDataView caseDataView6 = new CaseDataView("Caveat", 666L, DELETED_STATE);

        final CaseDataView caseDataView2 = new CaseDataView("MoneyClaimCase", 222L, SIMULATED_STATE);
        final CaseDataView caseDataView3 = new CaseDataView("A58", 111L, SIMULATED_STATE);
        final CaseDataView caseDataView7 = new CaseDataView("A58", 777L, SIMULATED_STATE);

        final CaseDataView caseDataView4 = new CaseDataView(null, 444L, FAILED_STATE);
        final CaseDataView caseDataView5 = new CaseDataView(null, 555L, FAILED_STATE);

        final List<CaseDataView> caseDataViews = Arrays.asList(caseDataView, caseDataView1, caseDataView6,caseDataView2,
                caseDataView3,caseDataView7, caseDataView4, caseDataView5);


        final SummaryStringLogBuilder summaryStringLogBuilder = new SummaryStringLogBuilder();
        final String buildSummaryString = summaryStringLogBuilder
                .buildSummaryString(caseDataViews, 1, 1, Arrays.asList("NFD", "Caveat"),
                        Arrays.asList("MoneyClaimCase", "A58"));

        assertThat(buildSummaryString).contains("Case Disposer Deletion Summary 1 of 1");

        assertThat(buildSummaryString).contains("Total cases : 8");
        assertThat(buildSummaryString).contains("Deleted cases : 3");
        assertThat(buildSummaryString).contains("Simulated cases : 3");
        assertThat(buildSummaryString).contains("Failed cases : 2");
        assertThat(buildSummaryString).contains("Total MoneyClaimCase Simulated cases : 1");
        assertThat(buildSummaryString).contains("Total A58 Simulated cases : 2");
        assertThat(buildSummaryString).contains("Total NFD Deleted cases : 1");
        assertThat(buildSummaryString).contains("Total Caveat Deleted cases : 2");
    }
}
