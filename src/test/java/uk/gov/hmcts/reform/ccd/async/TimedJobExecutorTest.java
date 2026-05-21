package uk.gov.hmcts.reform.ccd.async;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class TimedJobExecutorTest {

    @Test
    void shouldRunTaskSuccessfullyWithinTimeout() throws Exception {
        TimedJobExecutor executor = new TimedJobExecutor();

        AtomicBoolean executed = new AtomicBoolean(false);

        executor.runWithTimeout(() -> executed.set(true), Duration.ofSeconds(1));

        assertThat(executed.get()).isTrue();
    }

    @Test
    void shouldThrowTimeoutExceptionWhenTaskTakesTooLong()  {
        TimedJobExecutor executor = new TimedJobExecutor();

        Runnable longRunningTask = () -> {
            try {
                Thread.sleep(2000); // task exceeds timeout deliberately
            } catch (InterruptedException ignored) {
                // doesn't matter
            }
        };
        Throwable thrown = catchThrowable(() -> executor.runWithTimeout(longRunningTask, Duration.ofMillis(100)));

        assertThat(thrown).isInstanceOf(TimeoutException.class);
    }

    @Test
    void shouldInterruptTaskOnTimeout() {
        TimedJobExecutor executor = new TimedJobExecutor();

        AtomicBoolean ran = new AtomicBoolean(false);

        Runnable longRunningTask = () -> {
            ran.set(true);
            LockSupport.park(); // waits until interrupted
        };

        Throwable thrown = catchThrowable(() -> executor.runWithTimeout(longRunningTask, Duration.ofMillis(100))
        );

        assertThat(thrown).isInstanceOf(TimeoutException.class);
        assertThat(ran.get()).isTrue();
    }
}
