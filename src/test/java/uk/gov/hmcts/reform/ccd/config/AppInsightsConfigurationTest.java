package uk.gov.hmcts.reform.ccd.config;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppInsightsConfigurationTest {
    @Test
    void shouldCreateAppInsightsClient() {
        final var appInsightsConfiguration = new AppInsightsConfiguration();
        assertThat(appInsightsConfiguration.telemetryClient()).isInstanceOf(TelemetryClient.class);
    }
}
