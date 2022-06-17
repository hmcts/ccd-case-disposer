package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;
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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_DOCUMENT_PATH;

@DisplayName("dispose case documents")
@ExtendWith(MockitoExtension.class)
class DisposeDocumentsRemoteOperationTest {

    @Mock
    private RestClientBuilder restClientBuilder;

    @Mock
    private DocumentDeletionRecordHolder documentDeletionRecordHolder;

    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private DisposeDocumentsRemoteOperation disposeDocumentsRemoteOperation;

    @BeforeEach
    void setUp() {
        doReturn("http://localhost").when(parameterResolver).getDocumentStoreHost();
    }

    @Test
    void shouldPostDocumentsDeleteRemoteDisposeRequestWithoutAnomaly() {

        final String jsonRequest = new Gson().toJson(new DocumentsDeletePostRequest("1234567890123456"));
        final String jsonResponse = new Gson().toJson(new CaseDocumentsDeletionResults(1, 1));

        when(restClientBuilder.postRequestWithServiceAuthHeader("http://localhost", DELETE_DOCUMENT_PATH, jsonRequest)).thenReturn(jsonResponse);

        disposeDocumentsRemoteOperation.postDocumentsDelete("1234567890123456");

        verify(documentDeletionRecordHolder, times(1)).setCaseDocumentsDeletionResults(eq("1234567890123456"),
                any(CaseDocumentsDeletionResults.class));
        verify(restClientBuilder, times(1)).postRequestWithServiceAuthHeader("http://localhost", DELETE_DOCUMENT_PATH, jsonRequest);
    }

    @Test
    void shouldThrowExceptionWhenRequestInvalid() {
        try {
            final String jsonRequest = new Gson().toJson(new DocumentsDeletePostRequest("1234567890123456"));

            doThrow(new DocumentDeletionException("1234567890123456"))
                    .when(restClientBuilder)
                    .postRequestWithServiceAuthHeader("http://localhost", DELETE_DOCUMENT_PATH,
                            jsonRequest);

            disposeDocumentsRemoteOperation.postDocumentsDelete("1234567890123456");

            fail("The method should have thrown DocumentDeletionException when request is invalid");
        } catch (final DocumentDeletionException documentDeletionException) {
            assertThat(documentDeletionException.getMessage())
                    .isEqualTo("Error deleting documents for case : 1234567890123456");
        }
    }
}
