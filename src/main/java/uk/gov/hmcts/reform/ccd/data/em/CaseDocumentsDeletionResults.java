package uk.gov.hmcts.reform.ccd.data.em;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CaseDocumentsDeletionResults {

    private Integer caseDocumentsFound;
    private Integer markedForDeletion;
}
