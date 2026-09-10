package uk.gov.hmcts.reform.ccd.shell.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.ccd.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.model.ShellDocument;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShellDocumentHashAppenderTest {

    private static final UUID DOCUMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String HASH_TOKEN = "hash-token";

    @InjectMocks
    private ShellDocumentHashAppender underTest;

    @Test
    void shouldAppendHashToDocumentNode() {
        ObjectNode documentNode = JsonNodeFactory.instance.objectNode()
            .put("document_url", "http://localhost:4455/documents/" + DOCUMENT_ID);
        ShellDocument document = new ShellDocument(DOCUMENT_ID, documentNode);

        underTest.appendHash(List.of(document), Map.of(DOCUMENT_ID, HASH_TOKEN));

        assertThat(documentNode.get("document_hash").asString()).isEqualTo(HASH_TOKEN);
    }

    @Test
    void shouldAppendSameHashToEachOccurrenceOfDocument() {
        ObjectNode firstNode = JsonNodeFactory.instance.objectNode();
        ObjectNode secondNode = JsonNodeFactory.instance.objectNode();
        List<ShellDocument> documents = List.of(
            new ShellDocument(DOCUMENT_ID, firstNode),
            new ShellDocument(DOCUMENT_ID, secondNode)
        );

        underTest.appendHash(documents, Map.of(DOCUMENT_ID, HASH_TOKEN));

        assertThat(firstNode.get("document_hash").asString()).isEqualTo(HASH_TOKEN);
        assertThat(secondNode.get("document_hash").asString()).isEqualTo(HASH_TOKEN);
    }

    @Test
    void shouldFailWhenDocumentHashIsMissing() {
        ShellDocument document = new ShellDocument(DOCUMENT_ID, JsonNodeFactory.instance.objectNode());

        assertThatThrownBy(() -> underTest.appendHash(List.of(document), Map.of()))
            .isInstanceOf(ShellCaseException.class)
            .hasMessage("No hash resolved for document " + DOCUMENT_ID);
    }
}
