package uk.gov.hmcts.reform.ccd.shell.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.shell.model.ShellMappingResponse;
import uk.gov.hmcts.reform.ccd.shell.service.client.ShellMappingClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShellMappingServiceTest {

    private static final String SERVICE_TOKEN = "service-token";
    private static final String IDAM_TOKEN = "idam-token";
    private static final String CASE_TYPE_ID = "case-type-id";

    @Mock
    private ShellMappingClient shellMappingClient;

    @Mock
    private SecurityUtil securityUtil;

    private ShellMappingService shellMappingService;

    @BeforeEach
    void setUp() {
        shellMappingService = new ShellMappingService(shellMappingClient, securityUtil);
    }

    @Test
    void shouldReturnEmptyResponseWhenShellMappingDoesNotExist() {
        stubAuthTokens();
        when(shellMappingClient.getShellMappings(SERVICE_TOKEN, IDAM_TOKEN, CASE_TYPE_ID))
            .thenReturn(null);

        ShellMappingResponse response = shellMappingService.loadMappings(CASE_TYPE_ID);

        assertThat(response)
            .isNotNull()
            .satisfies(r -> {
                assertThat(r.getShellCaseTypeID()).isNull();
                assertThat(r.getShellCaseMappings()).isEmpty();
            });
    }


    @Test
    void shouldReturnClientResponseWhenShellMappingExists() {
        stubAuthTokens();
        ShellMappingResponse expectedResponse = new ShellMappingResponse("shell-case-type-id");
        when(shellMappingClient.getShellMappings(SERVICE_TOKEN, IDAM_TOKEN, CASE_TYPE_ID))
            .thenReturn(expectedResponse);

        ShellMappingResponse response = shellMappingService.loadMappings(CASE_TYPE_ID);

        assertThat(response).isSameAs(expectedResponse);
    }

    @Test
    void shouldReturnEmptyMapWhenCaseTypesIsNull() {
        Map<String, ShellMappingResponse> responseMap = shellMappingService.getShellMappings(null);

        assertThat(responseMap).isEmpty();
        verifyNoInteractions(shellMappingClient);
    }

    @Test
    void shouldReturnEmptyMapWhenCaseTypesIsEmpty() {
        Map<String, ShellMappingResponse> responseMap = shellMappingService.getShellMappings(List.of());

        assertThat(responseMap).isEmpty();
        verifyNoInteractions(shellMappingClient);
    }

    @Test
    void shouldPropagateNon404Errors() {
        stubAuthTokens();
        RuntimeException serviceUnavailable = new RuntimeException("Service unavailable");
        when(shellMappingClient.getShellMappings(SERVICE_TOKEN, IDAM_TOKEN, CASE_TYPE_ID))
            .thenThrow(serviceUnavailable);

        assertThatThrownBy(() -> shellMappingService.loadMappings(CASE_TYPE_ID))
            .isSameAs(serviceUnavailable);
    }

    private void stubAuthTokens() {
        when(securityUtil.getServiceAuthorization()).thenReturn(SERVICE_TOKEN);
        when(securityUtil.getIdamClientToken()).thenReturn(IDAM_TOKEN);
    }
}



