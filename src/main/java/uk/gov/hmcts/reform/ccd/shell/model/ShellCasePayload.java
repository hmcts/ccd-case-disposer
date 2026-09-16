package uk.gov.hmcts.reform.ccd.shell.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.node.ObjectNode;

public record ShellCasePayload(
    ObjectNode data,
    @JsonProperty("event_token") String eventToken,
    Event event
) {

    public static ShellCasePayload withEvent(ObjectNode data, String eventToken, String eventId) {
        return new ShellCasePayload(
            data,
            eventToken,
            new Event(eventId, "Create case", "Initial case creation"));
    }

    @JsonProperty("ignore_warning")
    public boolean ignoreWarning() {
        return false;
    }

    public record Event(String id, String summary, String description) {

    }
}
