package uk.gov.hmcts.reform.ccd.util.perf;

import com.google.common.base.Stopwatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Aspect
@Component
@ConditionalOnBooleanProperty(prefix = "performance.logging", name = "enabled")
public class LogExecutionTimeAspect {
    private static final Logger LOG =
        LoggerFactory.getLogger(LogExecutionTimeAspect.class.getPackageName());

    @Around("execution(* uk.gov.hmcts.reform.ccd.service.remote.DisposeRemoteOperation.delete(..))")
    public Object logRemoteExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return proceed(joinPoint, joinPoint.getSignature().toShortString());
    }

    @Around(value = "@annotation(annotation)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, LogExecutionTime annotation) throws Throwable {
        String givenValue = annotation.value();
        String metricName = givenValue.isBlank() ? joinPoint.getSignature().toShortString() : givenValue;
        return proceed(joinPoint, metricName);
    }

    private Object proceed(ProceedingJoinPoint joinPoint, String name) throws Throwable {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            return joinPoint.proceed();
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Performance: {} duration={}", name, format(stopwatch.elapsed()));
            }
        }
    }

    private String format(Duration duration) {
        long ms = duration.toMillis();
        if (ms < 1000) {
            return ms + "ms";
        }
        return String.format("%.2fs", ms / 1000.0);
    }
}
