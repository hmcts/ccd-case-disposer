package uk.gov.hmcts.reform.ccd.exception;

public class MigrationScriptException extends RuntimeException {

    private static final long serialVersionUID = 4L;

    public MigrationScriptException(String script) {
        super("Found migration not yet applied " + script);
    }
}