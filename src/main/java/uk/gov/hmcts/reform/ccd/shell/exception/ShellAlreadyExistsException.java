package uk.gov.hmcts.reform.ccd.shell.exception;

import lombok.Getter;

@Getter
public class ShellAlreadyExistsException extends ShellCaseException {

    private final Long shellCaseReference;

    public ShellAlreadyExistsException(
        Long originalCaseReference,
        Long shellCaseReference
    ) {
        super("Shell case %s already exists for case %s".formatted(shellCaseReference, originalCaseReference));
        this.shellCaseReference = shellCaseReference;
    }

}
