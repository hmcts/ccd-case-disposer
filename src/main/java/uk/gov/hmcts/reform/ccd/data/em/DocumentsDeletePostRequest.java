package uk.gov.hmcts.reform.ccd.data.em;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class DocumentsDeletePostRequest implements Serializable {

    private static final long serialVersionUID = 432973322;

    private String caseRef;
}
