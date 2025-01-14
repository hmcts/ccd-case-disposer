package uk.gov.hmcts.reform.ccd.data;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkPrimaryKey;

import java.util.List;

@Repository
public interface CaseLinkRepository extends CrudRepository<CaseLinkEntity, CaseLinkPrimaryKey> {

    @Query(value = "SELECT * FROM case_link WHERE case_id = :caseId", nativeQuery = true)
    List<CaseLinkEntity> findByCaseId(@Param("caseId") Long caseId);

    @Query(value = "SELECT * FROM case_link WHERE case_id = :caseId OR linked_case_id = :caseId", nativeQuery = true)
    List<CaseLinkEntity> findByCaseIdOrLinkedCaseId(@Param("caseId") Long caseId);

    @Query(value = "SELECT * FROM case_link WHERE linked_case_id = :linkedCaseId", nativeQuery = true)
    List<CaseLinkEntity> findByLinkedCaseId(@Param("linkedCaseId") Long linkedCaseId);

    @Modifying
    @Query(value = "DELETE FROM case_link WHERE case_id = :caseId "
        + "AND linked_case_id = :linkedCaseId", nativeQuery = true)
    void delete(@Param("caseId") Long caseId, @Param("linkedCaseId") Long linkedCaseId);
}
