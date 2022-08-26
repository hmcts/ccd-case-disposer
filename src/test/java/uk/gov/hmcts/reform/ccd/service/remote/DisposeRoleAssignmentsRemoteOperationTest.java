package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.am.QueryResponse;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsRequest;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsResponse;
import uk.gov.hmcts.reform.ccd.exception.RoleAssignmentDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.RoleDeletionRecordHolder;

import java.util.Collections;
import java.util.UUID;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_ROLE_PATH;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.QUERY_ROLE_PATH;

@DisplayName("dispose case role assignments")
@ExtendWith(MockitoExtension.class)
class DisposeRoleAssignmentsRemoteOperationTest {

    @Mock
    private RestClientBuilder restClientBuilder;

    @Mock
    private RoleDeletionRecordHolder roleDeletionRecordHolder;

    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private DisposeRoleAssignmentsRemoteOperation disposeRoleAssignmentsRemoteOperation;

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("should post role assignment delete remote dispose request without query")
    void shouldPostRoleAssignmentsDeleteRemoteDisposeRequest() {

        final Gson gson = new Gson();
        final Response response = mock(Response.class);

        final String caseRef = "1234567890123456";
        final RoleAssignmentsRequest roleAssignmentsDeleteRequest =
            new RoleAssignmentsRequest(caseRef);

        doReturn("http://localhost").when(parameterResolver).getRoleAssignmentsHost();
        doReturn(false).when(parameterResolver).getCheckCaseRolesExist();

        when(response.getStatus()).thenReturn(200);

        final String requestBody = gson.toJson(roleAssignmentsDeleteRequest);

        when(restClientBuilder.postRequestWithAllHeaders("http://localhost", DELETE_ROLE_PATH, requestBody)).thenReturn(response);

        disposeRoleAssignmentsRemoteOperation.postRoleAssignmentsDelete(caseRef);

        verify(roleDeletionRecordHolder, times(1)).setCaseRolesDeletionResults("1234567890123456",
                                                                               200);
        verify(restClientBuilder, times(1)).postRequestWithAllHeaders("http://localhost",
                                                                      DELETE_ROLE_PATH,
                                                                      requestBody);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("should post role assignment delete remote dispose request with query")
    void shouldPostRoleAssignmentsDeleteRemoteDisposeRequestAfterQuery() {

        final Gson gson = new Gson();
        final Response dResponse = mock(Response.class);
        final Response qResponse = mock(Response.class);

        final String caseRef = "1234567890123456";
        final RoleAssignmentsRequest roleAssignmentsDeleteRequest =
            new RoleAssignmentsRequest(caseRef);

        final RoleAssignmentsRequest roleAssignmentsQueryRequest =
            new RoleAssignmentsRequest(caseRef);

        RoleAssignmentsResponse roleAssignmentsResponse = new RoleAssignmentsResponse();
        QueryResponse queryResponse = new QueryResponse();
        queryResponse.setId(UUID.randomUUID());
        roleAssignmentsResponse.setRoleAssignmentResponse(Collections.singletonList(queryResponse));

        doReturn("http://localhost").when(parameterResolver).getRoleAssignmentsHost();
        doReturn(true).when(parameterResolver).getCheckCaseRolesExist();

        when(dResponse.getStatus()).thenReturn(200);
        when(qResponse.getStatus()).thenReturn(200);
        when(qResponse.readEntity(RoleAssignmentsResponse.class)).thenReturn(roleAssignmentsResponse);

        final String requestDeleteBody = gson.toJson(roleAssignmentsDeleteRequest);
        final String requestQueryBody = gson.toJson(roleAssignmentsQueryRequest);

        when(restClientBuilder.postRequestWithAllHeaders("http://localhost", DELETE_ROLE_PATH, requestDeleteBody)).thenReturn(dResponse);
        when(restClientBuilder.postRequestWithRoleAssignmentFetchContentType("http://localhost", QUERY_ROLE_PATH, requestQueryBody)).thenReturn(qResponse);

        disposeRoleAssignmentsRemoteOperation.postRoleAssignmentsDelete(caseRef);

        verify(roleDeletionRecordHolder, times(1)).setCaseRolesDeletionResults("1234567890123456",
                                                                               200);
        verify(restClientBuilder, times(1)).postRequestWithAllHeaders("http://localhost",
                                                                      DELETE_ROLE_PATH,
                                                                      requestDeleteBody);
        verify(restClientBuilder, times(1)).postRequestWithRoleAssignmentFetchContentType("http://localhost",
                                                                      QUERY_ROLE_PATH,
                                                                      requestQueryBody);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("should skip post role assignment delete if no roles found")
    void shouldSkipPostRoleAssignmentsDeleteRemoteDisposeRequestIfNoRolesFound() {

        final Gson gson = new Gson();
        final Response qResponse = mock(Response.class);

        final String caseRef = "1234567890123456";
        final RoleAssignmentsRequest roleAssignmentsQueryRequest =
            new RoleAssignmentsRequest(caseRef);

        RoleAssignmentsResponse roleAssignmentsResponse = new RoleAssignmentsResponse();
        roleAssignmentsResponse.setRoleAssignmentResponse(Collections.EMPTY_LIST);

        doReturn("http://localhost").when(parameterResolver).getRoleAssignmentsHost();
        doReturn(true).when(parameterResolver).getCheckCaseRolesExist();

        when(qResponse.getStatus()).thenReturn(200);
        when(qResponse.readEntity(RoleAssignmentsResponse.class)).thenReturn(roleAssignmentsResponse);

        final String requestQueryBody = gson.toJson(roleAssignmentsQueryRequest);

        when(restClientBuilder.postRequestWithRoleAssignmentFetchContentType("http://localhost", QUERY_ROLE_PATH, requestQueryBody)).thenReturn(qResponse);

        disposeRoleAssignmentsRemoteOperation.postRoleAssignmentsDelete(caseRef);

        verify(roleDeletionRecordHolder, times(1)).setCaseRolesDeletionResults("1234567890123456",
                                                                               200);
        verify(restClientBuilder, times(0)).postRequestWithAllHeaders(eq("http://localhost"),
                                                                      eq(DELETE_ROLE_PATH),
                                                                      anyString());
        verify(restClientBuilder, times(1)).postRequestWithRoleAssignmentFetchContentType(eq("http://localhost"),
                                                                      eq(QUERY_ROLE_PATH),
                                                                      eq(requestQueryBody));
    }

    @Test
    void shouldThrowExceptionWhenQueryRequestInvalid() {
        try {
            final Response response = mock(Response.class);
            final String caseRef = "1234567890123456";
            doReturn("http://localhost").when(parameterResolver).getRoleAssignmentsHost();
            doReturn(true).when(parameterResolver).getCheckCaseRolesExist();

            when(response.getStatus()).thenReturn(500);

            when(restClientBuilder.postRequestWithRoleAssignmentFetchContentType(eq("http://localhost"), eq(QUERY_ROLE_PATH), anyString())).thenReturn(response);

            disposeRoleAssignmentsRemoteOperation.postRoleAssignmentsDelete(caseRef);

            fail("The method should have thrown DocumentDeletionException when request is invalid");
        } catch (final RoleAssignmentDeletionException roleAssignmentDeletionException) {
            assertThat(roleAssignmentDeletionException.getMessage())
                .isEqualTo("Error deleting role assignments for case : 1234567890123456");
        }
    }

    @Test
    void shouldThrowExceptionWhenDeleteRequestInvalid() {
        try {
            final String caseRef = "1234567890123456";
            final String jsonRequest = new Gson().toJson(new RoleAssignmentsRequest("1234567890123456"));
            doReturn("http://localhost").when(parameterResolver).getRoleAssignmentsHost();
            doReturn(false).when(parameterResolver).getCheckCaseRolesExist();

            doThrow(new RoleAssignmentDeletionException(caseRef))
                    .when(restClientBuilder)
                    .postRequestWithAllHeaders("http://localhost", DELETE_ROLE_PATH, jsonRequest);

            disposeRoleAssignmentsRemoteOperation.postRoleAssignmentsDelete(caseRef);

            fail("The method should have thrown DocumentDeletionException when request is invalid");
        } catch (final RoleAssignmentDeletionException roleAssignmentDeletionException) {
            assertThat(roleAssignmentDeletionException.getMessage())
                    .isEqualTo("Error deleting role assignments for case : 1234567890123456");
        }
    }
}
