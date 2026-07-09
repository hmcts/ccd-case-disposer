package uk.gov.hmcts.reform.ccd.utils;

import java.util.List;

@FunctionalInterface
public interface RemoteDeletionVerifier {
    void verifyRemoteDeletion(List<Long> caseRefs);
}
