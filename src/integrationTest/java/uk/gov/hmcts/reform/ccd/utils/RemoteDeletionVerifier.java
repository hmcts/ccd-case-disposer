package uk.gov.hmcts.reform.ccd.utils;

import java.util.List;

public interface RemoteDeletionVerifier {
    void verifyRemoteDeletion(final List<Long> caseRefs);
}
