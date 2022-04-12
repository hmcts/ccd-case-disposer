package uk.gov.hmcts.reform.ccd.data.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkPrimaryKey;

import java.util.List;

@Repository
public interface CaseLinkRepository extends CrudRepository<CaseLinkEntity, CaseLinkPrimaryKey> {

    List<CaseLinkEntity> findByCaseId(final Long caseId);

    List<CaseLinkEntity> findByLinkedCaseId(final Long linkedCaseId);

    @Query("FROM CaseLinkEntity WHERE linked_case_id IN :queryLinkedCases")
    List<CaseLinkEntity> findAllByLinkedCaseId(@Param("queryLinkedCases") final List<Long> linkedCaseIds);

}
