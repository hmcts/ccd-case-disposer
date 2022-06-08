package uk.gov.hmcts.reform.ccd.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Service
@Slf4j
public class SecurityUtil {

    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;

    @Autowired
    private ParameterResolver parameterResolver;

    @Autowired
    public SecurityUtil(final AuthTokenGenerator authTokenGenerator,final IdamClient idamClient) {
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
