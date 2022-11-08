package uk.gov.hmcts.reform.ccd.data.am;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoleAssignmentsPostResponse {
    @JsonProperty("roleAssignmentResponse")
    private List<QueryResponse> roleAssignmentResponse;
}
