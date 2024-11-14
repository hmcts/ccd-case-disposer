package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.DocumentDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.DocumentDeletionRecordHolder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_DOCUMENT_PATH;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.HEARING_RECORDINGS_CASE_TYPE;

@DisplayName("dispose case documents")
@ExtendWith(MockitoExtension.class)
class DisposeDocumentsRemoteOperationTest {

    @Mock
    private CcdRestClientBuilder ccdRestClientBuilder;

    @Mock
    private DocumentDeletionRecordHolder documentDeletionRecordHolder;

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

        final String jsonRequest = new Gson().toJson(new DocumentsDeletePostRequest("1234567890123456"));
        final String jsonResponse = new Gson().toJson(new CaseDocumentsDeletionResults(1, 1));

        when(ccdRestClientBuilder.postRequestWithServiceAuthHeader(
            "http://localhost",
            DELETE_DOCUMENT_PATH,
            jsonRequest
        )).thenReturn(jsonResponse);
        doReturn("http://localhost").when(parameterResolver).getDocumentStoreHost();

        disposeDocumentsRemoteOperation.delete(caseData);

        verify(documentDeletionRecordHolder, times(1)).setCaseDocumentsDeletionResults(
            eq("1234567890123456"),
            any(CaseDocumentsDeletionResults.class)
        );
        verify(ccdRestClientBuilder, times(1)).postRequestWithServiceAuthHeader(
            "http://localhost",
            DELETE_DOCUMENT_PATH,
            jsonRequest
        );
    }

    @Test
    void shouldThrowExceptionWhenRequestInvalid() {
        try {
            final String jsonRequest = new Gson().toJson(new DocumentsDeletePostRequest("1234567890123456"));

            doThrow(new DocumentDeletionException("1234567890123456"))
                .when(ccdRestClientBuilder)
                .postRequestWithServiceAuthHeader("http://localhost", DELETE_DOCUMENT_PATH,
                                                  jsonRequest
                );

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
            final String jsonRequest = new Gson().toJson(new DocumentsDeletePostRequest("1234567890123456"));
            final String jsonResponse = new Gson().toJson(new CaseDocumentsDeletionResults(1, 1));

            doThrow(new JsonSyntaxException("Unable to map document post response to object"))
                .when(documentDeletionRecordHolder)
                .setCaseDocumentsDeletionResults(
                    eq("1234567890123456"),
                    any(CaseDocumentsDeletionResults.class)
                );

            when(ccdRestClientBuilder.postRequestWithServiceAuthHeader(
                "http://localhost",
                DELETE_DOCUMENT_PATH,
                jsonRequest
            ))
                .thenReturn(jsonResponse);
            doReturn("http://localhost").when(parameterResolver).getDocumentStoreHost();

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
        when(parameterResolver.getHearingCaseType()).thenReturn(HEARING_RECORDINGS_CASE_TYPE);
        final CaseData caseData = CaseData.builder()
            .reference(1234567890123456L)
            .caseType(HEARING_RECORDINGS_CASE_TYPE).build();

        disposeDocumentsRemoteOperation.delete(caseData);

        verifyNoInteractions(documentDeletionRecordHolder);
        verifyNoInteractions(ccdRestClientBuilder);

    }
}
