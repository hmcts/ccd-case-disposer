package uk.gov.hmcts.reform.ccd.shell.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ShellMappingResponse(
    @JsonProperty("shellCaseTypeID") String shellCaseTypeId,
    @JsonProperty("shellCaseMappings") List<ShellCaseFieldMapping> shellCaseMappings
) {
}
