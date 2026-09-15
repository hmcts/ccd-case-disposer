package uk.gov.hmcts.reform.ccd.shell.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CcdCaseServiceTest {
    private static final String SERVICE_TOKEN = "service-token";
    private static final String USER_TOKEN = "user-token";
    private static final String SHELL_CASE_TYPE = "ShellCaseType";
    private static final String EVENT_TOKEN = "event-token";
    private static final Long ORIGINAL_CASE_REFERENCE = 1234567890123456L;
    private static final String ORIGINAL_CASE_TYPE = "OriginalCaseType";
    private static final long SHELL_CASE_REFERENCE = 9876543210123456L;

    @Mock
    private SecurityUtil securityUtil;
    @Mock
    private CcdClient ccdClient;

    private CcdCaseService underTest;

    @BeforeEach
    void setUp() {
        underTest = new CcdCaseService(ccdClient, securityUtil);
        when(securityUtil.getServiceAuthorization()).thenReturn(SERVICE_TOKEN);
        when(securityUtil.getIdamClientToken()).thenReturn(USER_TOKEN);
    }

    @Test
    void shouldLoadOriginalCase() {
        CcdCaseResponse response = new CcdCaseResponse(
            ORIGINAL_CASE_REFERENCE,
            ORIGINAL_CASE_TYPE,
            "CaseCreated",
            JsonNodeFactory.instance.objectNode()
        );
        when(ccdClient.getOriginalCaseData(SERVICE_TOKEN, USER_TOKEN, ORIGINAL_CASE_REFERENCE))
            .thenReturn(response);

        CcdCaseResponse result = underTest.loadCase(ORIGINAL_CASE_REFERENCE);

        assertThat(result).isSameAs(response);
        verify(ccdClient).getOriginalCaseData(SERVICE_TOKEN, USER_TOKEN, ORIGINAL_CASE_REFERENCE);
    }

    @Test
    void shouldCreateShellCaseWithOriginalCaseMetadata() {
        CaseData originalCase = CaseData.builder()
            .reference(ORIGINAL_CASE_REFERENCE)
            .caseType(ORIGINAL_CASE_TYPE)
            .build();
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode().put("mapped_field", "mapped value");
        when(ccdClient.getCreateCaseToken(SERVICE_TOKEN, USER_TOKEN, "createCase", SHELL_CASE_TYPE))
            .thenReturn(new CcdCreateCaseEventResponse(EVENT_TOKEN));

        underTest.createShellCase(originalCase, mappedData, SHELL_CASE_TYPE);

        ArgumentCaptor<ShellCasePayload> payloadCaptor = ArgumentCaptor.forClass(ShellCasePayload.class);
        verify(ccdClient).getCreateCaseToken(SERVICE_TOKEN, USER_TOKEN, "createCase", SHELL_CASE_TYPE);
        verify(ccdClient).createCase(
            eq(SERVICE_TOKEN),
            eq(USER_TOKEN),
            eq(SHELL_CASE_TYPE),
            payloadCaptor.capture()
        );
        ShellCasePayload payload = payloadCaptor.getValue();
        assertThat(payload.eventToken()).isEqualTo(EVENT_TOKEN);
        assertThat(payload.data().get("mapped_field").asString()).isEqualTo("mapped value");
        assertThat(payload.data().get("original_case_reference").asString())
            .isEqualTo(ORIGINAL_CASE_REFERENCE.toString());
        assertThat(payload.data().get("original_case_type").asString()).isEqualTo(ORIGINAL_CASE_TYPE);
    }

    @Test
    void shouldReturnEmptyWhenNoShellCaseExists() {
        when(ccdClient.searchCase(
            eq(SERVICE_TOKEN), eq(USER_TOKEN), eq(SHELL_CASE_TYPE), any(JsonNode.class)))
            .thenReturn(new CcdCaseSearchResponse(0L, List.of()));

        Optional<Long> result = underTest.findShellCase(SHELL_CASE_TYPE, ORIGINAL_CASE_REFERENCE);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnShellCaseReferenceWhenShellCaseExists() {
        CcdCaseSearchResponse.EsCase shellCase =
            new CcdCaseSearchResponse.EsCase(SHELL_CASE_REFERENCE);
        when(ccdClient.searchCase(
            eq(SERVICE_TOKEN), eq(USER_TOKEN), eq(SHELL_CASE_TYPE), any(JsonNode.class)))
            .thenReturn(new CcdCaseSearchResponse(1L, List.of(shellCase)));

        Optional<Long> result = underTest.findShellCase(SHELL_CASE_TYPE, ORIGINAL_CASE_REFERENCE);

        assertThat(result).contains(SHELL_CASE_REFERENCE);
    }

    @Test
    void shouldSearchShellCaseTypeByOriginalCaseReference() {
        when(ccdClient.searchCase(
            eq(SERVICE_TOKEN), eq(USER_TOKEN), eq(SHELL_CASE_TYPE), any(JsonNode.class)))
            .thenReturn(new CcdCaseSearchResponse(0L, List.of()));

        underTest.findShellCase(SHELL_CASE_TYPE, ORIGINAL_CASE_REFERENCE);

        ArgumentCaptor<JsonNode> requestCaptor = ArgumentCaptor.forClass(JsonNode.class);
        verify(ccdClient).searchCase(
            eq(SERVICE_TOKEN),
            eq(USER_TOKEN),
            eq(SHELL_CASE_TYPE),
            requestCaptor.capture()
        );

        JsonNode request = requestCaptor.getValue();
        assertThat(request.get("size").asInt()).isEqualTo(5);
        assertThat(request.get("query").get("term")
                       .get("data.original_case_reference.keyword").asString())
            .isEqualTo(String.valueOf(ORIGINAL_CASE_REFERENCE));
    }

    @Test
    void shouldFailWhenCcdReturnsInvalidResponse() {
        when(ccdClient.searchCase(
            eq(SERVICE_TOKEN), eq(USER_TOKEN), eq(SHELL_CASE_TYPE), any(JsonNode.class)))
            .thenReturn(null);

        assertThatThrownBy(() -> underTest.findShellCase(SHELL_CASE_TYPE, ORIGINAL_CASE_REFERENCE))
            .isInstanceOf(ShellCaseException.class)
            .hasMessageContaining(String.valueOf(ORIGINAL_CASE_REFERENCE));
    }

    @Test
    void shouldWrapCcdClientFailure() {
        FeignException clientFailure = mock(FeignException.class);
        when(ccdClient.searchCase(
            eq(SERVICE_TOKEN), eq(USER_TOKEN), eq(SHELL_CASE_TYPE), any(JsonNode.class)))
            .thenThrow(clientFailure);

        assertThatThrownBy(() -> underTest.findShellCase(SHELL_CASE_TYPE, ORIGINAL_CASE_REFERENCE))
            .isInstanceOf(ShellCaseException.class)
            .hasCause(clientFailure);
    }
}
