package uk.gov.hmcts.reform.ccd.data.am;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryResponse {
    private UUID id;
    private String actorIdType;
    private String actorId;
    private String roleType;
    private String roleName;
    private String classification;
    private String grantType;
    private String roleCategory;
    private boolean readOnly;
    private String beginTime;
    private String endTime;
    private String created;
    private Map<String, JsonNode> attributes;
    private JsonNode notes;
    private List<String> authorisations;
}
