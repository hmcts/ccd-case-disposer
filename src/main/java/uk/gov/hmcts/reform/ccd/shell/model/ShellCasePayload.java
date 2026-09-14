package uk.gov.hmcts.reform.ccd.shell.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.node.ObjectNode;

public record ShellCasePayload(
    ObjectNode data,
    @JsonProperty("event_token") String eventToken
) {

    private static final Event EVENT = new Event("CREATE", "Create case", "Initial case creation");

    @JsonProperty("event")
    public Event event() {
        return EVENT;
    }

    @JsonProperty("ignore_warning")
    public boolean ignoreWarning() {
        return false;
    }

    private record Event(String id, String summary, String description) {
    }
}
