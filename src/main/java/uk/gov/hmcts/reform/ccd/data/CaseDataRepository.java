package uk.gov.hmcts.reform.ccd.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseDataRepository extends JpaRepository<CaseDataEntity, Long> {

    @Query("SELECT c FROM CaseDataEntity c WHERE c.resolvedTtl < CURRENT_DATE "
            + "AND c.caseType IN :queryCaseTypes ORDER BY c.resolvedTtl DESC")
    List<CaseDataEntity> findExpiredCases(@Param("queryCaseTypes") List<String> queryCaseTypes);

    @Query("SELECT c FROM CaseDataEntity c WHERE c.reference = :queryCaseReference")
    Optional<CaseDataEntity> findByReference(@Param("queryCaseReference") Long queryCaseReference);

    void delete(final CaseDataEntity caseDataEntity);

}
