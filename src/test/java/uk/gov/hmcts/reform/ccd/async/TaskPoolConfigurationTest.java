package uk.gov.hmcts.reform.ccd.async;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class TaskPoolConfigurationTest {

    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private TaskPoolConfiguration taskPoolConfiguration;

    @Test
    void shouldCreateThreadPoolTaskExecutorWithConfiguredValues() {
        // given
        doReturn(5).when(parameterResolver).getThreadCorePoolSize();
        doReturn(10).when(parameterResolver).getThreadMaxPoolSize();
        doReturn(200).when(parameterResolver).getThreadQueueCapacity();

        // when
        ThreadPoolTaskExecutor executor = taskPoolConfiguration.caseDeletionExecutor();

        // then
        assertThat(executor).isNotNull();
        assertThat(executor.getCorePoolSize()).isEqualTo(5);
        assertThat(executor.getMaxPoolSize()).isEqualTo(10);
        assertThat(executor.getThreadNamePrefix()).isEqualTo("case-deletion-");
    }

    @Test
    void shouldUseCallerRunsPolicy() {
        doReturn(1).when(parameterResolver).getThreadCorePoolSize();
        doReturn(1).when(parameterResolver).getThreadMaxPoolSize();
        doReturn(0).when(parameterResolver).getThreadQueueCapacity();

        ThreadPoolTaskExecutor executor = taskPoolConfiguration.caseDeletionExecutor();

        assertThat(executor.getThreadPoolExecutor()
                       .getRejectedExecutionHandler())
            .isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);
    }
}
