package uk.gov.hmcts.reform.ccd.async;

import org.junit.jupiter.api.Test;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

class TaskPoolConfigurationTest {

    @Test
    void shouldCreateInstanceOfAsyncConfigurer() {
        final TaskPoolConfiguration taskPoolConfiguration = new TaskPoolConfiguration();
        assertThat(taskPoolConfiguration).isInstanceOf(AsyncConfigurer.class);
    }

    @Test
    void shouldCreateExecutor() {
        final TaskPoolConfiguration taskPoolConfiguration = new TaskPoolConfiguration();
        assertThat(taskPoolConfiguration.getAsyncExecutor()).isInstanceOf(Executor.class);
    }

    @Test
    void shouldCreateThreadPoolTaskExecutorExecutor() {
        final TaskPoolConfiguration taskPoolConfiguration = new TaskPoolConfiguration();

        final ThreadPoolTaskExecutor threadPoolTaskExecutor =
                (ThreadPoolTaskExecutor) taskPoolConfiguration.getAsyncExecutor();

        assertThat(threadPoolTaskExecutor.getCorePoolSize()).isEqualTo(5);
        assertThat(threadPoolTaskExecutor.getMaxPoolSize()).isEqualTo(10);
        assertThat(threadPoolTaskExecutor.getThreadNamePrefix()).isEqualTo("Case-Deletion-thread-");
    }

    @Test
    void shouldCreateAsyncUncaughtExceptionHandler() {
        final TaskPoolConfiguration taskPoolConfiguration = new TaskPoolConfiguration();
        assertThat(taskPoolConfiguration.getAsyncUncaughtExceptionHandler())
                .isInstanceOf(AsyncUncaughtExceptionHandler.class);
    }
}