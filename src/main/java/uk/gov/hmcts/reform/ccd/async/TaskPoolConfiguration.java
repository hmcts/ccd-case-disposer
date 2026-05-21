package uk.gov.hmcts.reform.ccd.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TaskPoolConfiguration {

    private final ParameterResolver parameterResolver;

    @Bean(name = "caseDeletionExecutor")
    public ThreadPoolTaskExecutor caseDeletionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(parameterResolver.getThreadMaxPoolSize());
        executor.setCorePoolSize(parameterResolver.getThreadCorePoolSize());
        executor.setQueueCapacity(parameterResolver.getThreadQueueCapacity());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("case-deletion-");

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(300);

        executor.initialize();
        return executor;
    }
}
