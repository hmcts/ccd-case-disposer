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
        when(securityUtil.getServiceAuthorization()).thenReturn(SERVICE_TOKEN);
        when(securityUtil.getIdamClientToken()).thenReturn(IDAM_TOKEN);
    }

    @Test
    void shouldReturnEmptyResponseWhenShellMappingDoesNotExist() {
        when(shellMappingClient.getShellMappings(SERVICE_TOKEN, IDAM_TOKEN, CASE_TYPE_ID))
            .thenReturn(null);

        ShellMappingResponse response = shellMappingService.loadMappings(CASE_TYPE_ID);

        assertThat(response).isNotNull();
        assertThat(response.getShellCaseTypeID()).isNull();
        assertThat(response.getShellCaseMappings()).isEmpty();
    }

    @Test
    void shouldReturnEmptyResponseWhenClientReturnsNullBody() {
        when(shellMappingClient.getShellMappings(SERVICE_TOKEN, IDAM_TOKEN, CASE_TYPE_ID))
            .thenReturn(null);

        ShellMappingResponse response = shellMappingService.loadMappings(CASE_TYPE_ID);

        assertThat(response).isNotNull();
        assertThat(response.getShellCaseTypeID()).isNull();
        assertThat(response.getShellCaseMappings()).isEmpty();
    }

    @Test
    void shouldReturnEmptyMappingEntryForCaseTypeWhenShellMappingDoesNotExist() {
        when(shellMappingClient.getShellMappings(SERVICE_TOKEN, IDAM_TOKEN, CASE_TYPE_ID))
            .thenReturn(null);

        Map<String, ShellMappingResponse> responseMap = shellMappingService.getShellMappings(List.of(CASE_TYPE_ID));

        assertThat(responseMap).containsOnlyKeys(CASE_TYPE_ID);
        assertThat(responseMap.get(CASE_TYPE_ID).getShellCaseTypeID()).isNull();
        assertThat(responseMap.get(CASE_TYPE_ID).getShellCaseMappings()).isEmpty();
    }

    @Test
    void shouldPropagateNon404Errors() {
        RuntimeException serviceUnavailable = new RuntimeException("Service unavailable");
        when(shellMappingClient.getShellMappings(SERVICE_TOKEN, IDAM_TOKEN, CASE_TYPE_ID))
            .thenThrow(serviceUnavailable);

        assertThatThrownBy(() -> shellMappingService.loadMappings(CASE_TYPE_ID))
            .isSameAs(serviceUnavailable);
    }
}



