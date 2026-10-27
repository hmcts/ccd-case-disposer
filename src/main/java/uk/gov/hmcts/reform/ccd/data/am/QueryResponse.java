package uk.gov.hmcts.reform.ccd.data.am;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryResponse {
    private UUID id;
    private String roleName;
    private Map<String, JsonNode> attributes;
}
