package uk.gov.hmcts.reform.ccd.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventEntity;

@Repository
public interface CaseEventRepository extends JpaRepository<CaseEventEntity, Long> {


    @Modifying
    @Query(value = "DELETE FROM case_event WHERE case_data_id = :caseDataId", nativeQuery = true)
    void deleteByCaseDataId(@Param("caseDataId") Long caseDataId);

}
