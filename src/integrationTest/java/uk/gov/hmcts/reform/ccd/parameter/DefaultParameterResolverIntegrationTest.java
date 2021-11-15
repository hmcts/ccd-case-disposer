package uk.gov.hmcts.reform.ccd.parameter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = {DefaultParameterResolver.class})
class DefaultParameterResolverIntegrationTest {
    @Inject
    private DefaultParameterResolver underTest;

    @Test
    void testShouldParseDeletableCaseTypesCorrectly() {
        final List<String> expectedDeletableCaseTypes = List.of("deletable_case_type", "bbb", "ccc");

        final List<String> actualDeletableCaseTypes = underTest.getDeletableCaseTypes();

        assertThat(actualDeletableCaseTypes)
            .isNotEmpty()
            .hasSameElementsAs(expectedDeletableCaseTypes);
    }

    @Test
    void testShouldGetDefaultCasesIndexNamePattern() {
        final String indexNamePattern = underTest.getCasesIndexNamePattern();

        assertThat(indexNamePattern)
            .isNotNull()
            .isEqualTo("%s_cases");
    }

    @Test
    void testShouldGetDefaultCasesIndexType() {
        final String type = underTest.getCasesIndexType();

        assertThat(type)
            .isNotNull()
            .isEqualTo("_doc");
    }
}
