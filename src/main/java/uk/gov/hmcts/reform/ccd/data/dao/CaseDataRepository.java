package uk.gov.hmcts.reform.ccd.data.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.util.List;

@Repository
public interface CaseDataRepository extends CrudRepository<CaseDataEntity, Long> {

    //    @Query("SELECT c FROM CaseDataEntity c WHERE c.resolvedTTL < CURRENT_DATE "
    //        + "AND c.caseType IN :queryCaseTypes ORDER BY c.resolvedTTL DESC")
    @Query("SELECT c FROM CaseDataEntity c WHERE c.resolvedTTL < CURRENT_DATE ORDER BY c.resolvedTTL DESC")
    List<CaseDataEntity> findExpiredCases(/*@Param("queryCaseTypes") List queryCaseTypes*/);

}
