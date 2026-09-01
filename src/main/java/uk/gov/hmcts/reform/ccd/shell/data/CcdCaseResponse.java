package uk.gov.hmcts.reform.ccd.shell.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;

public record CcdCaseResponse(
    Long id,
    @JsonProperty("case_type") String caseType,
    String state,
    JsonNode data
) {
}
