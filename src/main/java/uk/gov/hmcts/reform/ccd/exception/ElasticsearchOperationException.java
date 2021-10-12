package uk.gov.hmcts.reform.ccd.exception;

public class ElasticsearchOperationException extends RuntimeException {
    public ElasticsearchOperationException(Throwable cause) {
        super(cause);
    }

    public ElasticsearchOperationException(String message) {
        super(message);
    }
}
