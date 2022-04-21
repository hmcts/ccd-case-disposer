package uk.gov.hmcts.reform.ccd.policy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.fixture.TestData;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_TODAY_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.NON_DELETABLE_CASE_DATA_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.TODAY;

@ExtendWith(MockitoExtension.class)
class CaseTypeRetentionPolicyImplTest {
    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private CaseTypeRetentionPolicyImpl underTest;

    @Test
    void testShouldRaiseNullPointerExceptionWhenNullValueIsProvided() {
        // GIVEN
        final CaseData caseData = null;

        // WHEN/THEN
        assertThatNullPointerException().isThrownBy(() -> underTest.mustRetain(caseData));
    }

    @Test
    void testShouldRaiseNullPointerExceptionWhenCaseTypeIsNull() {
        // GIVEN
        final CaseData caseData = new CaseData(2L, 2L, null, TODAY, 2L, null);

        // WHEN/THEN
        assertThatNullPointerException().isThrownBy(() -> underTest.mustRetain(caseData));
    }

    @ParameterizedTest
    @MethodSource("provideTestParams")
    void testMustRetain(final List<String> deletableCaseTypes,
                        final CaseData caseData,
                        final boolean flag) {
        doReturn(deletableCaseTypes).when(parameterResolver).getAllDeletableCaseTypes();

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
            Arguments.of(List.of(TestData.DELETABLE_CASE_TYPE), DELETABLE_CASE_DATA_WITH_TODAY_TTL, false),
            Arguments.of(List.of(TestData.DELETABLE_CASE_TYPE), NON_DELETABLE_CASE_DATA_WITH_PAST_TTL, true),
            Arguments.of(emptyList(), DELETABLE_CASE_DATA_WITH_TODAY_TTL, true)
        );
    }
}
