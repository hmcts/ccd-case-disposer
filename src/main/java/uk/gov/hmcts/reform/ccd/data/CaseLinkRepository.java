package uk.gov.hmcts.reform.ccd.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkPrimaryKey;

import java.util.List;

@Repository
public interface CaseLinkRepository extends CrudRepository<CaseLinkEntity, CaseLinkPrimaryKey> {

    List<CaseLinkEntity> findByCaseId(final Long caseId);

    List<CaseLinkEntity> findByLinkedCaseId(final Long linkedCaseId);

    @Async
    void delete(CaseLinkEntity caseLinkEntity);
}
