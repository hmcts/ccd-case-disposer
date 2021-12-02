package uk.gov.hmcts.reform.ccd.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_FUTURE_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_TODAY_TTL;

class TtlRetentionPolicyImplTest {
    private final TtlRetentionPolicyImpl underTest = new TtlRetentionPolicyImpl();

    @Test
    void testMustRetainWhenTtlIsYesterday() {
        final Boolean result = underTest.mustRetain(DELETABLE_CASE_DATA_WITH_PAST_TTL);

        assertThat(result)
            .isNotNull()
            .isFalse();
    }

    @Test
    void testMustRetainWhenTtlIsToday() {
        final Boolean result = underTest.mustRetain(DELETABLE_CASE_DATA_WITH_TODAY_TTL);

        assertThat(result)
            .isNotNull()
            .isTrue();
    }

    @Test
    void testMustRetainWhenTtlIsTomorrow() {
        final Boolean result = underTest.mustRetain(DELETABLE_CASE_DATA_WITH_FUTURE_TTL);

        assertThat(result)
            .isNotNull()
            .isTrue();
    }

}
