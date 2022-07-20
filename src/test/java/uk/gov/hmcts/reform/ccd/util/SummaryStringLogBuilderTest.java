package uk.gov.hmcts.reform.ccd.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY_SIMULATION;

class SummaryStringLogBuilderTest {

    @Test
    void shouldReturnDeletionSummaryString() {
        final List<CaseFamily> deletableCaseFamily = asList(DELETABLE_CASE_FAMILY);

        final List<CaseFamily> simulationCaseFamily = asList(DELETABLE_CASE_FAMILY_SIMULATION);

        final SummaryStringLogBuilder summaryStringLogBuilder = new SummaryStringLogBuilder();
        final String buildSummaryString = summaryStringLogBuilder
                .buildSummaryString(deletableCaseFamily, simulationCaseFamily, 1, 7);

        assertThat(buildSummaryString).contains("Case Disposer Deletion Summary 1 of 7");
        assertThat(buildSummaryString).contains("Total cases : 7");
        assertThat(buildSummaryString).contains("Simulated cases : 3");
        assertThat(buildSummaryString).contains("Deleted cases : 4");
    }
}