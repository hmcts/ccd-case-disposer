package uk.gov.hmcts.reform.ccd.service.remote;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.HearingDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.HearingDeletionRecordHolder;

import static jakarta.ws.rs.core.Response.ok;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_HEARINGS_PATH;

@ExtendWith(MockitoExtension.class)
public class DisposerHearingsRemoteOperationTest {

    @Mock
    private CcdRestClientBuilder ccdRestClientBuilder;

    @Mock
    private ParameterResolver parameterResolver;

    @Mock
    private HearingDeletionRecordHolder hearingDeletionRecordHolder;

    @InjectMocks
    private DisposeHearingsRemoteOperation disposeHearingsRemoteOperation;

    final CaseData caseData = CaseData.builder().reference(1234567890123456L).build();

    @Test
    void shouldDeleteHearingsSuccessfully() {
        final String caseRef = "1234567890123456";

        Response mockResponse = ok().build();

        when(parameterResolver.getHearingHost()).thenReturn("http://localhost");
        when(ccdRestClientBuilder.deleteRequestWithAuthHeaders(any(), eq(DELETE_HEARINGS_PATH), eq(caseRef)))
            .thenReturn(mockResponse);

        disposeHearingsRemoteOperation.delete(caseData);

        verify(ccdRestClientBuilder).deleteRequestWithAuthHeaders(any(), eq(DELETE_HEARINGS_PATH), eq(caseRef));
        verify(hearingDeletionRecordHolder).setHearingDeletionResults(caseRef, mockResponse.getStatus());
    }

    @Test
    void shouldThrowHearingDeletionExceptionWhenErrorOccurs() {
        final String caseRef = "1234567890123456";

        when(parameterResolver.getHearingHost()).thenReturn("http://localhost");

        doThrow(new RuntimeException("Delete request failed"))
            .when(ccdRestClientBuilder).deleteRequestWithAuthHeaders(any(), eq(DELETE_HEARINGS_PATH), eq(caseRef));


        assertThrows(HearingDeletionException.class, () -> disposeHearingsRemoteOperation.delete(caseData));
    }
}
