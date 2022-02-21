package uk.gov.hmcts.reform.ccd.util;

import org.junit.jupiter.api.Test;

class DefaultSnooperTest {
    private final DefaultSnooper underTest = new DefaultSnooper();

    @Test
    void testShouldLogSnoopedMessage() {
        underTest.snoop("Test message");
    }

    @Test
    void testShouldLogSnoopedMessageWithException() {
        underTest.snoop("Test message", new Exception("An exception occurred"));
    }

}
