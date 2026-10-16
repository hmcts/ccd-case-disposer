package uk.gov.hmcts.reform.ccd.data;

import jakarta.inject.Inject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;


@SuppressWarnings({"java:S2187", "PMD.TestClassWithoutTestCases"})
@ContextConfiguration(classes = {RepositoryTestConfiguration.class})
public class BaseRepositoryTest {

    @Inject
    private DataSource dataSource;

    public void insertDataIntoDatabase(final String scriptPath) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            final ClassPathResource resource = new ClassPathResource(scriptPath);
            ScriptUtils.executeSqlScript(connection, resource);
        }
    }
}
