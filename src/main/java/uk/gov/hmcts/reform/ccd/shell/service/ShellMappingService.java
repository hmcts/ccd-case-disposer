package uk.gov.hmcts.reform.ccd.shell.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    private final Map<String, ShellMappingResponse> cache =
        new ConcurrentHashMap<>();

    public ShellMappingResponse loadMappings(String caseTypeId) {
        return cache.computeIfAbsent(caseTypeId, this::retrieveMappings);
    }

    private ShellMappingResponse retrieveMappings(String caseTypeId) {
        ShellMappingResponse response = shellMappingClient.getShellMappings(
            securityUtil.getServiceAuthorization(),
            securityUtil.getIdamClientToken(),
            caseTypeId
        );

        if (response == null) {
            log.info("Shell mapping response was empty for caseTypeId={}, continuing without shell mappings",
                caseTypeId
            );
            return emptyResponse();
        }

        return response;
    }

    private ShellMappingResponse emptyResponse() {
        return new ShellMappingResponse(null);
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
