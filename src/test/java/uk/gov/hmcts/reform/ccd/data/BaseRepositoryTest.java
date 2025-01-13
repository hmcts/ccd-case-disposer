package uk.gov.hmcts.reform.ccd.data;

import jakarta.inject.Inject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;


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
