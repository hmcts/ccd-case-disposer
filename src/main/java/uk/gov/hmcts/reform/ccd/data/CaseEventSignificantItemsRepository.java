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

    List<CaseEventSignificantItemsEntity> findByCaseEventId(final Long caseEventId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM CaseEventSignificantItemsEntity sf WHERE sf.caseEventId = :caseEventId")
    void deleteByCaseEventId(@Param("caseEventId") Long caseEventId);
}
