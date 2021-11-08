package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = {ApplicationParameters.class})
class ApplicationParametersIntegrationTest {
    @Inject
    private ApplicationParameters underTest;

    @Test
    void testShouldParseDeletableCaseTypesCorrectly() {
        final List<String> expectedDeletableCaseTypes = List.of("deletable_case_type", "bbb", "ccc");

        final List<String> actualDeletableCaseTypes = underTest.getDeletableCaseTypes();

        assertThat(actualDeletableCaseTypes)
            .isNotEmpty()
            .hasSameElementsAs(expectedDeletableCaseTypes);
    }
}
