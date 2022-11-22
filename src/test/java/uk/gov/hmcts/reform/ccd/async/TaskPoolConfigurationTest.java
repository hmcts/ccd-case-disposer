package uk.gov.hmcts.reform.ccd.async;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class TaskPoolConfigurationTest {

    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private TaskPoolConfiguration taskPoolConfiguration;

    @Test
    void shouldCreateInstanceOfAsyncConfigurer() {
        assertThat(taskPoolConfiguration).isInstanceOf(AsyncConfigurer.class);
    }

    @Test
    void shouldCreateExecutor() {
        doReturn(5).when(parameterResolver).getThreadCorePoolSize();
        doReturn(10).when(parameterResolver).getThreadMaxPoolSize();
        doReturn(200).when(parameterResolver).getThreadQueueCapacity();

        assertThat(taskPoolConfiguration.getAsyncExecutor()).isInstanceOf(Executor.class);
    }

    @Test
    void shouldCreateThreadPoolTaskExecutorExecutor() {
        doReturn(5).when(parameterResolver).getThreadCorePoolSize();
        doReturn(10).when(parameterResolver).getThreadMaxPoolSize();
        doReturn(200).when(parameterResolver).getThreadQueueCapacity();
        
        final ThreadPoolTaskExecutor threadPoolTaskExecutor =
                (ThreadPoolTaskExecutor) taskPoolConfiguration.getAsyncExecutor();

        assertThat(threadPoolTaskExecutor.getCorePoolSize()).isEqualTo(5);
        assertThat(threadPoolTaskExecutor.getMaxPoolSize()).isEqualTo(10);
        assertThat(threadPoolTaskExecutor.getThreadNamePrefix()).isEqualTo("Case-Deletion-thread-");
    }

    @Test
    void shouldCreateAsyncUncaughtExceptionHandler() {
        assertThat(taskPoolConfiguration.getAsyncUncaughtExceptionHandler())
                .isInstanceOf(AsyncUncaughtExceptionHandler.class);
    }
}