package uk.gov.hmcts.reform.ccd.util.log;

import dnl.utils.text.table.TextTable;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_STATE;

class TableTextBuilderTest {

    @Test
    void shouldCreateTableText() {
        final CaseDataView caseDataView = new CaseDataView("CAVEAT", 123L, DELETED_STATE);

        final TextTable textTable = new TableTextBuilder().buildTextTable(List.of(caseDataView));

        assertThat(textTable).isNotNull();
        assertThat(textTable.getTableModel().getColumnName(0)).isEqualTo("Case Type");
        assertThat(textTable.getTableModel().getColumnName(1)).isEqualTo("Case ID");
        assertThat(textTable.getTableModel().getColumnName(2)).isEqualTo("Delete State");
        assertThat(textTable.getTableModel().getRowCount()).isEqualTo(1);
        assertThat(textTable.getTableModel().getValueAt(0, 0)).isEqualTo("CAVEAT");
        assertThat(textTable.getTableModel().getValueAt(0, 1)).isEqualTo("123");
        assertThat(textTable.getTableModel().getValueAt(0, 2)).isEqualTo(DELETED_STATE);
    }
}
