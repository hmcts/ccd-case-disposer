package uk.gov.hmcts.reform.ccd.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventSignificantItemsEntity;

import java.util.List;

@Repository
public interface CaseEventSignificantItemsRepository extends JpaRepository<CaseEventSignificantItemsEntity, Long> {

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM case_event_significant_items si"
        + "    USING case_event ce"
        + "    WHERE si.case_event_id = ce.id AND ce.case_data_id = :caseDataId;", nativeQuery = true)
    void deleteByCaseDataId(@Param("caseDataId") Long caseDataId);

    @Query(value = "SELECT si.id FROM case_event_significant_items si"
        + "    INNER JOIN case_event ce"
        + "    ON si.case_event_id = ce.id"
        + "    where ce.case_data_id = :caseDataId;", nativeQuery = true)
    List<Long> findByCaseDataId(@Param("caseDataId") Long caseDataId);
}
