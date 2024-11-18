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
import uk.gov.hmcts.reform.ccd.service.remote.clients.DocumentClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.DocumentDeletionRecordHolder;

import java.util.Optional;

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

        disposeDocumentsRemoteOperation.delete(caseData);

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
        try {
            when(securityUtil.getServiceAuthorization()).thenReturn("some_cool_service_auth");
            doThrow(new DocumentDeletionException("1234567890123456"))
                .when(documentClient)
                .deleteDocument(anyString(), any(DocumentsDeletePostRequest.class));

            disposeDocumentsRemoteOperation.delete(caseData);

            fail("The method should have thrown DocumentDeletionException when request is invalid");
        } catch (final DocumentDeletionException documentDeletionException) {
            assertThat(documentDeletionException.getMessage())
                .isEqualTo("Error deleting documents for case : 1234567890123456");
        }
    }

    @Test
    void shouldThrowExceptionWhenResponseIsUnableToMapToObject() {
        try {
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

            disposeDocumentsRemoteOperation.delete(caseData);

            fail("The method should have thrown JsonParseException when request is invalid");
        } catch (final DocumentDeletionException documentDeletionException) {
            assertThat(documentDeletionException.getCause().getMessage())
                .contains("Unable to map json to object document deletion endpoint response due to following "
                              + "endpoint response:");
        }
    }

    @Test
    void shouldNotDeleteHearings() {
        final CaseData caseData = CaseData.builder()
            .reference(1234567890123456L)
            .caseType(HEARING_RECORDINGS_CASE_TYPE).build();

        disposeDocumentsRemoteOperation.delete(caseData);

        verifyNoInteractions(documentDeletionRecordHolder);
        verifyNoInteractions(documentClient);

    }
}
