package uk.gov.hmcts.reform.ccd.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Service
@Slf4j
public class SecurityUtil {

    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public SecurityUtil(final AuthTokenGenerator authTokenGenerator) {
        this.authTokenGenerator = authTokenGenerator;
    }

    public String getServiceAuthorization() {
        return authTokenGenerator.generate();
    }

}
