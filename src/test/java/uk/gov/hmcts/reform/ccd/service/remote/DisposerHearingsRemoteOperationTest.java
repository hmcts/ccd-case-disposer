package uk.gov.hmcts.reform.ccd.service.remote;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.HearingDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.remote.clients.HearingClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.HearingDeletionRecordHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.HEARING_RECORDINGS_CASE_TYPE;

@ExtendWith(MockitoExtension.class)
class DisposerHearingsRemoteOperationTest {

    private static final String AUTH_HEADER = "Bearer 123";
    private static final String SERVICE_AUTH_HEADER = "Bearer 456";

    @Mock
    private HearingClient hearingClient;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private ParameterResolver parameterResolver;

    @Mock
    private HearingDeletionRecordHolder hearingDeletionRecordHolder;

    @InjectMocks
    private DisposeHearingsRemoteOperation disposeHearingsRemoteOperation;

    final CaseData caseData = CaseData.builder()
        .reference(1234567890123456L)
        .caseType(HEARING_RECORDINGS_CASE_TYPE).build();

    @Test
    void shouldDeleteHearingsSuccessfully() {
        ResponseEntity<Void> mockResponse = new ResponseEntity<>(HttpStatus.NO_CONTENT);

        when(parameterResolver.getHearingCaseType()).thenReturn(HEARING_RECORDINGS_CASE_TYPE);
        when(securityUtil.getIdamClientToken()).thenReturn(AUTH_HEADER);
        when(securityUtil.getServiceAuthorization()).thenReturn(SERVICE_AUTH_HEADER);
        when(hearingClient.deleteHearing(AUTH_HEADER,
                                         SERVICE_AUTH_HEADER,
                                         List.of(caseData.getReference().toString())))
            .thenReturn(mockResponse);

        disposeHearingsRemoteOperation.delete(caseData);

        verify(hearingClient).deleteHearing(AUTH_HEADER,
                                            SERVICE_AUTH_HEADER,
                                            List.of(String.valueOf(caseData.getReference())));
        verify(hearingDeletionRecordHolder)
            .setHearingDeletionResults(List.of(String.valueOf(caseData.getReference())).getFirst(),
                                       mockResponse.getStatusCode().value());
    }

    @Test
    void shouldThrowHearingDeletionExceptionWhenErrorOccurs() {
        final List<String> caseRefs = List.of("1234567890123456");
        final RuntimeException exception = new RuntimeException("Delete request failed");
        when(parameterResolver.getHearingCaseType()).thenReturn(HEARING_RECORDINGS_CASE_TYPE);
        when(securityUtil.getIdamClientToken()).thenReturn(AUTH_HEADER);
        when(securityUtil.getServiceAuthorization()).thenReturn(SERVICE_AUTH_HEADER);

        doThrow(exception)
            .when(hearingClient)
            .deleteHearing(AUTH_HEADER, SERVICE_AUTH_HEADER, caseRefs);

        assertThatExceptionOfType(HearingDeletionException.class)
            .isThrownBy(() -> disposeHearingsRemoteOperation.delete(caseData))
            .withMessage("Error deleting hearing for case : 1234567890123456")
            .withCause(exception);
    }


    @Test
    void shouldNotInvokeHearingDeletionIfCaseTypeIsNotHearing() {
        when(parameterResolver.getHearingCaseType()).thenReturn(HEARING_RECORDINGS_CASE_TYPE);
        final CaseData nonHrsCaseData = CaseData.builder()
            .reference(1234567890123456L)
            .caseType("someRandomCaseType").build();

        disposeHearingsRemoteOperation.delete(nonHrsCaseData);

        verifyNoInteractions(hearingDeletionRecordHolder);
        verifyNoInteractions(hearingClient);
        verifyNoInteractions(securityUtil);
    }

    @Test
    void shouldThrowExceptionWhenResponseCodeNot204() {
        ResponseEntity<Void> mockResponse = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(parameterResolver.getHearingCaseType()).thenReturn(HEARING_RECORDINGS_CASE_TYPE);
        when(securityUtil.getIdamClientToken()).thenReturn(AUTH_HEADER);
        when(securityUtil.getServiceAuthorization()).thenReturn(SERVICE_AUTH_HEADER);
        when(hearingClient.deleteHearing(AUTH_HEADER,
                                         SERVICE_AUTH_HEADER,
                                         List.of(caseData.getReference().toString())))
            .thenReturn(mockResponse);
        assertThrows(HearingDeletionException.class, () -> disposeHearingsRemoteOperation.delete(caseData));
        verify(hearingDeletionRecordHolder).setHearingDeletionResults(caseData.getReference().toString(), 500);
    }
}
