package uk.gov.hmcts.reform.ccd.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventSignificantItemsEntity;

import java.util.List;

@Repository
public interface CaseEventSignificantItemsRepository extends JpaRepository<CaseEventSignificantItemsEntity, Long> {
    List<CaseEventSignificantItemsEntity> findByCaseEventId(final Long caseEventId);
}
