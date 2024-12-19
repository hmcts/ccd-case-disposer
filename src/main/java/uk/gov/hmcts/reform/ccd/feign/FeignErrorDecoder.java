package uk.gov.hmcts.reform.ccd.feign;

import feign.Response;
import feign.RetryableException;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class FeignErrorDecoder implements feign.codec.ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == HttpStatus.NOT_IMPLEMENTED.value()
            || response.status() == HttpStatus.BAD_GATEWAY.value()
            || response.status() == HttpStatus.SERVICE_UNAVAILABLE.value()
            || response.status() == HttpStatus.GATEWAY_TIMEOUT.value()) {
            return new RetryableException(
                response.status(),
                "Feign Client Service unavailable: " + response.status() + ", Reason : " + response.reason(),
                response.request().httpMethod(),
                Long.valueOf(1000),
                response.request()
            );
        }
        return new Exception("Non Retryable Exception: " + response.status() + ", Reason : " + response.reason());
    }
}
