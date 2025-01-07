package uk.gov.hmcts.reform.ccd.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

@Repository
public class CaseDataInsertRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    public CaseDataInsertRepository(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    private static final String INSERT_QUERY = """
        INSERT INTO public.case_data (id, reference, case_type_id, resolved_ttl, jurisdiction)
        VALUES (:id, :reference, :caseType, :resolvedTtl, :jurisdiction)
        """;

    @Transactional
    public void saveCaseData(CaseDataEntity caseDataEntity) {
        Query insertQuery = entityManager.createNativeQuery(INSERT_QUERY);

        insertQuery.setParameter("id", caseDataEntity.getId())
            .setParameter("reference", caseDataEntity.getReference())
            .setParameter("caseType", caseDataEntity.getCaseType())
            .setParameter("resolvedTtl", caseDataEntity.getResolvedTtl())
            .setParameter("jurisdiction", caseDataEntity.getJurisdiction());

        insertQuery.getSingleResult();

    }
}
