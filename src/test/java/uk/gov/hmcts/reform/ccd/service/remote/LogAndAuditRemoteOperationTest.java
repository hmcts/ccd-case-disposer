package uk.gov.hmcts.reform.ccd.service.remote;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.data.lau.ActionLog;
import uk.gov.hmcts.reform.ccd.data.lau.CaseActionPostRequestResponse;
import uk.gov.hmcts.reform.ccd.exception.LogAndAuditException;
import uk.gov.hmcts.reform.ccd.service.remote.clients.LauClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.LauRecordHolder;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL;

@ExtendWith(MockitoExtension.class)
class LogAndAuditRemoteOperationTest {

    @Mock
    private LauClient lauClient;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private LauRecordHolder lauRecordHolder;

    @InjectMocks
    private LogAndAuditRemoteOperation logAndAuditRemoteOperation;

    @BeforeEach
    void setUp() {
        final UserDetails userDetails = mock(UserDetails.class);
        when(securityUtil.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn("123");
        when(securityUtil.getServiceAuthorization()).thenReturn("some_cool_service_auth");
    }

    @Test
    void shouldPostToLogAndAudit() {
        final CaseActionPostRequestResponse caseActionResponse =
            new CaseActionPostRequestResponse(ActionLog.builder().caseRef("123").build());

        when(lauClient.postLauAudit(anyString(), any(CaseActionPostRequestResponse.class)))
            .thenReturn(ResponseEntity.of(Optional.of(caseActionResponse)));


        logAndAuditRemoteOperation.postCaseDeletionToLogAndAudit(DELETABLE_CASE_DATA_WITH_PAST_TTL);

        verify(lauRecordHolder, times(1)).addLauCaseRef("123");
        verify(lauClient, times(1)).postLauAudit(
            eq("some_cool_service_auth"),
            any()
        );

    }

    @Test
    void shouldThrowExceptionWhenRequestInvalid() {
        doThrow(new LogAndAuditException("Hmmmmm something is wrong here"))
            .when(lauClient)
            .postLauAudit(anyString(), any(CaseActionPostRequestResponse.class));

        assertThatExceptionOfType(LogAndAuditException.class)
            .isThrownBy(() -> logAndAuditRemoteOperation
                .postCaseDeletionToLogAndAudit(DELETABLE_CASE_DATA_WITH_PAST_TTL))
            .withMessage("Error posting to Log and Audit for case: 1");
    }

    @Test
    void shouldThrowExceptionWhenResponseStatusIsNot2xx() {
        final CaseActionPostRequestResponse caseActionResponse =
            new CaseActionPostRequestResponse(ActionLog.builder().caseRef("123").build());

        when(lauClient.postLauAudit(anyString(), any(CaseActionPostRequestResponse.class)))
            .thenReturn(ResponseEntity.status(500).body(caseActionResponse));

        assertThatExceptionOfType(LogAndAuditException.class)
            .isThrownBy(() -> logAndAuditRemoteOperation
                .postCaseDeletionToLogAndAudit(DELETABLE_CASE_DATA_WITH_PAST_TTL))
            .withMessage("Unexpected response code 500 while sending data to Log and Audit for case: 1");
    }
}
