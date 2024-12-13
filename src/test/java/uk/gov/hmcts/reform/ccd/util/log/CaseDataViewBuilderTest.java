package uk.gov.hmcts.reform.ccd.util.log;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_STATE;

class CaseDataViewBuilderTest {
    @Test
    void shouldCreateCaseDataViewForDeletableCases() {
        final List<CaseData> deletableCaseFamily = List.of(
            CaseData.builder().id(1L).caseType(DELETABLE_CASE_TYPE).reference(11L).build(),
            CaseData.builder().id(2L).caseType(DELETABLE_CASE_TYPE).reference(22L).build()
        );

        final List<CaseDataView> caseDataViews = new ArrayList<>();

        new CaseDataViewBuilder().buildCaseDataViewList(deletableCaseFamily, caseDataViews, DELETED_STATE);

        assertThat(caseDataViews).hasSize(2);
        assertThat(caseDataViews.getFirst().getCaseType()).isEqualTo(DELETABLE_CASE_TYPE);
        assertThat(caseDataViews.getFirst().getCaseRef()).isEqualTo(11L);
        assertThat(caseDataViews.getFirst().getState()).isEqualTo(DELETED_STATE);
    }

}
