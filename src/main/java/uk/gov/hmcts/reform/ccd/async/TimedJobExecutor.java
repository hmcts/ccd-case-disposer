package uk.gov.hmcts.reform.ccd.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class TimedJobExecutor {

    @SuppressWarnings("PMD.DoNotUseThreads")
    public void runWithTimeout(Runnable task, Duration timeout)
        throws TimeoutException, ExecutionException, InterruptedException {

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> future = executor.submit(task);
            try {
                future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException te) {
                log.error("Timed out waiting for task to complete. Stopping...");
                future.cancel(true);
                throw te;
            }
        }
    }
}
