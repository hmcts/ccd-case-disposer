package uk.gov.hmcts.reform.ccd.data.am;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class RoleAssignmentsQueryPostRequest {
    private List<QueryRequest> queryRequests;

    public RoleAssignmentsQueryPostRequest(String caseRef) {
        queryRequests = Arrays.asList(QueryRequest
                                          .builder()
                                          .attributes(Collections.singletonMap("caseId", Arrays.asList(caseRef)))
                                          .build());
    }
}
