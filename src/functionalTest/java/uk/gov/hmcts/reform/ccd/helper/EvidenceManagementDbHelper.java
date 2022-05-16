package uk.gov.hmcts.reform.ccd.helper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.sql.DataSource;

@Service
public class EvidenceManagementDbHelper {

    @Qualifier("evidence")
    @Inject
    private DataSource emDatasource;

    private static final String RECORD_COUNT_SQL = "SELECT COUNT(*) AS recordCount FROM storeddocument where deleted=?";

    private static final String DELETED_DOCS_IDS_SQL = "SELECT id FROM storeddocument where deleted=?";

    public int getDocumentsCount(boolean deletedFlag) throws SQLException {

        int rowCount;

        try (Connection connection = emDatasource.getConnection()) {
            final PreparedStatement preparedStatement = connection.prepareStatement(RECORD_COUNT_SQL);
            preparedStatement.setObject(1, deletedFlag, java.sql.Types.BOOLEAN);

            final ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();
            rowCount = resultSet.getInt("recordCount");
            resultSet.close();
        }

        return rowCount;
    }

    public List<String> getDocumentsIds(boolean deletedFlag) throws SQLException {

        List<String> documentIds = new ArrayList<>();

        try (Connection connection = emDatasource.getConnection()) {
            final PreparedStatement preparedStatement = connection.prepareStatement(DELETED_DOCS_IDS_SQL);
            preparedStatement.setObject(1, deletedFlag, java.sql.Types.BOOLEAN);

            final ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                documentIds.add(resultSet.getString("id"));
            }

            resultSet.close();
        }

        return documentIds;
    }

}
