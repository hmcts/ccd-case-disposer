package uk.gov.hmcts.reform.ccd.shell.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.shell.data.CcdCreateCaseEventResponse;
import uk.gov.hmcts.reform.ccd.shell.model.ShellCasePayload;
import uk.gov.hmcts.reform.ccd.shell.service.client.CcdClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShellCaseCreatorTest {

    private static final String SERVICE_TOKEN = "service-token";
    private static final String USER_TOKEN = "user-token";
    private static final String SHELL_CASE_TYPE = "ShellCaseType";
    private static final String EVENT_TOKEN = "event-token";
    private static final Long ORIGINAL_CASE_REFERENCE = 1234567890123456L;
    private static final String ORIGINAL_CASE_TYPE = "OriginalCaseType";

    @Mock
    private SecurityUtil securityUtil;
    @Mock
    private CcdClient ccdClient;

    private ShellCaseCreator underTest;

    @BeforeEach
    void setUp() {
        underTest = new ShellCaseCreator(securityUtil, ccdClient);
        when(securityUtil.getServiceAuthorization()).thenReturn(SERVICE_TOKEN);
        when(securityUtil.getIdamClientToken()).thenReturn(USER_TOKEN);
    }

    @Test
    void shouldBuildPayloadWithOriginalCaseMetadataAndCreateEventToken() {
        CaseData originalCase = CaseData.builder()
            .reference(ORIGINAL_CASE_REFERENCE)
            .caseType(ORIGINAL_CASE_TYPE)
            .build();
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode().put("mapped_field", "mapped value");
        when(ccdClient.getCreateCaseToken(SERVICE_TOKEN, USER_TOKEN, "CREATE", SHELL_CASE_TYPE))
            .thenReturn(new CcdCreateCaseEventResponse(EVENT_TOKEN));

        ShellCasePayload result = underTest.build(originalCase, mappedData, SHELL_CASE_TYPE);

        assertThat(result.eventToken()).isEqualTo(EVENT_TOKEN);
        assertThat(result.data().get("mapped_field").asString()).isEqualTo("mapped value");
        assertThat(result.data().get("original_case_reference").asString())
            .isEqualTo(ORIGINAL_CASE_REFERENCE.toString());
        assertThat(result.data().get("original_case_type").asString()).isEqualTo(ORIGINAL_CASE_TYPE);
        verify(ccdClient).getCreateCaseToken(SERVICE_TOKEN, USER_TOKEN, "CREATE", SHELL_CASE_TYPE);
    }

    @Test
    void shouldSubmitPayloadToCcd() {
        ShellCasePayload payload = new ShellCasePayload(JsonNodeFactory.instance.objectNode(), EVENT_TOKEN);

        underTest.create(payload, SHELL_CASE_TYPE);

        verify(ccdClient).createCase(SERVICE_TOKEN, USER_TOKEN, SHELL_CASE_TYPE, payload);
    }
}
