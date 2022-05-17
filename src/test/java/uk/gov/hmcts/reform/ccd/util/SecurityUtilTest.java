package uk.gov.hmcts.reform.ccd.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@DisplayName("SecurityUtil")
class SecurityUtilTest {

    private static final String SERVICE_AUTH_JWT = "7gf364fg367f67";

    @Mock
    private AuthTokenGenerator serviceTokenGenerator;

    @InjectMocks
    private SecurityUtil securityUtil;

    @Test
    @DisplayName("should return a value from getServiceAuthorization")
    void shouldGetServiceAuthorization() {
        MockitoAnnotations.openMocks(this);

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTH_JWT);

        assertThat(securityUtil.getServiceAuthorization(), is(SERVICE_AUTH_JWT));
    }

}
