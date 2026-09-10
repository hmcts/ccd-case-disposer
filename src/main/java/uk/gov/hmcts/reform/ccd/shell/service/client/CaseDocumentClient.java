package uk.gov.hmcts.reform.ccd.shell.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.ccd.shell.data.CaseDocumentHashResponse;

import java.util.UUID;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.SERVICE_AUTHORISATION_HEADER;

@FeignClient(name = "case-document-client", url = "${remote.ccd-document-am.host}")
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface CaseDocumentClient {

    @GetMapping("/cases/documents/{documentId}/token")
    CaseDocumentHashResponse getDocumentHash(
        @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthHeader,
        @RequestHeader(AUTHORISATION_HEADER) String authHeader,
        @PathVariable("documentId") UUID documentId
    );

}
