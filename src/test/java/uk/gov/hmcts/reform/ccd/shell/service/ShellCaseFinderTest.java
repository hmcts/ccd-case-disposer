package uk.gov.hmcts.reform.ccd.shell.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.ccd.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.data.CcdCaseSearchResponse;
import uk.gov.hmcts.reform.ccd.shell.service.client.CcdClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShellCaseFinderTest {

    private static final String SERVICE_TOKEN = "service-token";
    private static final String USER_TOKEN = "user-token";
    private static final String SHELL_CASE_TYPE = "ShellCaseType";
    private static final long ORIGINAL_CASE_REFERENCE = 1234567890123456L;
    private static final long SHELL_CASE_REFERENCE = 9876543210123456L;

    @Mock
    private SecurityUtil securityUtil;
    @Mock
    private CcdClient ccdClient;

    private ShellCaseFinder underTest;

    @BeforeEach
    void setUp() {
        underTest = new ShellCaseFinder(securityUtil, ccdClient);
        when(securityUtil.getServiceAuthorization()).thenReturn(SERVICE_TOKEN);
        when(securityUtil.getIdamClientToken()).thenReturn(USER_TOKEN);
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
        FeignException clientFailure = org.mockito.Mockito.mock(FeignException.class);
        when(ccdClient.searchCase(
            eq(SERVICE_TOKEN), eq(USER_TOKEN), eq(SHELL_CASE_TYPE), any(JsonNode.class)))
            .thenThrow(clientFailure);

        assertThatThrownBy(() -> underTest.findShellCase(SHELL_CASE_TYPE, ORIGINAL_CASE_REFERENCE))
            .isInstanceOf(ShellCaseException.class)
            .hasCause(clientFailure);
    }
}
