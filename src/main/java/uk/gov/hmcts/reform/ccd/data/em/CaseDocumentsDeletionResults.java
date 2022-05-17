package uk.gov.hmcts.reform.ccd.data.em;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaseDocumentsDeletionResults {

    private Integer caseDocumentsFound;
    private Integer markedForDeletion;
}
