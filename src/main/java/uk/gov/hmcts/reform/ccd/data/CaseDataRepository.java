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

    @Query(value = "SELECT * FROM case_data WHERE resolved_ttl < CURRENT_DATE AND case_type_id IN (:queryCaseTypes) "
        + "ORDER BY resolved_ttl DESC", nativeQuery = true)
    List<CaseDataEntity> findExpiredCases(@Param("queryCaseTypes") List<String> queryCaseTypes);

    @Query(value = "SELECT * FROM case_data WHERE reference = :queryCaseReference", nativeQuery = true)
    Optional<CaseDataEntity> findByReference(@Param("queryCaseReference") Long queryCaseReference);

    @Modifying
    @Query(value = "DELETE FROM case_data WHERE id = :#{#caseDataEntity.id}", nativeQuery = true)
    void delete(@Param("caseDataEntity") CaseDataEntity caseDataEntity);

}
