package uk.gov.hmcts.reform.ccd;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import uk.gov.hmcts.reform.ccd.async.TimedJobExecutor;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApplicationBootstrapTest {

    @Mock
    TimedJobExecutor timedJobExecutor;

    @Mock
    private ApplicationArguments applicationArguments;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private ApplicationExecutor applicationExecutor;

    @Mock
    private TelemetryClient client;

    @InjectMocks
    private ApplicationBootstrap underTest;

    @Test
    void testShouldRunExecutor() throws Exception {
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Duration> timeoutCaptor = ArgumentCaptor.forClass(Duration.class);
        doNothing().when(timedJobExecutor).runWithTimeout(runnableCaptor.capture(), timeoutCaptor.capture());
        doNothing().when(securityUtil).generateTokens();

        underTest.run(applicationArguments);

        Runnable capturedJob = runnableCaptor.getValue();
        assertThat(capturedJob).isNotNull();

        capturedJob.run();

        verify(applicationExecutor).execute();
        verify(securityUtil).generateTokens();
        verify(client).flush();
        verify(timedJobExecutor).runWithTimeout(any(), any());
    }
}
