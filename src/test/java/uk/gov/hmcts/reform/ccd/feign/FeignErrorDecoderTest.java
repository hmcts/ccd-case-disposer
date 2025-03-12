package uk.gov.hmcts.reform.ccd.feign;

import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FeignErrorDecoderTest {

    private ErrorDecoder errorDecoder;

    @BeforeEach
    void setUp() {
        errorDecoder = new FeignErrorDecoder();
    }

    @ParameterizedTest
    @CsvSource({
        "400, Bad Request",
        "401, Unauthorized",
        "403, Forbidden",
        "500, Internal Server Error",
        "502, Bad Gateway",
        "503, Service Unavailable",
        "504, Gateway Timeout"
    })
    void decodeErrorReturnsRetryableException(int status, String reason) {
        Response response = Response.builder()
            .status(status)
            .reason(reason)
            .request(Request.create(Request.HttpMethod.GET, "/", Map.of(), null, new RequestTemplate()))
            .build();
        Exception exc = errorDecoder.decode("methodKey", response);
        assertThat(exc).isInstanceOf(RetryableException.class);
    }

    @Test
    void decodeErrorNonRetryableException() {
        Response response = Response.builder()
            .status(302)
            .reason("Found")
            .request(Request.create(Request.HttpMethod.GET, "/", Map.of(), null, new RequestTemplate()))
            .build();
        Exception exc = errorDecoder.decode("methodKey", response);
        assertThat(exc)
            .isInstanceOf(feign.FeignException.class)
            .isNotInstanceOf(RetryableException.class);
    }
}
