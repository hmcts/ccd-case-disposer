package uk.gov.hmcts.reform.ccd.concurent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.reform.ccd.exception.CaseDisposerAsyncException;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@Slf4j
public class TaskPoolConfiguration implements AsyncConfigurer {

    private static final int MAX_POOL_SIZE = 10;
    private static final int CORE_POOL_SIZE = 5;
    private static final int QUEUE_CAPACITY = 200;

    @Bean
    @Override
    public Executor getAsyncExecutor() {
        return getThreadPoolTaskExecutor();
    }


    public Executor getThreadPoolTaskExecutor() {
        log.info("Creating Async Task Executor");
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
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
