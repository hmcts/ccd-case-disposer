package uk.gov.hmcts.reform.ccd.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static java.util.concurrent.TimeUnit.MINUTES;

@Service
@Slf4j
@EnableScheduling
public class SecurityUtil {

    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;

    private String serviceAuthToken;
    private String idamClientToken;

    @Autowired
    private ParameterResolver parameterResolver;

    @Autowired
    public SecurityUtil(final AuthTokenGenerator authTokenGenerator, IdamClient idamClient) {
        this.authTokenGenerator = authTokenGenerator;
        this.idamClient = idamClient;
    }

    public String getServiceAuthorization() {
        return serviceAuthToken;
    }

    public String getIdamClientToken() {
        return idamClientToken;
    }

    @Scheduled(fixedRate = 55, timeUnit = MINUTES)
    private void generateTokens() {
        serviceAuthToken = authTokenGenerator.generate();
        idamClientToken = idamClient.getAccessToken(parameterResolver.getIdamUsername(),
                parameterResolver.getIdamPassword());
    }
}
