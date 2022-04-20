package uk.gov.hmcts.reform.ccd.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("JacksonObjectMapperConfig")
class JacksonObjectMapperConfigTest {

    private static final String DATE_TIME_ISO8601 = "2017-03-01T10:20:30";
    private static final LocalDateTime DATE_TIME = LocalDateTime.parse(DATE_TIME_ISO8601);
    private static final String DATE_TIME_ARRAY = "[2017,3,1,10,20,30]";

    private static JacksonObjectMapperConfig jacksonObjectMapperConfig;

    @BeforeEach
    void setUp() {
        jacksonObjectMapperConfig = new JacksonObjectMapperConfig();
    }

    @Test
    @DisplayName("should configure a simpleObjectMapper")
    void shouldConfigureSimpleObjectMapper() {
        final ObjectMapper objectMapper = jacksonObjectMapperConfig.simpleObjectMapper();

        assertAll(
            () -> assertThat(objectMapper.canSerialize(LocalDateTime.class), is(true)),
            () -> assertThat(objectMapper.writeValueAsString(DATE_TIME), equalTo(DATE_TIME_ARRAY))
        );
    }

}
