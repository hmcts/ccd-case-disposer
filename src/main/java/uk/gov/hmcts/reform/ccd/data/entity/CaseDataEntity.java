package uk.gov.hmcts.reform.ccd.data.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
    @Column(name = "resolved_ttl")
    private LocalDate resolvedTtl;
}
