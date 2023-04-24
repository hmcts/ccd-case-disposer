package uk.gov.hmcts.reform.ccd.helper;

import feign.jackson.JacksonEncoder;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;

import static feign.Feign.builder;

@Named
public class S2SHelper {

    @Value("${idam.s2s-auth.url}")
    private String s2sUrl;

    @Value("${ccd-data.secret}")
    private String secret;

    @Value("${ccd-data.name}")
    private String microservice;

    private ServiceAuthTokenGenerator tokenGenerator;

    public String getToken() {
        if (tokenGenerator == null) {
            final ServiceAuthorisationApi serviceAuthorisationApi = getServiceAuthorisationApi();
            this.tokenGenerator = new ServiceAuthTokenGenerator(secret, microservice, serviceAuthorisationApi);

            return tokenGenerator.generate();
        }
        return tokenGenerator.generate();
    }

    private ServiceAuthorisationApi getServiceAuthorisationApi() {
        return builder()
                .encoder(new JacksonEncoder())
                .contract(new SpringMvcContract())
                .target(ServiceAuthorisationApi.class, s2sUrl);
    }
}
