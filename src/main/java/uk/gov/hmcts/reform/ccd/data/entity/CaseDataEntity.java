package uk.gov.hmcts.reform.ccd.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Table(name = "case_data")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CaseDataEntity {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "reference", nullable = false)
    private Long reference;
    @Column(name = "case_type_id", nullable = false)
    private String caseType;
    @Column(name = "jurisdiction", nullable = false)
    private String jurisdiction;
    @Column(name = "resolved_ttl")
    private LocalDate resolvedTtl;
}
