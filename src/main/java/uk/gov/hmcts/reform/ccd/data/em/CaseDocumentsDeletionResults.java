package uk.gov.hmcts.reform.ccd.data.em;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CaseDocumentsDeletionResults {

    private Integer caseDocumentsFound;
    private Integer markedForDeletion;
}
