package uk.gov.hmcts.reform.ccd.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.reform.ccd.exception.CaseDisposerAsyncException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@Slf4j
@RequiredArgsConstructor
public class TaskPoolConfiguration implements AsyncConfigurer {

    private final ParameterResolver parameterResolver;

    @Bean
    @Override
    public Executor getAsyncExecutor() {
        return getThreadPoolTaskExecutor();
    }

    @SuppressWarnings("PMD.DoNotUseThreads")
    public Executor getThreadPoolTaskExecutor() {
        log.info("Creating Async Task Executor");
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setMaxPoolSize(parameterResolver.getThreadMaxPoolSize());
        executor.setCorePoolSize(parameterResolver.getThreadCorePoolSize());
        executor.setQueueCapacity(parameterResolver.getThreadQueueCapacity());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("Case-Deletion-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            CaseDisposerAsyncException exc = new CaseDisposerAsyncException("Unhandled Case-Disposer thread "
                    + "exception", ex);
            log.error("Unhandled Case-Disposer thread exception", exc);
        };
    }
}
