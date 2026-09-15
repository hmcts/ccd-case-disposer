package uk.gov.hmcts.reform.ccd.shell.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.ccd.shell.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.model.CaseDocumentHashResponse;
import uk.gov.hmcts.reform.ccd.shell.service.client.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShellDocumentServiceTest {

    private static final String SERVICE_TOKEN = "service-token";
    private static final String USER_TOKEN = "user-token";
    private static final String DOCUMENT_FIELD = "document";
    private static final String DOCUMENT_HASH_FIELD = "document_hash";
    private static final UUID FIRST_DOCUMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SECOND_DOCUMENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private CaseDocumentClient documentClient;
    @Mock
    private SecurityUtil securityUtil;

    private ShellDocumentService underTest;

    @BeforeEach
    void setUp() {
        underTest = new ShellDocumentService(documentClient, securityUtil);
    }

    @Test
    void shouldAppendHashesToDocumentsNestedInObjectsAndArrays() {
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode();
        ObjectNode firstDocument = documentNode(FIRST_DOCUMENT_ID);
        firstDocument.put("name", "case details.pdf");
        mappedData.putObject("details").set(DOCUMENT_FIELD, firstDocument);
        ArrayNode collection = mappedData.putArray("documents");
        ObjectNode secondDocument = documentNode(SECOND_DOCUMENT_ID);
        collection.addObject().set("value", secondDocument);
        mockTokens();
        when(documentClient.getDocumentHash(SERVICE_TOKEN, USER_TOKEN, FIRST_DOCUMENT_ID))
            .thenReturn(new CaseDocumentHashResponse("first-hash"));
        when(documentClient.getDocumentHash(SERVICE_TOKEN, USER_TOKEN, SECOND_DOCUMENT_ID))
            .thenReturn(new CaseDocumentHashResponse("second-hash"));

        underTest.appendDocumentHashes(mappedData);

        assertThat(firstDocument.get(DOCUMENT_HASH_FIELD).asString()).isEqualTo("first-hash");
        assertThat(secondDocument.get(DOCUMENT_HASH_FIELD).asString()).isEqualTo("second-hash");
    }

    @Test
    void shouldFetchHashOnceAndAppendItToEveryOccurrenceOfDocument() {
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode();
        ObjectNode firstDocument = documentNode(FIRST_DOCUMENT_ID);
        ObjectNode duplicateDocument = documentNode(FIRST_DOCUMENT_ID);
        mappedData.putArray("documents").add(firstDocument).add(duplicateDocument);
        mockTokens();
        when(documentClient.getDocumentHash(SERVICE_TOKEN, USER_TOKEN, FIRST_DOCUMENT_ID))
            .thenReturn(new CaseDocumentHashResponse("hash-token"));

        underTest.appendDocumentHashes(mappedData);

        assertThat(firstDocument.get(DOCUMENT_HASH_FIELD).asString()).isEqualTo("hash-token");
        assertThat(duplicateDocument.get(DOCUMENT_HASH_FIELD).asString()).isEqualTo("hash-token");
        verify(documentClient, times(1)).getDocumentHash(SERVICE_TOKEN, USER_TOKEN, FIRST_DOCUMENT_ID);
    }

    @Test
    void shouldDoNothingWhenMappedDataContainsNoDocuments() {
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode();
        mappedData.putObject("details").put("name", "case details");

        underTest.appendDocumentHashes(mappedData);

        verifyNoInteractions(documentClient, securityUtil);
    }

    @Test
    void shouldIgnoreObjectWithoutBothDocumentUrls() {
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode();
        mappedData.putObject("incompleteDocument")
            .put("document_url", documentUrl(FIRST_DOCUMENT_ID));

        underTest.appendDocumentHashes(mappedData);

        verifyNoInteractions(documentClient, securityUtil);
    }

    @Test
    void shouldFailWhenDocumentHashIsMissing() {
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode();
        mappedData.set(DOCUMENT_FIELD, documentNode(FIRST_DOCUMENT_ID));
        mockTokens();
        when(documentClient.getDocumentHash(SERVICE_TOKEN, USER_TOKEN, FIRST_DOCUMENT_ID))
            .thenReturn(new CaseDocumentHashResponse(null));

        assertThatThrownBy(() -> underTest.appendDocumentHashes(mappedData))
            .isInstanceOf(ShellCaseException.class)
            .hasMessage("No hash resolved for document " + FIRST_DOCUMENT_ID);
    }

    @Test
    void shouldWrapDocumentClientFailure() {
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode();
        mappedData.set(DOCUMENT_FIELD, documentNode(FIRST_DOCUMENT_ID));
        FeignException clientFailure = mock(FeignException.class);
        mockTokens();
        when(documentClient.getDocumentHash(SERVICE_TOKEN, USER_TOKEN, FIRST_DOCUMENT_ID))
            .thenThrow(clientFailure);

        assertThatThrownBy(() -> underTest.appendDocumentHashes(mappedData))
            .isInstanceOf(ShellCaseException.class)
            .hasMessage("Failed to fetch document hash for " + FIRST_DOCUMENT_ID)
            .hasCause(clientFailure);
    }

    @Test
    void shouldWrapInvalidDocumentId() {
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode();
        mappedData.putObject(DOCUMENT_FIELD)
            .put("document_url", "http://localhost:4455/documents/not-a-uuid")
            .put("document_binary_url", "http://localhost:4455/documents/not-a-uuid/binary");

        assertThatThrownBy(() -> underTest.appendDocumentHashes(mappedData))
            .isInstanceOf(ShellCaseException.class)
            .hasMessage("Unable to resolve document ID from mapped shell case data");
    }

    private void mockTokens() {
        when(securityUtil.getServiceAuthorization()).thenReturn(SERVICE_TOKEN);
        when(securityUtil.getIdamClientToken()).thenReturn(USER_TOKEN);
    }

    private ObjectNode documentNode(UUID documentId) {
        return JsonNodeFactory.instance.objectNode()
            .put("document_url", documentUrl(documentId))
            .put("document_binary_url", documentUrl(documentId) + "/binary");
    }

    private String documentUrl(UUID documentId) {
        return "http://localhost:4455/documents/" + documentId;
    }
}
