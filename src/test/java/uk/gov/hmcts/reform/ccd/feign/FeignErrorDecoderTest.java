package uk.gov.hmcts.reform.ccd.feign;

import feign.Request;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class FeignErrorDecoderTest {

    private ErrorDecoder errorDecoder;

    @BeforeEach
    void setUp() {
        errorDecoder = new FeignErrorDecoder();
    }

    @Test
    void testDecode_InternalServerError() {
        Response response = Response.builder()
            .status(500)
            .reason("Internal Server Error")
            .request(Request.create(Request.HttpMethod.GET, "/test", Collections.emptyMap(),
                                    null, StandardCharsets.UTF_8))
            .build();

        Exception exception = errorDecoder.decode("methodKey", response);

        assertInstanceOf(Exception.class, exception);
        assertEquals("Non Retryable Exception: 500 Reason : Internal Server Error",
                     exception.getMessage());
    }

    @Test
    void testDecode_GenericError() {
        Response response = Response.builder()
            .status(400)
            .reason("Bad Request")
            .request(Request.create(Request.HttpMethod.GET, "/test", Collections.emptyMap(),
                                    null, StandardCharsets.UTF_8))
            .build();

        Exception exception = errorDecoder.decode("methodKey", response);

        assertInstanceOf(Exception.class, exception);
        assertEquals("Non Retryable Exception: 400 Reason : Bad Request",
                     exception.getMessage());
    }

    @Test
    void testDecode_BadGatewayError() {
        Response response = Response.builder()
            .status(502)
            .reason("Bad Gateway Error")
            .request(Request.create(Request.HttpMethod.GET, "/test", Collections.emptyMap(),
                                    null, StandardCharsets.UTF_8))
            .build();

        Exception exception = errorDecoder.decode("methodKey", response);

        assertInstanceOf(Exception.class, exception);
        assertEquals("Feign Client Service unavailable: 502 Reason : Bad Gateway Error",
                     exception.getMessage());
    }

    @Test
    void testDecode_NotImplementedError() {
        Response response = Response.builder()
            .status(501)
            .reason("Not Implemented Error")
            .request(Request.create(Request.HttpMethod.GET, "/test", Collections.emptyMap(),
                                    null, StandardCharsets.UTF_8))
            .build();

        Exception exception = errorDecoder.decode("methodKey", response);

        assertInstanceOf(Exception.class, exception);
        assertEquals("Feign Client Service unavailable: 501 Reason : Not Implemented Error",
                     exception.getMessage());
    }

    @Test
    void testDecode_ServiceUnavailableError() {
        Response response = Response.builder()
            .status(503)
            .reason("Service Unavailable Error")
            .request(Request.create(Request.HttpMethod.GET, "/test", Collections.emptyMap(),
                                    null, StandardCharsets.UTF_8))
            .build();

        Exception exception = errorDecoder.decode("methodKey", response);

        assertInstanceOf(Exception.class, exception);
        assertEquals("Feign Client Service unavailable: 503 Reason : Service Unavailable Error",
                     exception.getMessage());
    }

    @Test
    void testDecode_GatewayTimeoutError() {
        Response response = Response.builder()
            .status(504)
            .reason("Gateway Timeout Error")
            .request(Request.create(Request.HttpMethod.GET, "/test", Collections.emptyMap(),
                                    null, StandardCharsets.UTF_8))
            .build();

        Exception exception = errorDecoder.decode("methodKey", response);

        assertInstanceOf(Exception.class, exception);
        assertEquals("Feign Client Service unavailable: 504 Reason : Gateway Timeout Error",
                     exception.getMessage());
    }


}
