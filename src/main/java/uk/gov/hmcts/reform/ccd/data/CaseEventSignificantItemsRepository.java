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

    @Modifying
    @Query(value = "DELETE FROM case_event_significant_items WHERE case_event_id = :caseEventId", nativeQuery = true)
    void deleteByCaseEventId(@Param("caseEventId") Long caseEventId);

    List<CaseEventSignificantItemsEntity> findByCaseEventId(final Long caseEventId);
}
