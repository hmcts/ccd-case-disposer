package uk.gov.hmcts.reform.ccd.data.model;

import lombok.Builder;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.time.LocalDate;
import java.util.Objects;


@Builder
public record CaseData(Long id, Long reference, String caseType, String jurisdiction, LocalDate resolvedTtl) {
    public static CaseData fromEntity(CaseDataEntity caseDataEntity) {
        return CaseData.builder()
            .id(caseDataEntity.getId())
            .reference(caseDataEntity.getReference())
            .caseType(caseDataEntity.getCaseType())
            .jurisdiction(caseDataEntity.getJurisdiction())
            .resolvedTtl(caseDataEntity.getResolvedTtl())
            .build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CaseData caseData = (CaseData) o;
        return Objects.equals(id, caseData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
