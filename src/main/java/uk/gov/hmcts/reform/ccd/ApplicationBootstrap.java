package uk.gov.hmcts.reform.ccd;

import com.microsoft.applicationinsights.TelemetryClient;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import uk.gov.hmcts.reform.ccd.async.TimedJobExecutor;
import uk.gov.hmcts.reform.ccd.exception.JobInterruptedException;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@SpringBootApplication
// Spring needs a constructor, it's not a utility class
@SuppressWarnings({"HideUtilityClassConstructor", "java:S6813"})
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.ccd", "uk.gov.hmcts.reform.idam"})
@ComponentScan(basePackages = {"uk.gov.hmcts.reform"})
public class ApplicationBootstrap implements ApplicationRunner {

    public static final String MARKER = "CCD-Case-Disposer";

    @Inject
    private ApplicationExecutor applicationExecutor;

    @Inject
    private TimedJobExecutor timedJobExecutor;

    @Value("${case-collector.max-run-duration-minutes:480}")
    private Long timeoutInMinutes = 480L;

    @Autowired
    private TelemetryClient client;

    @Inject
    private SecurityUtil securityUtil;

    @Value("${telemetry.wait.period:10000}")
    private int waitPeriod;

    @Value("${case-collector.version:1}")
    private int caseCollectorVersion;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Duration timeout = Duration.ofMinutes(timeoutInMinutes);
        try {
            log.info("[{}] Starting the Case-Disposer job.", MARKER);
            securityUtil.generateTokens();
            timedJobExecutor.runWithTimeout(() -> applicationExecutor.execute(caseCollectorVersion), timeout);
            log.info("[{}] Completed the Case-Disposer job successfully.", MARKER);
        } catch (TimeoutException | JobInterruptedException e) {
            log.error("[{}] Timed out waiting for the job to complete.", MARKER);
        } catch (RuntimeException e) {
            log.error("[{}] Error executing Case-Disposer job.", MARKER, e);
        } finally {
            client.flush();
            waitTelemetryGracefulPeriod();
        }
    }

    private void waitTelemetryGracefulPeriod() throws InterruptedException {
        Thread.sleep(waitPeriod);
    }

    public static void main(final String[] args) {
        final ApplicationContext context = SpringApplication.run(ApplicationBootstrap.class);
        SpringApplication.exit(context);
    }

}
