package uk.gov.hmcts.reform.ccd.shell.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.ccd.shell.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.model.CaseDocumentHashResponse;
import uk.gov.hmcts.reform.ccd.shell.service.client.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ShellDocumentService {

    private static final String DOCUMENT_URL = "document_url";
    private static final String DOCUMENT_BINARY_URL = "document_binary_url";

    private final CaseDocumentClient documentClient;
    private final SecurityUtil securityUtil;

    public void appendDocumentHashes(JsonNode mappedData) {
        List<ShellDocument> documents = resolveDocuments(mappedData);
        Map<UUID, String> hashes = fetchHashes(documents);
        appendHash(documents, hashes);
    }

    private Map<UUID, String> fetchHashes(List<ShellDocument> documents) {
        Map<UUID, String> hashes = new ConcurrentHashMap<>();
        for (ShellDocument document : documents) {
            hashes.computeIfAbsent(document.documentId(), this::fetchDocumentHash);
        }
        return Map.copyOf(hashes);
    }

    private String fetchDocumentHash(UUID documentId) {
        try {
            CaseDocumentHashResponse response = documentClient.getDocumentHash(
                securityUtil.getServiceAuthorization(),
                securityUtil.getIdamClientToken(),
                documentId);

            if (response == null || response.hashToken() == null || response.hashToken().isBlank()) {
                throw new ShellCaseException("CCD Document AM returned an invalid hash for " + documentId);
            }
            return  response.hashToken();
        } catch (FeignException exc) {
            throw new ShellCaseException("Failed to fetch document hash for " + documentId, exc);
        }
    }

    private void appendHash(List<ShellDocument> documents, Map<UUID, String> hashes) {
        for (ShellDocument document : documents) {
            String hash = hashes.get(document.documentId());
            if (hash == null) {
                throw new ShellCaseException("No hash resolved for document " + document.documentId());
            }
            document.documentNode().put("document_hash", hash);
        }
    }

    private List<ShellDocument> resolveDocuments(JsonNode mappedData) {
        List<ShellDocument> documents = new ArrayList<>();
        findDocuments(mappedData, documents);
        return List.copyOf(documents);
    }

    private void findDocuments(JsonNode node, List<ShellDocument> documents) {
        if (node.isObject()) {
            Optional<ShellDocument> document = resolveDocument((ObjectNode) node);
            if (document.isPresent()) {
                documents.add(document.get());
                return;
            }
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

    private record ShellDocument(
        UUID documentId,
        ObjectNode documentNode
    ) {
    }
}
