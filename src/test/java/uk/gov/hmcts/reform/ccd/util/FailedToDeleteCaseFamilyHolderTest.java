package uk.gov.hmcts.reform.ccd.util;

import org.junit.jupiter.api.Test;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY;

class FailedToDeleteCaseFamilyHolderTest {


    @Test
    void shouldAddCaseFamily() {
        final FailedToDeleteCaseFamilyHolder failedToDeleteCaseFamilyHolder = new FailedToDeleteCaseFamilyHolder();

        failedToDeleteCaseFamilyHolder.addCaseFamily(DELETABLE_CASE_FAMILY);

        assertThat(failedToDeleteCaseFamilyHolder.getCaseRefs().size()).isEqualTo(1);
        assertThat(failedToDeleteCaseFamilyHolder.getCaseRefs().get(0))
                .isNotEmpty()
                .containsExactlyInAnyOrderElementsOf(of(1L, 4L));

        assertThat(failedToDeleteCaseFamilyHolder.getFailedToDeleteCaseFamilies().size()).isEqualTo(1);
        assertThat(failedToDeleteCaseFamilyHolder.getFailedToDeleteCaseFamilies()
                .get(0)
                .getRootCase()
                .getId())
                .isEqualTo(1);
        assertThat(failedToDeleteCaseFamilyHolder.getFailedToDeleteCaseFamilies()
                .get(0)
                .getRootCase()
                .getReference())
                .isEqualTo(1);

        assertThat(failedToDeleteCaseFamilyHolder.getFailedToDeleteCaseFamilies().get(0).getLinkedCases().size())
                .isEqualTo(3);
        assertThat(failedToDeleteCaseFamilyHolder.getFailedToDeleteCaseFamilies()
                .get(0)
                .getLinkedCases()
                .get(0)
                .getId())
                .isEqualTo(4);
        assertThat(failedToDeleteCaseFamilyHolder.getFailedToDeleteCaseFamilies()
                .get(0)
                .getLinkedCases()
                .get(0)
                .getReference())
                .isEqualTo(4);

        assertThat(failedToDeleteCaseFamilyHolder.getFailedToDeleteCaseFamilies()
                .get(0)
                .getLinkedCases()
                .get(1)
                .getId())
                .isEqualTo(1);
        assertThat(failedToDeleteCaseFamilyHolder.getFailedToDeleteCaseFamilies()
                .get(0)
                .getLinkedCases()
                .get(1)
                .getReference())
                .isEqualTo(1);
    }
}