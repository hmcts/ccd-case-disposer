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
    void testShouldParseExpirableCaseTypesCorrectly() {
        final List<String> expectedExpirableCaseTypes = List.of("aaa", "bbb", "ccc");

        final List<String> actualExpirableCaseTypes = underTest.getExpirableCaseTypes();

        assertThat(actualExpirableCaseTypes)
            .isNotEmpty()
            .hasSameElementsAs(expectedExpirableCaseTypes);
    }
}
