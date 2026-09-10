package uk.gov.hmcts.reform.ccd.shell.service;

import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.ccd.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.model.ShellDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CaseDocumentResolver {
    private static final String DOCUMENT_URL = "document_url";
    private static final String DOCUMENT_BINARY_URL = "document_binary_url";

    public List<ShellDocument> resolveDocuments(JsonNode mappedData) {
        List<ShellDocument> documents = new ArrayList<>();
        findDocuments(mappedData, documents);
        return List.copyOf(documents);
    }

    private void findDocuments(JsonNode node, List<ShellDocument> documents) {
        if (node.isObject()) {
            resolveDocument((ObjectNode) node).ifPresent(documents::add);
        }

        if (node.isContainer()) {
            node.forEach(child -> findDocuments(child, documents));
        }
    }

    private Optional<ShellDocument> resolveDocument(ObjectNode node) {
        JsonNode documentUrl = node.get(DOCUMENT_URL);
        JsonNode documentBinaryUrl = node.get(DOCUMENT_BINARY_URL);
        if (documentUrl == null || documentBinaryUrl == null) {
            return Optional.empty();
        }

        UUID documentId = extractDocumentId(documentUrl.asString());
        return Optional.of(new ShellDocument(documentId, node));
    }

    private UUID extractDocumentId(String documentUrl) {
        try {
            String documentId = documentUrl.substring(documentUrl.lastIndexOf('/') + 1);
            return UUID.fromString(documentId);
        } catch (RuntimeException exception) {
            throw new ShellCaseException("Unable to resolve document ID from mapped shell case data", exception);
        }
    }
}
