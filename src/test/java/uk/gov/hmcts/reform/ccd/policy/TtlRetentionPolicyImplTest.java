package uk.gov.hmcts.reform.ccd.policy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_FUTURE_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_TODAY_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;

class TtlRetentionPolicyImplTest {
    private final TtlRetentionPolicyImpl underTest = new TtlRetentionPolicyImpl();

    @Test
    void testShouldRaiseNullPointerExceptionWhenNullValueIsProvided() {
        // GIVEN
        final CaseData caseData = null;

        // WHEN/THEN
        assertThatNullPointerException().isThrownBy(() -> underTest.mustRetain(caseData));
    }

    @Test
    void testShouldReturnTrueWhenDateIsNull() {
        // GIVEN
        final CaseData caseData = new CaseData(2L, 2L, DELETABLE_CASE_TYPE,null, null, 2L, null);

        // WHEN/THEN
        assertThat(underTest.mustRetain(caseData))
            .isNotNull()
            .isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideTestParams")
    void testMustRetain(final CaseData caseData, final boolean flag) {
        final Boolean result = underTest.mustRetain(caseData);

        if (flag) {
            assertThat(result)
                .isNotNull()
                .isTrue();
        } else {
            assertThat(result)
                .isNotNull()
                .isFalse();
        }
    }

    private static Stream<Arguments> provideTestParams() {
        return Stream.of(
            Arguments.of(DELETABLE_CASE_DATA_WITH_PAST_TTL, false),
            Arguments.of(DELETABLE_CASE_DATA_WITH_TODAY_TTL, true),
            Arguments.of(DELETABLE_CASE_DATA_WITH_FUTURE_TTL, true)
        );
    }

}
