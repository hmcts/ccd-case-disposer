package uk.gov.hmcts.reform.ccd.utils;

public interface RemoteDeletionVerifier<T> {

    T snapshot();

    void clear();
}
