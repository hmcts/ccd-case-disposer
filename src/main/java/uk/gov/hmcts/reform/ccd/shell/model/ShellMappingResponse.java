package uk.gov.hmcts.reform.ccd.shell.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ShellMappingResponse(
    @JsonProperty("shellCaseTypeID") String shellCaseTypeId,
    @JsonProperty("caseStates") List<CaseState> caseStates,
    @JsonProperty("shellCaseMappings") List<ShellCaseFieldMapping> shellCaseMappings
) {
    public record CaseState(
        @JsonProperty("name") String name,
        @JsonProperty("stateCategory") String category
    ) {}
}
