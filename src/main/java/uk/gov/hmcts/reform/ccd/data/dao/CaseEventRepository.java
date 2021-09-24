package uk.gov.hmcts.reform.ccd.data.dao;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventEntity;

public interface CaseEventRepository extends CrudRepository<CaseEventEntity, Long> {

    void deleteByCaseDataId(Long caseDataId);

}
