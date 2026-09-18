package uk.gov.hmcts.reform.ccd.shell.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class ShellCaseFieldMapping {
    @JsonProperty("OriginatingCaseFieldName")
    private String originatingCaseFieldName;

    @JsonProperty("ShellCaseFieldName")
    private String shellCaseFieldName;
}
