package uk.gov.hmcts.reform.ccd.shell.util;

import jakarta.inject.Named;
import uk.gov.hmcts.reform.ccd.shell.model.ShellMappingResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Named
public class ShellMappingsCache {

    private final Map<String, ShellMappingResponse> shellMappingsByCaseType = new ConcurrentHashMap<>();

    public void putAll(Map<String, ShellMappingResponse> shellMappings) {
        if (shellMappings == null || shellMappings.isEmpty()) {
            return;
        }
        shellMappingsByCaseType.putAll(shellMappings);
    }

    public ShellMappingResponse getByCaseType(String caseTypeId) {
        return shellMappingsByCaseType.get(caseTypeId);
    }

    public void clear() {
        shellMappingsByCaseType.clear();
    }
}

