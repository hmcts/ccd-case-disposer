package uk.gov.hmcts.reform.ccd.feign;

import feign.Retryer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class RetryableFeignConfigTest {

    @Test
    void testFeignRetryer() {
        RetryableFeignConfig config = new RetryableFeignConfig();
        Retryer retryer = config.feignRetryer();

        assertNotNull(retryer, "Retryer should not be null");
        assertInstanceOf(Retryer.Default.class, retryer, "Retryer should be an instance of Retryer.Default");
    }
}
