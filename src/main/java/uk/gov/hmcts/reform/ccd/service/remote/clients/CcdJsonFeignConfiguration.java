package uk.gov.hmcts.reform.ccd.service.remote.clients;

import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.context.annotation.Bean;

public class CcdJsonFeignConfiguration {

    @Bean
    Encoder ccdJsonFeignEncoder() {
        return new JacksonEncoder();
    }

    @Bean
    Decoder ccdJsonFeignDecoder() {
        return new ResponseEntityDecoder(new JacksonDecoder());
    }
}
