package uk.gov.hmcts.reform.ccd.shell.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.shell.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.model.ShellMappingResponse;
import uk.gov.hmcts.reform.ccd.shell.service.client.ShellMappingClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class ShellMappingService {
    private final ShellMappingClient shellMappingClient;
    private final SecurityUtil securityUtil;

    private final Map<String, ShellMappingResponse> cache = new ConcurrentHashMap<>();

    public ShellMappingResponse loadMappings(String caseTypeId) {
        return cache.computeIfAbsent(caseTypeId, this::retrieveMappings);
    }

    private ShellMappingResponse retrieveMappings(String caseTypeId) {
        ShellMappingResponse response;
        try {
            response = shellMappingClient.getShellMappings(
                securityUtil.getServiceAuthorization(),
                securityUtil.getIdamClientToken(),
                caseTypeId
            );
        }  catch (FeignException exc) {
            log.warn("Failed to retrieve mappings for case type {}", caseTypeId);
            throw new ShellCaseException("Failed to retrieve mappings for case type " + caseTypeId, exc);
        }

        if (response == null) {
            throw new ShellCaseException("Shell mapping response was null for case type " + caseTypeId);
        }

        if (response.shellCaseTypeID() != null && response.shellCaseMappings() == null) {
            throw new ShellCaseException("Shell mapping response was invalid for case type " + caseTypeId);
        }

        log.info("Shell mapping for case type {} returned {}", caseTypeId, response);
        return response;
    }

    public Map<String, ShellMappingResponse> getShellMappings(List<String> caseTypes) {
        if (caseTypes == null || caseTypes.isEmpty()) {
            return Map.of();
        }

        return caseTypes.stream()
            .collect(Collectors.toMap(
                Function.identity(),
                this::loadMappings
            ));
    }

    public void clearCache() {
        cache.clear();
    }
}
