package uk.gov.hmcts.reform.ccd.util.perf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogExecutionTime {
    /**
     * Optional logical name for the metric.
     * Defaults to class.method.
     */
    String value() default "";
}
