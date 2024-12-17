package uk.gov.hmcts.reform.ccd.feign;

import feign.Response;
import feign.RetryableException;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ErrorDecoder implements feign.codec.ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 502 || response.status() == 503
            || response.status() == 504 || response.status() == 404 || response.status() == 501) {
            System.out.println("Coming here");
            return new RetryableException(
                response.status(),
                "Feign Client Service unavailable, retrying...",
                response.request().httpMethod(),
                Long.valueOf(1000),
                response.request()
            );
        }
        return new RuntimeException("NonRetryable Exception: " + response.status());
    }
}
