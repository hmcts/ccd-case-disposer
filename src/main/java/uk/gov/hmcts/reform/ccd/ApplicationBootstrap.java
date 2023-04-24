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

@Slf4j
@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, it's not a utility class
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam"})
@ComponentScan(basePackages = {"uk.gov.hmcts.reform"})
public class ApplicationBootstrap implements ApplicationRunner {

    @Inject
    private ApplicationExecutor applicationExecutor;

    @Autowired
    private TelemetryClient client;

    @Value("${telemetry.wait.period:10000}")
    private int waitPeriod;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            log.info("Starting the Case-Disposer job.");
            applicationExecutor.execute();
            log.info("Completed the Case-Disposer job successfully.");
        } catch (Exception e) {
            log.error("Error executing Case-Disposer job.", e);
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
