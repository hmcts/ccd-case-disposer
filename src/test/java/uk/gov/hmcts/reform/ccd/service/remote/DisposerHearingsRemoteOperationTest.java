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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_HEARINGS_PATH;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.HEARING_RECORDINGS_CASE_TYPE;

@ExtendWith(MockitoExtension.class)
class DisposerHearingsRemoteOperationTest {

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
        when(securityUtil.getIdamClientToken()).thenReturn("123");
        when(securityUtil.getServiceAuthorization()).thenReturn("456");
        when(hearingClient.deleteHearing("123",
                                         "456",
                                         List.of(caseData.reference().toString())))
            .thenReturn(mockResponse);

        disposeHearingsRemoteOperation.delete(caseData);

        verify(hearingClient).deleteHearing("123",
                                            "456",
                                            List.of(String.valueOf(caseData.reference())));
        verify(hearingDeletionRecordHolder)
            .setHearingDeletionResults(List.of(String.valueOf(caseData.reference())).getFirst(),
                                       mockResponse.getStatusCode().value());
    }

    @Test
    void shouldThrowHearingDeletionExceptionWhenErrorOccurs() {
        final List<String> caseRefs = List.of("1234567890123456");

        when(parameterResolver.getHearingCaseType()).thenReturn(HEARING_RECORDINGS_CASE_TYPE);
        doThrow(new RuntimeException("Delete request failed"))
            .when(hearingClient).deleteHearing(any(), eq(DELETE_HEARINGS_PATH), eq(caseRefs));

        assertThrows(HearingDeletionException.class, () -> disposeHearingsRemoteOperation.delete(caseData));
    }


    @Test
    void shouldNotDeleteHearings() {
        when(parameterResolver.getHearingCaseType()).thenReturn(HEARING_RECORDINGS_CASE_TYPE);
        final CaseData caseData = CaseData.builder()
            .reference(1234567890123456L)
            .caseType("someRandomCaseType").build();

        disposeHearingsRemoteOperation.delete(caseData);

        verifyNoInteractions(hearingDeletionRecordHolder);
        verifyNoInteractions(hearingClient);
        verifyNoInteractions(securityUtil);

    }

    @Test
    void shouldThrowExceptionWhenResponseCodeNot204() {
        ResponseEntity<Void> mockResponse = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(parameterResolver.getHearingCaseType()).thenReturn(HEARING_RECORDINGS_CASE_TYPE);
        when(securityUtil.getIdamClientToken()).thenReturn("123");
        when(securityUtil.getServiceAuthorization()).thenReturn("456");
        when(hearingClient.deleteHearing("123",
                                         "456",
                                         List.of(caseData.reference().toString())))
            .thenReturn(mockResponse);

        assertThrows(HearingDeletionException.class, () -> disposeHearingsRemoteOperation.delete(caseData));
    }
}
