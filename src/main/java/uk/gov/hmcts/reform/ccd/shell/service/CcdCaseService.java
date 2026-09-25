package uk.gov.hmcts.reform.ccd.shell.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.shell.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.model.CcdCaseResponse;
import uk.gov.hmcts.reform.ccd.shell.model.CcdCaseSearchResponse;
import uk.gov.hmcts.reform.ccd.shell.model.CcdCreateCaseEventResponse;
import uk.gov.hmcts.reform.ccd.shell.model.ShellCasePayload;
import uk.gov.hmcts.reform.ccd.shell.service.client.CcdClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CcdCaseService {

    private static final String ORIGINAL_CASE_REFERENCE = "original_case_reference";
    private static final String ORIGINAL_CASE_TYPE = "original_case_type";
    private static final String TRIGGER_ID = "createCase";
    private static final int NUMBER_OF_RESULTS = 5;
    private static final int SINGLE_RESULT = 1;

    private final CcdClient ccdClient;
    private final SecurityUtil securityUtil;

    public CcdCaseResponse loadCase(Long caseReference) {
        try {
            CcdCaseResponse response = ccdClient.getOriginalCaseData(
                securityUtil.getServiceAuthorization(),
                securityUtil.getIdamClientToken(),
                caseReference
            );
            if (response == null || response.data() == null) {
                throw new ShellCaseException("CCD returned invalid case data - " + caseReference);
            }
            return response;
        } catch (FeignException exc) {
            throw new ShellCaseException("Failed to load original case: " + caseReference, exc);
        }
    }

    public Optional<Long> findShellCase(String shellCaseType, long originalCaseReference) {
        ObjectNode request = buildSearchRequest(String.valueOf(originalCaseReference));
        try {
            CcdCaseSearchResponse response = ccdClient.searchCase(
                securityUtil.getServiceAuthorization(),
                securityUtil.getIdamClientToken(),
                shellCaseType,
                request
            );
            validateSearchResponse(response, originalCaseReference);

            if (response.total() > SINGLE_RESULT) {
                log.warn("More than a single result returned from case search - total {}", response.total());
            }

            return response.cases().stream()
                .map(CcdCaseSearchResponse.EsCase::reference)
                .findFirst();
        } catch (FeignException exc) {
            throw new ShellCaseException("failed to search for case: " + originalCaseReference, exc);
        }
    }

    public void createShellCase(CaseData originalCaseData, ObjectNode shellCaseData, String shellCaseType) {
        try {
            CcdCreateCaseEventResponse response = ccdClient.getCreateCaseToken(
                securityUtil.getServiceAuthorization(),
                securityUtil.getIdamClientToken(),
                TRIGGER_ID,
                shellCaseType
            );
            if (response == null || response.token() == null || response.token().isBlank()) {
                throw new ShellCaseException(
                    "Invalid create case token returned from CCD for case " + originalCaseData.getReference());
            }
            shellCaseData.put(ORIGINAL_CASE_REFERENCE, originalCaseData.getReference().toString());
            shellCaseData.put(ORIGINAL_CASE_TYPE, originalCaseData.getCaseType());
            ShellCasePayload payload = ShellCasePayload.withEvent(shellCaseData, response.token(), TRIGGER_ID);
            ccdClient.createCase(
                securityUtil.getServiceAuthorization(),
                securityUtil.getIdamClientToken(),
                shellCaseType,
                payload
            );
        } catch (FeignException exc) {
            throw new ShellCaseException("Failed to create shell case " + originalCaseData.getReference(), exc);
        }
    }

    private void validateSearchResponse(CcdCaseSearchResponse response, long originalCaseReference) {
        if (response == null || response.cases() == null) {
            throw new ShellCaseException(
                "CCD returned invalid shell case search response while searching for " + originalCaseReference);
        }

        if (response.total() != null && response.total() > 0 && response.cases().isEmpty()) {
            throw new ShellCaseException(
                "ES reported found results (total=" + response.total()
                    + ") but returned empty list for case: " +  originalCaseReference);
        }
    }

    private ObjectNode buildSearchRequest(String originalCaseReference) {
        // {"query": {
        //     "term": {
        //         "data.original_case_reference.keyword": originalCaseReference
        //      }
        // }, "size": 5}
        ObjectNode query = JsonNodeFactory.instance.objectNode();
        String propertyName = "data." + ORIGINAL_CASE_REFERENCE + ".keyword";
        query
            .put("size", NUMBER_OF_RESULTS)
            .putObject("query")
            .putObject("term")
            .put(propertyName, originalCaseReference);

        return query;
    }
}
