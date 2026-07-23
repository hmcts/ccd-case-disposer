package uk.gov.hmcts.reform.ccd.parameter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.exception.DuplicateCaseTypeException;

import static java.util.List.of;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE_SIMULATION;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.NON_DELETABLE_CASE_TYPE;

@ExtendWith(MockitoExtension.class)
class DeletableCaseTypePropertyInterceptorTest {

    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private DeletableCaseTypePropertyInterceptor deletableCaseTypePropertyInterceptor;

    @Test
    void shouldNotThrowExceptionForUniqueCaseTypes() {
        doReturn(of(DELETABLE_CASE_TYPE, NON_DELETABLE_CASE_TYPE))
                .when(parameterResolver).getDeletableCaseTypes();
        doReturn(of(DELETABLE_CASE_TYPE_SIMULATION)).when(parameterResolver)
                .getDeletableCaseTypesSimulation();

        assertDoesNotThrow(deletableCaseTypePropertyInterceptor::init);
    }

    @Test
    void shouldNotThrowExceptionWhenCaseTypesAreEmptyStrings() {
        doReturn(of(""))
                .when(parameterResolver).getDeletableCaseTypes();
        doReturn(of(DELETABLE_CASE_TYPE_SIMULATION)).when(parameterResolver)
                .getDeletableCaseTypesSimulation();

        assertDoesNotThrow(deletableCaseTypePropertyInterceptor::init);
    }

    @Test
    void shouldThrowExceptionForNonUniqueCaseTypes() {

        doReturn(of(DELETABLE_CASE_TYPE, NON_DELETABLE_CASE_TYPE))
                .when(parameterResolver).getDeletableCaseTypes();
        doReturn(of(DELETABLE_CASE_TYPE_SIMULATION, DELETABLE_CASE_TYPE)).when(parameterResolver)
                .getDeletableCaseTypesSimulation();

        DuplicateCaseTypeException exception = assertThrows(
            DuplicateCaseTypeException.class,
            deletableCaseTypePropertyInterceptor::init);
        assertThat(exception.getMessage())
            .isEqualTo("Found duplicate deletable case type in application.yaml: [" + DELETABLE_CASE_TYPE + "]");
    }
}
