package uk.gov.hmcts.reform.ccd.shell.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.shell.model.ShellMappingResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ShellMappingsHolderTest {

    private ShellMappingsHolder shellMappingsHolder;

    @BeforeEach
    void setUp() {
        shellMappingsHolder = new ShellMappingsHolder();
    }

    @Test
    void shouldStoreAndRetrieveMappingsByCaseType() {
        ShellMappingResponse probateMapping = new ShellMappingResponse("PROBATE_SHELL");
        ShellMappingResponse civilMapping = new ShellMappingResponse("CIVIL_SHELL");

        shellMappingsHolder.putAll(Map.of(
            "PROBATE", probateMapping,
            "CIVIL", civilMapping
        ));

        assertThat(shellMappingsHolder.getByCaseType("PROBATE")).isSameAs(probateMapping);
        assertThat(shellMappingsHolder.getByCaseType("CIVIL")).isSameAs(civilMapping);
    }

    @Test
    void shouldNotChangeStateWhenPutAllIsNullOrEmpty() {
        shellMappingsHolder.putAll(null);
        shellMappingsHolder.putAll(Map.of());

        assertThat(shellMappingsHolder.getByCaseType("UNKNOWN")).isNull();
    }

    @Test
    void shouldClearStoredMappings() {
        shellMappingsHolder.putAll(Map.of("PROBATE", new ShellMappingResponse("PROBATE_SHELL")));

        shellMappingsHolder.clear();

        assertThat(shellMappingsHolder.getByCaseType("PROBATE")).isNull();
    }
}

