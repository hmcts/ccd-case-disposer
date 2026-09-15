package uk.gov.hmcts.reform.ccd.shell.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CcdCaseSearchResponse(Long total, List<EsCase> cases) {

    public record EsCase(@JsonProperty("id") Long reference) {
    }
}
