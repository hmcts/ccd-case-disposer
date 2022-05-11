package uk.gov.hmcts.reform.ccd.data.em;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class DocumentsDeletePostRequest implements Serializable {

    public static final long serialVersionUID = 432973322;

    private String caseRef;

    public DocumentsDeletePostRequest(final String caseRef) {
        this.caseRef = caseRef;
    }
}
