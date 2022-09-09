package uk.gov.hmcts.reform.ccd.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@Service
public class SecurityUtils {

    private String serviceAuthorization;
    private String clientToken;
    private UserDetails userDetails;

    @Autowired
    public SecurityUtils(final AuthTokenGenerator authTokenGenerator,
                         final IdamClient idamClient,
                         final ParameterResolver parameterResolver) {
        this.serviceAuthorization = authTokenGenerator.generate();
        this.clientToken = idamClient.getAccessToken(parameterResolver.getIdamUsername(),
                parameterResolver.getIdamPassword());
        this.userDetails = idamClient.getUserDetails(clientToken);
    }

    public String getServiceAuthorization() {
        return serviceAuthorization;
    }

    public String getIdamClientToken() {
        return clientToken;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }
}