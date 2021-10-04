package uk.gov.hmcts.reform.ccd.exception;

public class ElasticsearchOperationException extends RuntimeException {
    public ElasticsearchOperationException(String message) {
        super(message);
    }
}
