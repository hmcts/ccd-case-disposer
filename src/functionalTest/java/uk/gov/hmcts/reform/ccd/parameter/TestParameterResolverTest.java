package uk.gov.hmcts.reform.ccd.parameter;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.parameter.TestParameterResolver.DELETABLE_CASE_TYPES_PROPERTY;

@ActiveProfiles("functional")
@SpringBootTest(classes = {TestParameterResolver.class})
class TestParameterResolverTest {

    @Inject
    private TestParameterResolver underTest;

    @BeforeEach
    void prepare() {
        System.clearProperty(DELETABLE_CASE_TYPES_PROPERTY);
    }

    @Test
    void testGetDeletableCaseTypesWhenNoDeletableCaseTypesPresent() {
        final List<String> result = underTest.getDeletableCaseTypes();

        assertThat(result).isEmpty();
    }

    @Test
    void testGetDeletableCaseTypesWhenSingleDeletableCaseTypeIsPresent() {
        System.setProperty(DELETABLE_CASE_TYPES_PROPERTY, "CT1");

        final List<String> result = underTest.getDeletableCaseTypes();

        assertThat(result)
            .isNotEmpty()
            .hasSameElementsAs(List.of("CT1"));
    }

    @Test
    void testGetDeletableCaseTypesWhenMultipleDeletableCaseTypesArePresent() {
        System.setProperty(DELETABLE_CASE_TYPES_PROPERTY, "CT1, CT2");

        final List<String> result = underTest.getDeletableCaseTypes();

        assertThat(result)
            .isNotEmpty()
            .hasSameElementsAs(List.of("CT1", "CT2"));
    }

    @Test
    void testGetDeletableCaseTypesWhenDeletableCaseTypesChangesValue() {
        System.setProperty(DELETABLE_CASE_TYPES_PROPERTY, "CT1, CT2");

        final List<String> initialResult = underTest.getDeletableCaseTypes();

        assertThat(initialResult)
            .isNotEmpty()
            .hasSameElementsAs(List.of("CT1", "CT2"));

        System.setProperty(DELETABLE_CASE_TYPES_PROPERTY, "CT1");

        final List<String> result = underTest.getDeletableCaseTypes();

        assertThat(result)
            .isNotEmpty()
            .hasSameElementsAs(List.of("CT1"));
    }

}
