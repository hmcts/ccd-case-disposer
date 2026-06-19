package uk.gov.hmcts.reform.ccd.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.exception.IdamAuthTokenGenerationException;
import uk.gov.hmcts.reform.ccd.exception.ServiceAuthTokenGenerationException;
import uk.gov.hmcts.reform.ccd.exception.UserDetailsGenerationException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    @SuppressWarnings("java:S1874")
    void shouldGetServiceAuthorization() {
        final UserInfo userInfo = mock(UserInfo.class);

        when(serviceTokenGenerator.generate()).thenReturn("7gf364fg367f67");
        when(parameterResolver.getIdamUsername()).thenReturn("JohnTerry");
        when(parameterResolver.getIdamPassword()).thenReturn("Chelsea123");
        when(idamClient.getAccessToken("JohnTerry", "Chelsea123")).thenReturn("Bearer 123");
        when(idamClient.getUserInfo("Bearer 123")).thenReturn(userInfo);

        securityUtil.generateTokens();

        assertThat(securityUtil.getServiceAuthorization()).isEqualTo("7gf364fg367f67");
        assertThat(securityUtil.getIdamClientToken()).isEqualTo("Bearer 123");
        assertThat(securityUtil.getUserInfo()).isEqualTo(userInfo);
    }


    @Test
    void shouldThrowServiceAuthTokenGenerationException() {
        doThrow(new ServiceAuthTokenGenerationException("1234567890123456"))
                .when(serviceTokenGenerator).generate();
        assertThatThrownBy(() -> securityUtil.generateTokens())
            .isInstanceOf(ServiceAuthTokenGenerationException.class)
            .hasMessageContaining("Case disposer is unable to generate service auth token due to error -");
    }

    @Test
    void shouldThrowIdamAuthTokenGenerationException() {
        when(serviceTokenGenerator.generate()).thenReturn("service-token");
        when(parameterResolver.getIdamUsername()).thenReturn("JohnTerry");
        when(parameterResolver.getIdamPassword()).thenReturn("Chelsea123");

        doThrow(new IdamAuthTokenGenerationException("1234567890123456"))
            .when(idamClient).getAccessToken("JohnTerry", "Chelsea123");

        assertThatThrownBy(() -> securityUtil.generateTokens())
            .isInstanceOf(IdamAuthTokenGenerationException.class)
            .hasMessageContaining("Case disposer is unable to generate IDAM token due to error -");
    }

    @Test
    void shouldThrowUserDetailsGenerationException() {
        when(serviceTokenGenerator.generate()).thenReturn("service-token");
        when(parameterResolver.getIdamUsername()).thenReturn("JohnSmith");
        when(parameterResolver.getIdamPassword()).thenReturn("Chelsea123");
        when(idamClient.getAccessToken("JohnSmith", "Chelsea123")).thenReturn("Bearer 123");

        doThrow(new UserDetailsGenerationException("1234567890123456"))
            .when(idamClient).getUserInfo("Bearer 123");

        assertThatThrownBy(() -> securityUtil.generateTokens())
            .isInstanceOf(UserDetailsGenerationException.class)
            .hasMessageContaining("Case disposer is unable to get UserInfo due to error -");
    }
}
