package uk.gov.hmcts.reform.ccd.data.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

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
