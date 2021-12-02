package uk.gov.hmcts.reform.ccd.policy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.fixture.TestData;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_TODAY_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.NON_DELETABLE_CASE_DATA_WITH_TODAY_TTL;

@ExtendWith(MockitoExtension.class)
class CaseTypeRetentionPolicyImplTest {
    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private CaseTypeRetentionPolicyImpl underTest;

    @Test
    void testMustRetainWhenCaseTypeIsDeletable() {
        doReturn(List.of(TestData.DELETABLE_CASE_TYPE)).when(parameterResolver).getDeletableCaseTypes();

        final Boolean result = underTest.mustRetain(DELETABLE_CASE_DATA_WITH_TODAY_TTL);

        assertThat(result)
            .isNotNull()
            .isFalse();
    }

    @Test
    void testMustRetainWhenCaseTypeIsNotDeletable() {
        doReturn(List.of(TestData.DELETABLE_CASE_TYPE)).when(parameterResolver).getDeletableCaseTypes();

        final Boolean result = underTest.mustRetain(NON_DELETABLE_CASE_DATA_WITH_TODAY_TTL);

        assertThat(result)
            .isNotNull()
            .isTrue();
    }
}
