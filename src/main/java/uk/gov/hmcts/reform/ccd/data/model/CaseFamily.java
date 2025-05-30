package uk.gov.hmcts.reform.ccd.data.model;

import java.util.List;

public record CaseFamily(List<CaseData> linkedCases) {
}
