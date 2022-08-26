package uk.gov.hmcts.reform.ccd.data.am;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryResponse {
    private UUID id;
    private String actorIdType;
    private String roleName;
    private Map<String, JsonNode> attributes;
}
