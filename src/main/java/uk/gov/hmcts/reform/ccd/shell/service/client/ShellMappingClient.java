package uk.gov.hmcts.reform.ccd.shell.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.ccd.shell.model.ShellMappingResponse;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.SERVICE_AUTHORISATION_HEADER;

@FeignClient(
    name = "shell-mapping-service",
    // Missing shell mappings are expected for some case types; treat 404 as empty so processing continues.
    url = "${shell.mapping.url}",
    dismiss404 = true
)
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface ShellMappingClient {

    @GetMapping(
        value = "/api/retrieve-shell-mappings/{originalCaseTypeId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ShellMappingResponse getShellMappings(
        @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthHeader,
        @RequestHeader(AUTHORISATION_HEADER) String authHeader,
        @PathVariable("originalCaseTypeId") String originalCaseTypeId
    );
}
