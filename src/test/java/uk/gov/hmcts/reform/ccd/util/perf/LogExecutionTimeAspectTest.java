package uk.gov.hmcts.reform.ccd.util.perf;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogExecutionTimeAspectTest {
    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @Mock
    private LogExecutionTime annotation;

    @InjectMocks
    private LogExecutionTimeAspect aspect;

    @Test
    void shouldProceedAndLogUsingAnnotationValue() throws Throwable {
        when(annotation.value()).thenReturn("custom.metric");
        when(joinPoint.proceed()).thenReturn("result");

        Object result = aspect.logExecutionTime(joinPoint, annotation);

        assertThat(result).isEqualTo("result");
        verify(joinPoint).proceed();
    }

    @Test
    void shouldFallbackToMethodSignatureWhenAnnotationValueBlank() throws Throwable {
        when(annotation.value()).thenReturn("");
        when(signature.toShortString()).thenReturn("SomeClass.delete(..)");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.proceed()).thenReturn(null);

        aspect.logExecutionTime(joinPoint, annotation);

        verify(joinPoint).proceed();
    }

    @Test
    void shouldProceedAndLogForRemoteOperation() throws Throwable {
        when(signature.toShortString()).thenReturn("SomeClass.delete(..)");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.proceed()).thenReturn(null);

        aspect.logRemoteExecutionTime(joinPoint);

        verify(joinPoint).proceed();
        verify(joinPoint).getSignature();
    }

}
