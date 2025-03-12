package uk.gov.hmcts.reform.ccd.feign;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FeignErrorDecoder implements feign.codec.ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        FeignException exception = FeignException.errorStatus(methodKey, response);
        log.info("Feign response status: {}, message - {}", status, exception.getMessage());
        if (response.status() >= 400) {
            return new RetryableException(
                status,
                exception.getMessage(),
                response.request().httpMethod(),
                (Long) null, // unix timestamp *at which time* the request can be retried
                response.request()
            );
        }
        return exception;
    }
}
