package uk.gov.hmcts.reform.ccd.shell.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class ShellMappingResponse {

    @JsonProperty("shellCaseTypeID")
    private String shellCaseTypeID;

    @JsonProperty("shellCaseMappings")
    private final List<ShellCaseFieldMapping> shellCaseMappings = new ArrayList<>();
}
