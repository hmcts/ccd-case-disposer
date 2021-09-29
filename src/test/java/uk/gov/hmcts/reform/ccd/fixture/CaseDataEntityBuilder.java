package uk.gov.hmcts.reform.ccd.fixture;

import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.time.LocalDate;
import java.util.Optional;

public class CaseDataEntityBuilder {
    private final Long id;

    private Long reference;
    private String caseType;
    private LocalDate resolvedTtl;

    public CaseDataEntityBuilder(Long id) {
        this.id = id;
    }

    public CaseDataEntityBuilder withReference(Long reference) {
        this.reference = reference;
        return this;
    }

    public CaseDataEntityBuilder withCaseType(String caseType) {
        this.caseType = caseType;
        return this;
    }

    public CaseDataEntityBuilder withResolvedTtl(LocalDate resolvedTtl) {
        this.resolvedTtl = resolvedTtl;
        return this;
    }

    public CaseDataEntity build() {
        CaseDataEntity caseDataEntity = new CaseDataEntity();
        caseDataEntity.setId(this.id);
        Optional.ofNullable(reference).ifPresent(caseDataEntity::setReference);
        Optional.ofNullable(caseType).ifPresent(caseDataEntity::setCaseType);
        Optional.ofNullable(resolvedTtl).ifPresent(caseDataEntity::setResolvedTtl);

        return caseDataEntity;
    }

}
