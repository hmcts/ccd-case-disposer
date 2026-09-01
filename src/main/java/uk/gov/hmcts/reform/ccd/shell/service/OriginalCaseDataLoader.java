package uk.gov.hmcts.reform.ccd.shell.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.shell.data.CcdCaseResponse;
import uk.gov.hmcts.reform.ccd.shell.service.client.CcdClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

@RequiredArgsConstructor
@Service
public class OriginalCaseDataLoader {

    private final CcdClient ccdClient;
    private final SecurityUtil securityUtil;

    public CcdCaseResponse load(Long caseReference) {
        return ccdClient.getOriginalCaseData(
            securityUtil.getServiceAuthorization(),
            securityUtil.getIdamClientToken(),
            caseReference);
    }
}
