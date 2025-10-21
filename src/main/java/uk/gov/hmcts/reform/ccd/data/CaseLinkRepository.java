package uk.gov.hmcts.reform.ccd.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkPrimaryKey;

import java.util.Collection;
import java.util.List;

@Repository
public interface CaseLinkRepository extends CrudRepository<CaseLinkEntity, CaseLinkPrimaryKey> {

    List<CaseLinkEntity> findByCaseId(final Long caseId);

    @Query("SELECT c FROM CaseLinkEntity c WHERE c.caseId = :caseId OR c.linkedCaseId = :caseId")
    List<CaseLinkEntity> findByCaseIdOrLinkedCaseId(final Long caseId);

    List<CaseLinkEntity> findByLinkedCaseId(final Long linkedCaseId);

    @Query("SELECT c FROM CaseLinkEntity c WHERE c.caseId in (:caseIds) OR c.linkedCaseId in (:caseIds)")
    List<CaseLinkEntity> findByCaseIdInOrLinkedCaseIdIn(final Collection<Long> caseIds);

    void delete(CaseLinkEntity caseLinkEntity);
}
