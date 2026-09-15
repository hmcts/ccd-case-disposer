package uk.gov.hmcts.reform.ccd.shell.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.ccd.shell.data.CcdCaseResponse;
import uk.gov.hmcts.reform.ccd.shell.data.CcdCaseSearchResponse;
import uk.gov.hmcts.reform.ccd.shell.data.CcdCreateCaseEventResponse;
import uk.gov.hmcts.reform.ccd.shell.model.ShellCasePayload;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.SERVICE_AUTHORISATION_HEADER;

@FeignClient(name = "ccd-client", url = "${remote.ccd-data-store.host}")
public interface CcdClient {

    @GetMapping(value = "/cases/{caseReference}", headers = "Experimental=true")
    CcdCaseResponse getOriginalCaseData(
        @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthHeader,
        @RequestHeader(AUTHORISATION_HEADER) String authHeader,
        @PathVariable("caseReference") long caseReference
    );

    @PostMapping("/searchCases")
    CcdCaseSearchResponse searchCase(
        @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthHeader,
        @RequestHeader(AUTHORISATION_HEADER) String authHeader,
        @RequestParam("ctid") String caseType,
        @RequestBody JsonNode searchRequest
    );

    @GetMapping(value = "/case-types/{caseType}/event-triggers/{triggerId}", headers = "Experimental=true")
    CcdCreateCaseEventResponse getCreateCaseToken(
        @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthHeader,
        @RequestHeader(AUTHORISATION_HEADER) String authHeader,
        @PathVariable("triggerId") String triggerId,
        @PathVariable("caseType") String caseType
    );

    @PostMapping(value = "/case-types/{caseType}/cases", headers = "Experimental=true")
    void createCase(
        @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthHeader,
        @RequestHeader(AUTHORISATION_HEADER) String authHeader,
        @PathVariable("caseType") String caseType,
        @RequestBody ShellCasePayload payload
        );
}
