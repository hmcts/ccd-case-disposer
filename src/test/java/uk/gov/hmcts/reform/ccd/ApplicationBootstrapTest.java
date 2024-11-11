package uk.gov.hmcts.reform.ccd;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApplicationBootstrapTest {

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
        doNothing().when(securityUtil).generateTokens();

        underTest.run(applicationArguments);

        verify(client).flush();
        verify(applicationExecutor).execute();
    }
}
