package uk.gov.hmcts.reform.ccd.data.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Table(name = "case_data")
@Entity
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy= InheritanceType.SINGLE_TABLE)
public class CaseDataEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "reference", nullable = false)
    private Long reference;
    @Column(name = "case_type_id", nullable = false)
    private String caseType;
    @Column(name = "resolved_ttl")
    private LocalDate resolvedTtl;
}
