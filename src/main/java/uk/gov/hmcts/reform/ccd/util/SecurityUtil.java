package uk.gov.hmcts.reform.ccd.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.exception.IdamAuthTokenGenerationException;
import uk.gov.hmcts.reform.ccd.exception.ServiceAuthTokenGenerationException;
import uk.gov.hmcts.reform.ccd.exception.UserDetailsGenerationException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static java.util.concurrent.TimeUnit.MINUTES;


@Service
@Slf4j
@EnableScheduling
@Getter
public class SecurityUtil {

    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;
    private final ParameterResolver parameterResolver;

    private String serviceAuthorization;
    private String idamClientToken;
    private UserInfo userInfo;

    public SecurityUtil(
        final AuthTokenGenerator authTokenGenerator,
        final IdamClient idamClient,
        final ParameterResolver parameterResolver
    ) {
        this.authTokenGenerator = authTokenGenerator;
        this.idamClient = idamClient;
        this.parameterResolver = parameterResolver;
    }

    public void generateTokens() {
        generateServiceToken();
        generateIdamToken();
        generateUserInfo();
    }

    private void generateUserInfo() {
        try {
            userInfo = idamClient.getUserInfo(idamClientToken);
        } catch (final Exception exception) {
            log.error("Case disposer is unable to get UserInfo due to error", exception);
            throw new UserDetailsGenerationException(String.format("Case disposer is unable to get UserInfo "
                    + "due to error - %s", exception.getMessage()), exception);
        }
    }

    @Scheduled(initialDelay = 237, fixedRate = 237, timeUnit = MINUTES)
    private void generateServiceToken() {
        try {
            serviceAuthorization = authTokenGenerator.generate();
        } catch (final Exception exception) {
            log.error("Case disposer is unable to generate service auth token due to error", exception);
            throw new ServiceAuthTokenGenerationException(String.format("Case disposer is unable to generate service "
                    + "auth token due to error - %s", exception.getMessage()), exception);
        }
    }

    @Scheduled(initialDelay = 55, fixedRate = 55, timeUnit = MINUTES)
    private void generateIdamToken() {
        try {
            idamClientToken = idamClient.getAccessToken(parameterResolver.getIdamUsername(),
                    parameterResolver.getIdamPassword());
        } catch (final Exception exception) {
            log.error("Case disposer is unable to generate IDAM token due to error", exception);
            throw new IdamAuthTokenGenerationException(String.format("Case disposer is unable to generate IDAM "
                    + "token due to error - %s", exception.getMessage()), exception);
        }
    }
}
