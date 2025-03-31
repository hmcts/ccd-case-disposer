package uk.gov.hmcts.reform.ccd.data;

import jakarta.inject.Inject;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.config.WireMockStubs;
import uk.gov.hmcts.reform.ccd.config.es.TestContainers;
import uk.gov.hmcts.reform.ccd.utils.DatabaseIntegrationTestUtils;
import uk.gov.hmcts.reform.ccd.utils.ElasticSearchIntegrationTestUtils;
import uk.gov.hmcts.reform.ccd.utils.RemoteDeletionVerifier;
import uk.gov.hmcts.reform.ccd.utils.SimulationIntegrationTestUtils;

import java.util.List;
import java.util.Map;

import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;
import static uk.gov.hmcts.reform.ccd.config.TestParameterResolver.DELETABLE_CASE_TYPES_PROPERTY;
import static uk.gov.hmcts.reform.ccd.config.TestParameterResolver.DELETABLE_CASE_TYPES_PROPERTY_SIMULATION;


public class TestDataProvider extends TestContainers {

    @Inject
    private SimulationIntegrationTestUtils simulationIntegrationTestUtils;

    @Inject
    private ElasticSearchIntegrationTestUtils elasticSearchIntegrationTestUtils;

    @Inject
    private DatabaseIntegrationTestUtils databaseIntegrationTestUtils;

    @Autowired
    private List<RemoteDeletionVerifier> remoteDeletionVerifiers;

    @Inject
    private WireMockStubs wireMockStubs;

    protected void setupData(final String deletableCaseTypes,
                             final String deletableCaseTypesSimulation,
                             final String ccdScriptPath,
                             final List<Long> rowIds,
                             final Map<String, List<Long>> indexedData) throws Exception {

        setSystemProperty(DELETABLE_CASE_TYPES_PROPERTY, deletableCaseTypes);
        setSystemProperty(DELETABLE_CASE_TYPES_PROPERTY_SIMULATION, deletableCaseTypesSimulation);

        wireMockStubs.setUpStubs(WIREMOCK_SERVER);

        elasticSearchIntegrationTestUtils.resetIndices(indexedData);
        elasticSearchIntegrationTestUtils.createElasticSearchIndex(indexedData);

        databaseIntegrationTestUtils.insertDataIntoDatabase(ccdScriptPath);

        databaseIntegrationTestUtils.verifyDatabaseIsPopulated(rowIds);
        elasticSearchIntegrationTestUtils.verifyCaseDataAreInElasticsearch(indexedData);
    }

    protected void verifyDatabaseDeletion(final List<Long> rowIds) {
        databaseIntegrationTestUtils.verifyDatabaseDeletion(rowIds);
    }

    protected void verifyElasticsearchDeletion(final Map<String, List<Long>> deletedFromIndexed,
                                               final Map<String, List<Long>> notDeletedFromIndexed) {
        elasticSearchIntegrationTestUtils.verifyElasticsearchDeletion(deletedFromIndexed, notDeletedFromIndexed);
    }

    protected void verifyDatabaseDeletionSimulation(final List<Long> simulatedEndStateRowIds) {
        simulationIntegrationTestUtils.verifyDatabaseDeletionSimulation(simulatedEndStateRowIds);
    }

    protected void verifyRemoteDeletion(final List<Long> caseRefs) {
        remoteDeletionVerifiers.forEach(remoteDeletionVerifier ->
                remoteDeletionVerifier.verifyRemoteDeletion(caseRefs));
    }

    private void setSystemProperty(String propertyName, final String value) {
        if (value != null) {
            setProperty(propertyName, value);
        } else {
            clearProperty(propertyName);
        }
    }
}
