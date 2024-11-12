package uk.gov.hmcts.reform.ccd.service.remote.clients;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_HEARINGS_PATH;

@FeignClient(name = "hearingClient", url = "${ccd.hearing.host}")
public interface HearingClient {

    @DeleteMapping(
        value = DELETE_HEARINGS_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    Response deleteHearing(
        @RequestHeader("Authorization") String authHeader,
        @RequestHeader("ServiceAuthorization") String serviceAuthHeader,
        @RequestBody final List<String> ccdCaseIds
    );

}
