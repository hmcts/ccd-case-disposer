package uk.gov.hmcts.reform.ccd.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventEntity;

@Repository
public interface CaseEventRepository extends JpaRepository<CaseEventEntity, Long> {

    void deleteByCaseDataId(Long caseDataId);

}
