package uk.gov.hmcts.reform.ccd.service.remote.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_DOCUMENT_PATH;

@FeignClient(name = "documentClient", url = "${ccd.document.store.host}")
public interface DocumentClient {

    @PostMapping(
        value = DELETE_DOCUMENT_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CaseDocumentsDeletionResults> deleteDocument(
        @RequestHeader("ServiceAuthorization") String serviceAuthHeader,
        @RequestBody final DocumentsDeletePostRequest documentsDeletePostRequest
    );

}
