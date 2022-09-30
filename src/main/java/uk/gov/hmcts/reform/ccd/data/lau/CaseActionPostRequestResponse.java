package uk.gov.hmcts.reform.ccd.data.lau;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@AllArgsConstructor
@Getter
@Setter
public class CaseActionPostRequestResponse implements Serializable {

    public static final long serialVersionUID = 432973322;

    private ActionLog actionLog;
}
