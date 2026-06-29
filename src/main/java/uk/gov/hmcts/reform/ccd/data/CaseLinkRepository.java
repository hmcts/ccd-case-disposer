package uk.gov.hmcts.reform.ccd.data;

import org.springframework.data.jpa.repository.NativeQuery;
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

    @NativeQuery("""
        WITH expired_ids AS (
                SELECT id FROM case_data WHERE resolved_ttl < CURRENT_DATE AND case_type_id IN :queryCaseTypes)
        SELECT cl.* FROM case_link cl
        JOIN expired_ids expired
        ON cl.case_id = expired.id OR cl.linked_case_id = expired.id
        """)
    List<CaseLinkEntity> findByCaseIdInOrLinkedCaseIdIn(final Collection<String> queryCaseTypes);

    void delete(CaseLinkEntity caseLinkEntity);
}
