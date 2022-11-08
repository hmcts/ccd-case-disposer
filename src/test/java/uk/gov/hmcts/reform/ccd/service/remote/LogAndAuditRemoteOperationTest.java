package uk.gov.hmcts.reform.ccd.service.remote;


import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.lau.ActionLog;
import uk.gov.hmcts.reform.ccd.data.lau.CaseActionPostRequestResponse;
import uk.gov.hmcts.reform.ccd.exception.LogAndAuditException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.LauRecordHolder;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.LAU_SAVE_PATH;

@ExtendWith(MockitoExtension.class)
class LogAndAuditRemoteOperationTest {

    @Mock
    private ParameterResolver parameterResolver;

    @Mock
    private RestClientBuilder restClientBuilder;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private LauRecordHolder lauRecordHolder;

    @InjectMocks
    private LogAndAuditRemoteOperation logAndAuditRemoteOperation;

    @BeforeEach
    void setUp() {
        final UserDetails userDetails = mock(UserDetails.class);
        when(parameterResolver.getLogAndAuditHost()).thenReturn("http://localhost");
        when(parameterResolver.isLogAndAuditEnabled()).thenReturn(true);
        when(securityUtil.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn("123");
    }

    @Test
    void shouldPostToLogAndAudit() {
        final String caseActionResponse =
                new Gson().toJson(new CaseActionPostRequestResponse(ActionLog.builder().caseRef("123").build()));

        when(restClientBuilder.postRequestWithServiceAuthHeader(eq("http://localhost"), eq(LAU_SAVE_PATH),
                anyString())).thenReturn(caseActionResponse);

        logAndAuditRemoteOperation.postCaseDeletionToLogAndAudit(DELETABLE_CASE_DATA_WITH_PAST_TTL);

        verify(lauRecordHolder, times(1)).addLauCaseRef("123");
        verify(restClientBuilder, times(1)).postRequestWithServiceAuthHeader(eq("http://localhost"), eq(LAU_SAVE_PATH),
                anyString());
    }

    @Test
    void shouldThrowExceptionWhenRequestInvalid() {
        try {
            final String caseActionResponse =
                    new Gson().toJson(new CaseActionPostRequestResponse(ActionLog.builder().caseRef("1").build()));

            doThrow(new LogAndAuditException("Hmmmmm something is wrong here"))
                    .when(restClientBuilder)
                    .postRequestWithServiceAuthHeader("http://localhost", LAU_SAVE_PATH,
                            caseActionResponse);

            logAndAuditRemoteOperation.postCaseDeletionToLogAndAudit(DELETABLE_CASE_DATA_WITH_PAST_TTL);

            fail("The method should have thrown LogAndAuditException when request is invalid");
        } catch (final LogAndAuditException logAndAuditException) {
            assertThat(logAndAuditException.getMessage())
                    .isEqualTo("Error posting to Log and Audit for case : 1");
        }
    }
}