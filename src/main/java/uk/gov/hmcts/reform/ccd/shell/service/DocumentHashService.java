package uk.gov.hmcts.reform.ccd.shell.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.shell.model.ShellDocument;
import uk.gov.hmcts.reform.ccd.shell.service.client.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DocumentHashService {

    private final CaseDocumentClient documentClient;
    private final SecurityUtil securityUtil;

    public Map<UUID, String> fetchHashes(List<ShellDocument> documents) {
        Map<UUID, String> hashes = new ConcurrentHashMap<>();
        for (ShellDocument document : documents) {
            hashes.computeIfAbsent(
                document.documentId(),
                documentId -> documentClient.getDocumentHash(
                    securityUtil.getServiceAuthorization(),
                    securityUtil.getIdamClientToken(),
                    documentId).hashToken()
            );
        }

        return Map.copyOf(hashes);
    }
}
