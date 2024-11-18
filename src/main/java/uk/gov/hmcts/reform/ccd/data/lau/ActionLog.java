package uk.gov.hmcts.reform.ccd.data.lau;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ActionLog implements Serializable {

    public static final long serialVersionUID = 432973322;

    private String userId;
    private String caseAction;
    private String caseRef;
    private String caseJurisdictionId;
    private String caseTypeId;
    private String timestamp;
}
