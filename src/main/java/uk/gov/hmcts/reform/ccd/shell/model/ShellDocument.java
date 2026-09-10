package uk.gov.hmcts.reform.ccd.shell.model;

import tools.jackson.databind.node.ObjectNode;

import java.util.UUID;

public record ShellDocument(
    UUID documentId,
    ObjectNode documentNode
) {
}
