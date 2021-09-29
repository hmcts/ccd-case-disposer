package uk.gov.hmcts.reform.ccd.data.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.util.List;

@Repository
public interface CaseDataRepository extends JpaRepository<CaseDataEntity, Long> {

    //    @Query("SELECT c FROM CaseDataEntity c WHERE c.resolvedTTL < CURRENT_DATE "
    //        + "AND c.caseType IN :queryCaseTypes ORDER BY c.resolvedTTL DESC")
    @Query("SELECT c FROM CaseDataEntity c WHERE c.resolvedTtl < CURRENT_DATE ORDER BY c.resolvedTtl DESC")
    List<CaseDataEntity> findExpiredCases(/*queryDate@Param("queryCaseTypes") List queryCaseTypes*/);

}
