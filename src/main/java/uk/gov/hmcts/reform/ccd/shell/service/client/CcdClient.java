package uk.gov.hmcts.reform.ccd.shell.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.ccd.shell.data.CcdCaseResponse;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.SERVICE_AUTHORISATION_HEADER;

@FeignClient(name = "ccd-client", url = "${remote.ccd-data-store.host}")
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface CcdClient {

    @GetMapping(value = "/cases/{caseReference}", headers = "Experimental=true")
    CcdCaseResponse getOriginalCaseData(
        @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthHeader,
        @RequestHeader(AUTHORISATION_HEADER) String authHeader,
        @PathVariable("caseReference") long caseReference
    );
}
