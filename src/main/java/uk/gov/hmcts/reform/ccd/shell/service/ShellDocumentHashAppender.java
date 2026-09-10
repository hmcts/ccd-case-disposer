package uk.gov.hmcts.reform.ccd.shell.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.model.ShellDocument;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ShellDocumentHashAppender {

    public void appendHash(List<ShellDocument> documents, Map<UUID, String> hashes) {
        for (ShellDocument document : documents) {
            String hash = hashes.get(document.documentId());
            if (hash == null) {
                throw new ShellCaseException("No hash resolved for document " + document.documentId());
            }
            document.documentNode().put("document_hash", hash);
        }
    }
}
