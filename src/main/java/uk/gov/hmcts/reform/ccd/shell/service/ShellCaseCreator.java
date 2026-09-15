package uk.gov.hmcts.reform.ccd.shell.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.shell.model.ShellCasePayload;
import uk.gov.hmcts.reform.ccd.shell.service.client.CcdClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

@Service
@RequiredArgsConstructor
public class ShellCaseCreator {

    public static final String ORIGINAL_CASE_REFERENCE = "original_case_reference";
    private static final String ORIGINAL_CASE_TYPE = "original_case_type";
    private static final String TRIGGER_ID = "CREATE";

    private final SecurityUtil securityUtil;
    private final CcdClient ccdClient;

    public ShellCasePayload build(CaseData originalCaseData, ObjectNode shellCaseData, String shellCaseType) {
        String eventToken = ccdClient.getCreateCaseToken(
            securityUtil.getServiceAuthorization(),
            securityUtil.getIdamClientToken(),
            TRIGGER_ID,
            shellCaseType).token();
        shellCaseData.put(ORIGINAL_CASE_REFERENCE, originalCaseData.getReference().toString());
        shellCaseData.put(ORIGINAL_CASE_TYPE, originalCaseData.getCaseType());

        return new ShellCasePayload(shellCaseData, eventToken);
    }

    public void create(ShellCasePayload payload, String shellCaseType) {
        ccdClient.createCase(
            securityUtil.getServiceAuthorization(),
            securityUtil.getIdamClientToken(),
            shellCaseType,
            payload
        );
    }
}
