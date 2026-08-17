package uk.gov.hmcts.reform.ccd.shell.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.shell.model.ShellMappingResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ShellMappingsCacheTest {

    private static final String PROBATE_SERVICE = "PROBATE";

    private ShellMappingsCache shellMappingsCache;

    @BeforeEach
    void setUp() {
        shellMappingsCache = new ShellMappingsCache();
    }

    @Test
    void shouldStoreAndRetrieveMappingsByCaseType() {
        ShellMappingResponse probateMapping = new ShellMappingResponse("PROBATE_SHELL");
        ShellMappingResponse civilMapping = new ShellMappingResponse("CIVIL_SHELL");

        shellMappingsCache.putAll(Map.of(
            PROBATE_SERVICE, probateMapping,
            "CIVIL", civilMapping
        ));

        assertThat(shellMappingsCache.getByCaseType(PROBATE_SERVICE)).isSameAs(probateMapping);
        assertThat(shellMappingsCache.getByCaseType("CIVIL")).isSameAs(civilMapping);
    }

    @Test
    void shouldNotChangeStateWhenPutAllIsNullOrEmpty() {
        shellMappingsCache.putAll(null);
        shellMappingsCache.putAll(Map.of());

        assertThat(shellMappingsCache.getByCaseType("UNKNOWN")).isNull();
    }

    @Test
    void shouldClearStoredMappings() {
        shellMappingsCache.putAll(Map.of(PROBATE_SERVICE, new ShellMappingResponse("PROBATE_SHELL")));

        shellMappingsCache.clear();

        assertThat(shellMappingsCache.getByCaseType(PROBATE_SERVICE)).isNull();
    }
}

