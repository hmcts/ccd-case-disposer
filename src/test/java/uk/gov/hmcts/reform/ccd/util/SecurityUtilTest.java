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

    private static final String USERNAME = "JohnTerry";
    private static final String PASSWORD = "Chelsea123";
    private static final String BEARER_TOKEN = "Bearer 123";

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
        when(parameterResolver.getIdamUsername()).thenReturn(USERNAME);
        when(parameterResolver.getIdamPassword()).thenReturn(PASSWORD);
        when(idamClient.getAccessToken(USERNAME, PASSWORD)).thenReturn(BEARER_TOKEN);
        when(idamClient.getUserInfo(BEARER_TOKEN)).thenReturn(userInfo);

        securityUtil.generateTokens();

        assertThat(securityUtil.getServiceAuthorization()).isEqualTo("7gf364fg367f67");
        assertThat(securityUtil.getIdamClientToken()).isEqualTo(BEARER_TOKEN);
        assertThat(securityUtil.getUserInfo()).isEqualTo(userInfo);
    }


    @Test
    void shouldThrowServiceAuthTokenGenerationException() {
        doThrow(new ServiceAuthTokenGenerationException("1234567890123456"))
                .when(serviceTokenGenerator).generate();
        assertThatThrownBy(securityUtil::generateTokens)
            .isInstanceOf(ServiceAuthTokenGenerationException.class)
            .hasMessageContaining("Case disposer is unable to generate service auth token due to error -");
    }

    @Test
    void shouldThrowIdamAuthTokenGenerationException() {
        when(serviceTokenGenerator.generate()).thenReturn("service-token");
        when(parameterResolver.getIdamUsername()).thenReturn(USERNAME);
        when(parameterResolver.getIdamPassword()).thenReturn(PASSWORD);

        doThrow(new IdamAuthTokenGenerationException("1234567890123456"))
            .when(idamClient).getAccessToken(USERNAME, PASSWORD);

        assertThatThrownBy(securityUtil::generateTokens)
            .isInstanceOf(IdamAuthTokenGenerationException.class)
            .hasMessageContaining("Case disposer is unable to generate IDAM token due to error -");
    }

    @Test
    void shouldThrowUserDetailsGenerationException() {
        when(serviceTokenGenerator.generate()).thenReturn("service-token");
        when(parameterResolver.getIdamUsername()).thenReturn(USERNAME);
        when(parameterResolver.getIdamPassword()).thenReturn(PASSWORD);
        when(idamClient.getAccessToken(USERNAME, PASSWORD)).thenReturn(BEARER_TOKEN);

        doThrow(new UserDetailsGenerationException("1234567890123456"))
            .when(idamClient).getUserInfo(BEARER_TOKEN);

        assertThatThrownBy(securityUtil::generateTokens)
            .isInstanceOf(UserDetailsGenerationException.class)
            .hasMessageContaining("Case disposer is unable to get UserInfo due to error -");
    }
}
