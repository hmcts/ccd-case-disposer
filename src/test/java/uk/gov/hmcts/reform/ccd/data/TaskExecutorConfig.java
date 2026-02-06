package uk.gov.hmcts.reform.ccd.data;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;

@TestConfiguration
public class TaskExecutorConfig {
    @Bean
    TaskExecutor taskExecutor() {
        return Runnable::run; // synchronous for tests
    }
}
