package uk.gov.hmcts.reform.ccd.service.remote;

import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.HearingDeletionException;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.HearingDeletionRecordHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_HEARINGS_PATH;

@ExtendWith(MockitoExtension.class)
class DisposerHearingsRemoteOperationTest {

    @Mock
    private HearingClient hearingClient;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private HearingDeletionRecordHolder hearingDeletionRecordHolder;

    @InjectMocks
    private DisposeHearingsRemoteOperation disposeHearingsRemoteOperation;

    final CaseData caseData = CaseData.builder().reference(1234567890123456L).build();

    @Test
    void shouldDeleteHearingsSuccessfully() {
        Response mockResponse = mock(Response.class);

        when(mockResponse.status()).thenReturn(204);
        when(securityUtil.getIdamClientToken()).thenReturn("123");
        when(securityUtil.getServiceAuthorization()).thenReturn("456");
        when(hearingClient.deleteHearing("123",
                                         "456",
                                         List.of(caseData.getReference().toString())))
            .thenReturn(mockResponse);

        disposeHearingsRemoteOperation.delete(caseData);

        verify(hearingClient).deleteHearing("123",
                                            "456",
                                            List.of(String.valueOf(caseData.getReference())));
        verify(hearingDeletionRecordHolder)
            .setHearingDeletionResults(List.of(String.valueOf(caseData.getReference())).getFirst(),
                                       mockResponse.status());
    }

    @Test
    void shouldThrowHearingDeletionExceptionWhenErrorOccurs() {
        final List<String> caseRefs = List.of("1234567890123456");

        doThrow(new RuntimeException("Delete request failed"))
            .when(hearingClient).deleteHearing(any(), eq(DELETE_HEARINGS_PATH), eq(caseRefs));

        assertThrows(HearingDeletionException.class, () -> disposeHearingsRemoteOperation.delete(caseData));
    }
}
