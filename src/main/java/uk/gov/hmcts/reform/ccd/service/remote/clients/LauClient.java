package uk.gov.hmcts.reform.ccd.service.remote.clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.ccd.data.lau.CaseActionPostRequestResponse;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.LAU_SAVE_PATH;

@FeignClient(name = "lauClient", url = "${remote.log.and.audit.host}")
public interface LauClient {

    @PostMapping(
        value = LAU_SAVE_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CaseActionPostRequestResponse> postLauAudit(
        @RequestHeader("ServiceAuthorization") String serviceAuthHeader,
        @RequestBody final CaseActionPostRequestResponse caseActionPostRequestResponse
    );

}
