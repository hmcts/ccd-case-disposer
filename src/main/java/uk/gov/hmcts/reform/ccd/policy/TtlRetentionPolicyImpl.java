package uk.gov.hmcts.reform.ccd.policy;

import lombok.NonNull;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.time.LocalDate;

public class TtlRetentionPolicyImpl implements RetentionPolicy {
    @Override
    public Boolean mustRetain(@NonNull final CaseData caseData) {
        return !isExpired(caseData.getResolvedTtl());
    }

    private boolean isExpired(final LocalDate caseTtl) {
        final LocalDate today = LocalDate.now();
        return caseTtl != null && caseTtl.isBefore(today);
    }
}
