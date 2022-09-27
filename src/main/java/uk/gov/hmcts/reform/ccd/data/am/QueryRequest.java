package uk.gov.hmcts.reform.ccd.data.am;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class QueryRequest {

    private Map<String, List<String>> attributes;

}
