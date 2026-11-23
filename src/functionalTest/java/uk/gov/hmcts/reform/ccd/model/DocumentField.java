package uk.gov.hmcts.reform.ccd.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@SuppressWarnings({"PMD.FieldNamingConventions","checkstyle:MemberName"})
public class DocumentField {
    String document_url;
    String document_binary_url;
    String document_filename;
    String document_created_on;
}
