package uk.gov.hmcts.reform.ccd.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.exception.IdamAuthTokenGenerationException;
import uk.gov.hmcts.reform.ccd.exception.ServiceAuthTokenGenerationException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.invokeMethod;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class SecurityUtilTest {

    @Mock
    private AuthTokenGenerator serviceTokenGenerator;

    @Mock
    private ParameterResolver parameterResolver;

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private SecurityUtil securityUtil;

    @Test
    void shouldGetServiceAuthorization() {

        setField(securityUtil, "parameterResolver", parameterResolver);

        when(serviceTokenGenerator.generate()).thenReturn("7gf364fg367f67");
        when(parameterResolver.getIdamUsername()).thenReturn("JohnTerry");
        when(parameterResolver.getIdamPassword()).thenReturn("Chelsea123");
        when(idamClient.getAccessToken("JohnTerry", "Chelsea123")).thenReturn("Bearer 123");


        invokeMethod(securityUtil, "generateTokens");

        assertThat(securityUtil.getServiceAuthorization()).isEqualTo("7gf364fg367f67");
        assertThat(securityUtil.getIdamClientToken()).isEqualTo("Bearer 123");
    }


    @Test
    void shouldThrowServiceAuthTokenGenerationException() {
        try {
            setField(securityUtil, "parameterResolver", parameterResolver);
            doThrow(new ServiceAuthTokenGenerationException("1234567890123456"))
                    .when(serviceTokenGenerator).generate();

            invokeMethod(securityUtil, "generateTokens");

        } catch (final ServiceAuthTokenGenerationException serviceAuthTokenGenerationException) {
            assertThat(serviceAuthTokenGenerationException.getMessage())
                    .contains("Case disposer is unable to generate service auth token due to error -");
        }
    }

    @Test
    void shouldThrowIdamAuthTokenGenerationException() {
        try {
            setField(securityUtil, "parameterResolver", parameterResolver);

            when(serviceTokenGenerator.generate()).thenReturn("7gf364fg367f67");
            when(parameterResolver.getIdamUsername()).thenReturn("JohnTerry");
            when(parameterResolver.getIdamPassword()).thenReturn("Chelsea123");

            doThrow(new IdamAuthTokenGenerationException("1234567890123456"))
                    .when(idamClient).getAccessToken("JohnTerry", "Chelsea123");

            invokeMethod(securityUtil, "generateTokens");

        } catch (final IdamAuthTokenGenerationException idamAuthTokenGenerationException) {
            assertThat(idamAuthTokenGenerationException.getMessage())
                    .contains("Case disposer is unable to generate IDAM token due to error -");
        }
    }
}
