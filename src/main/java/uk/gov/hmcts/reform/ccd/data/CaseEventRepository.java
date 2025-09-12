package uk.gov.hmcts.reform.ccd.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventEntity;

import java.util.List;

@Repository
public interface CaseEventRepository extends JpaRepository<CaseEventEntity, Long> {

    List<CaseEventEntity> findByCaseDataId(final Long caseDataId);

}
