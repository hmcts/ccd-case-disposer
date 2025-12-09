package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.DocumentDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.remote.clients.DocumentClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.DocumentDeletionRecordHolder;

import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.HEARING_RECORDINGS_CASE_TYPE;

@DisplayName("dispose case documents")
@ExtendWith(MockitoExtension.class)
class DisposeDocumentsRemoteOperationTest {

    @Mock
    private DocumentClient documentClient;

    @Mock
    private DocumentDeletionRecordHolder documentDeletionRecordHolder;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private DisposeDocumentsRemoteOperation disposeDocumentsRemoteOperation;

    final CaseData caseData = CaseData.builder()
        .reference(1234567890123456L)
        .caseType("someRandomCaseType")
        .build();

    @Test
    void shouldPostDocumentsDeleteRemoteDisposeRequestWithoutAnomaly() {

        final CaseDocumentsDeletionResults caseActionResponse = new CaseDocumentsDeletionResults(1, 1);
        when(securityUtil.getServiceAuthorization()).thenReturn("some_cool_service_auth");

        when(documentClient.deleteDocument(anyString(), any(DocumentsDeletePostRequest.class)))
            .thenReturn(ResponseEntity.of(Optional.of(caseActionResponse)));

        disposeDocumentsRemoteOperation.delete(caseData).join();

        verify(documentDeletionRecordHolder, times(1)).setCaseDocumentsDeletionResults(
            eq("1234567890123456"),
            any(CaseDocumentsDeletionResults.class)
        );

        verify(documentClient, times(1)).deleteDocument(
            eq("some_cool_service_auth"),
            any()
        );
    }

    @Test
    void shouldThrowExceptionWhenRequestInvalid() {
        when(securityUtil.getServiceAuthorization()).thenReturn("some_cool_service_auth");
        when(documentClient.deleteDocument(anyString(), any(DocumentsDeletePostRequest.class)))
            .thenThrow(new DocumentDeletionException("1234567890123456"));

        try {
            disposeDocumentsRemoteOperation.delete(caseData).join();
            fail("The method should have thrown DocumentDeletionException when request is invalid");
        } catch (CompletionException ex) {
            assertThat(ex.getCause())
                .isInstanceOf(DocumentDeletionException.class)
                .hasMessage("Error deleting documents for case : 1234567890123456");
        }
    }

    @Test
    void shouldThrowExceptionWhenResponseIsUnableToMapToObject() {
        final CaseDocumentsDeletionResults caseActionResponse = new CaseDocumentsDeletionResults(1, 1);

        when(securityUtil.getServiceAuthorization()).thenReturn("some_cool_service_auth");
        when(documentClient.deleteDocument(anyString(), any(DocumentsDeletePostRequest.class)))
            .thenReturn(ResponseEntity.of(Optional.of(caseActionResponse)));

        doThrow(new JsonSyntaxException("Unable to map document post response to object"))
            .when(documentDeletionRecordHolder)
            .setCaseDocumentsDeletionResults(
                eq("1234567890123456"),
                any(CaseDocumentsDeletionResults.class)
            );

        try {
            disposeDocumentsRemoteOperation.delete(caseData).join();
            fail("The method should have thrown DocumentDeletionException when request is invalid");
        } catch (CompletionException ex) {
            assertThat(ex.getCause())
                .isInstanceOf(DocumentDeletionException.class);
            assertThat(ex.getCause().getCause().getMessage())
                .contains("Unable to map json to object document deletion endpoint response "
                             + "due to following endpoint response:");
        }
    }

    @Test
    void shouldNotDeleteHearings() {
        when(parameterResolver.getHearingCaseType()).thenReturn(HEARING_RECORDINGS_CASE_TYPE);
        final CaseData caseData = CaseData.builder()
            .reference(1234567890123456L)
            .caseType(HEARING_RECORDINGS_CASE_TYPE).build();

        disposeDocumentsRemoteOperation.delete(caseData).join();

        verifyNoInteractions(documentDeletionRecordHolder);
        verifyNoInteractions(documentClient);

    }
}
