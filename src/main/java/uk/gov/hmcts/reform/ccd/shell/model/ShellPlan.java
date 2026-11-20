package uk.gov.hmcts.reform.ccd.shell.model;

import tools.jackson.databind.node.ObjectNode;

public record ShellPlan(
    Long originalReference,
    String shellCaseType,
    ShellDecision decision,
    ObjectNode shellData,
    String reason
) {
}
