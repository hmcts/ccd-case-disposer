package uk.gov.hmcts.reform.ccd.data.model;

import lombok.Value;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.util.List;

@Value
public class CaseTreeNode {
    CaseDataEntity caseNode;
    List<CaseTreeNode> parentNodes;
}
