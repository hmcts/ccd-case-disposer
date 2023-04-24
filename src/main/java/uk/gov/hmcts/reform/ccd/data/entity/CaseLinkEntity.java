package uk.gov.hmcts.reform.ccd.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "case_link")
@IdClass(CaseLinkPrimaryKey.class)
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CaseLinkEntity {
    @Id
    @Column(name = "case_id", nullable = false)
    private Long caseId;
    @Id
    @Column(name = "linked_case_id", nullable = false)
    private Long linkedCaseId;
    @Column(name = "case_type_id", nullable = false)
    private String caseTypeId;
}
