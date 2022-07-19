package uk.gov.hmcts.reform.ccd.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
    @DisplayName("Should return a auth tokens")
    void shouldGetServiceAuthorization() {

        setField(securityUtil, "parameterResolver", parameterResolver);

        when(serviceTokenGenerator.generate()).thenReturn("7gf364fg367f67");
        when(parameterResolver.getIdamUsername()).thenReturn("JohnTerry");
        when(parameterResolver.getIdamPassword()).thenReturn("Chelsea123");
        when(idamClient.getAccessToken("JohnTerry", "Chelsea123")).thenReturn("Bearer 123");


        invokeMethod(securityUtil, "generateTokens");

        assertThat(securityUtil.getServiceAuthorization(), is("7gf364fg367f67"));
        assertThat(securityUtil.getIdamClientToken(), is("Bearer 123"));
    }

}
