package uk.gov.hmcts.reform.ccd.shell.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.ccd.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.model.ShellDocument;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaseDocumentResolverTest {

    private static final UUID FIRST_DOCUMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SECOND_DOCUMENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private CaseDocumentResolver underTest;

    @BeforeEach
    void setUp() {
        underTest = new CaseDocumentResolver();
    }

    @Test
    void shouldResolveDocumentsNestedInObjectsAndArrays() {
        /*
        Constructing this document:
        {
          "details": {
            "document": {
              "document_url": "http://localhost:4455/documents/11111111-1111-1111-1111-111111111111",
              "document_binary_url": "http://localhost:4455/documents/11111111-1111-1111-1111-111111111111/binary",
              "name": "case details.pdf"
            }
          },
          "documents": [
            {
              "value": {
                "document_url": "http://localhost:4455/documents/22222222-2222-2222-2222-222222222222",
                "document_binary_url": "http://localhost:4455/documents/22222222-2222-2222-2222-222222222222/binary"
              }
            }
          ]
        }
        */

        ObjectNode mappedData = JsonNodeFactory.instance.objectNode();
        ObjectNode firstDocument = documentNode(FIRST_DOCUMENT_ID);
        firstDocument.put("name", "case details.pdf");
        mappedData.putObject("details").set("document", firstDocument);
        ArrayNode collection = mappedData.putArray("documents");
        ObjectNode secondDocument = documentNode(SECOND_DOCUMENT_ID);
        collection.addObject().set("value", secondDocument);

        List<ShellDocument> result = underTest.resolveDocuments(mappedData);

        assertThat(result).extracting(ShellDocument::documentId)
            .containsExactly(FIRST_DOCUMENT_ID, SECOND_DOCUMENT_ID);
        assertThat(result.get(0).documentNode()).isSameAs(firstDocument);
        assertThat(result.get(1).documentNode()).isSameAs(secondDocument);
    }

    @Test
    void shouldReturnEmptyListWhenMappedDataContainsNoDocuments() {
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode();
        mappedData.putObject("details").put("name", "case details");

        List<ShellDocument> result = underTest.resolveDocuments(mappedData);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldIgnoreObjectWithoutBothDocumentUrls() {
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode();
        mappedData.putObject("incompleteDocument")
            .put("document_url", documentUrl(FIRST_DOCUMENT_ID));

        List<ShellDocument> result = underTest.resolveDocuments(mappedData);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldWrapInvalidDocumentId() {
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode();
        mappedData.putObject("document")
            .put("document_url", "http://localhost:4455/documents/not-a-uuid")
            .put("document_binary_url", "http://localhost:4455/documents/not-a-uuid/binary");

        assertThatThrownBy(() -> underTest.resolveDocuments(mappedData))
            .isInstanceOf(ShellCaseException.class)
            .hasMessage("Unable to resolve document ID from mapped shell case data");
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
