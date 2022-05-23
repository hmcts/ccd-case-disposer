package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.parameter.DefaultParameterResolver;

import java.util.List;
import javax.inject.Inject;

import static java.lang.System.clearProperty;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.config.TestParameterResolver.DELETABLE_CASE_TYPES_PROPERTY;
import static uk.gov.hmcts.reform.ccd.config.TestParameterResolver.DELETABLE_CASE_TYPES_PROPERTY_SIMULATION;

@ActiveProfiles("test")
@SpringBootTest(classes = {DefaultParameterResolver.class})
class DefaultParameterResolverIntegrationTest {
    @Inject
    private DefaultParameterResolver defaultParameterResolver;

    @BeforeAll
    static void setUp() {
        clearProperty(DELETABLE_CASE_TYPES_PROPERTY_SIMULATION);
        clearProperty(DELETABLE_CASE_TYPES_PROPERTY);
    }

    @Test
    void testShouldParseDeletableCaseTypesCorrectly() {
        final List<String> expectedDeletableCaseTypes = List.of("deletable_case_type", "bbb", "ccc");

        final List<String> actualDeletableCaseTypes = defaultParameterResolver.getDeletableCaseTypes();

        assertThat(actualDeletableCaseTypes)
                .isNotEmpty()
                .hasSameElementsAs(expectedDeletableCaseTypes);
    }

    @Test
    void testShouldParseSimulationCaseTypesCorrectly() {
        final List<String> expectedSimulationsCaseTypes = List.of("deletable_case_type_simulation", "ttt", "yyy");

        final List<String> actualSimulationCaseTypes = defaultParameterResolver.getDeletableCaseTypesSimulation();

        assertThat(actualSimulationCaseTypes)
                .isNotEmpty()
                .hasSameElementsAs(expectedSimulationsCaseTypes);
    }

    @Test
    void testShouldGetDefaultCasesIndexNamePattern() {
        final String indexNamePattern = defaultParameterResolver.getCasesIndexNamePattern();

        assertThat(indexNamePattern)
                .isNotNull()
                .isEqualTo("%s_cases");
    }

    @Test
    void testShouldGetDefaultCasesIndexType() {
        final String type = defaultParameterResolver.getCasesIndexType();

        assertThat(type)
                .isNotNull()
                .isEqualTo("_doc");
    }
}
