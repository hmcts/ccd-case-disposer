package uk.gov.hmcts.reform.ccd.data.lau;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CaseActionPostRequestResponse implements Serializable {

    public static final long serialVersionUID = 432973322;

    private ActionLog actionLog;
}
