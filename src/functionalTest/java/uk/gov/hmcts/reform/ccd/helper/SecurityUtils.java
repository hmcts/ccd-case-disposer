package uk.gov.hmcts.reform.ccd.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Service
@EnableFeignClients(basePackageClasses = {ServiceAuthorisationApi.class, IdamClient.class})
public class SecurityUtils {

    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;

    @Autowired
    private ParameterResolver parameterResolver;

    @Autowired
    public SecurityUtils(final AuthTokenGenerator authTokenGenerator, IdamClient idamClient) {
        this.authTokenGenerator = authTokenGenerator;
        this.idamClient = idamClient;
    }

    public String getServiceAuthorization() {
        return authTokenGenerator.generate();
    }

    public String getIdamClientToken() {
        return idamClient.getAccessToken(parameterResolver.getIdamUsername(),
                parameterResolver.getIdamPassword());
    }
}