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

    List<CaseLinkEntity> findByCaseId(Long caseId);

    @Query("SELECT c FROM CaseLinkEntity c WHERE c.caseId = :caseId OR c.linkedCaseId = :caseId")
    List<CaseLinkEntity> findByCaseIdOrLinkedCaseId(Long caseId);

    List<CaseLinkEntity> findByLinkedCaseId(Long linkedCaseId);

    @NativeQuery("""
        WITH expired_ids AS (
                SELECT id FROM case_data WHERE resolved_ttl < CURRENT_DATE AND case_type_id IN :queryCaseTypes)
        SELECT DISTINCT cl.* FROM case_link cl
        JOIN expired_ids expired
        ON cl.case_id = expired.id OR cl.linked_case_id = expired.id
        """)
    List<CaseLinkEntity> findExpiredCaseLinksByCaseTypes(Collection<String> queryCaseTypes);

}
