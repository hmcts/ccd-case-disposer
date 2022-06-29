package uk.gov.hmcts.reform.ccd.util.log;

import dnl.utils.text.table.TextTable;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;

import static uk.gov.hmcts.reform.ccd.util.LogConstants.COLUMN_NAMES;

@Named
public class TableTextBuilder {

    public TextTable buildTextTable(final List<CaseDataView> caseDataViews) {
        final Object[][] data = new Object[caseDataViews.size()][COLUMN_NAMES.size()];
        final List<Object[]> rowData = getRowData(caseDataViews);

        for (int i = 0; i < caseDataViews.size(); i++) {
            for (int j = 0; j < COLUMN_NAMES.size(); j++) {
                data[i][j] = rowData.get(i)[j];
            }
        }
        final TextTable textTable = new TextTable(COLUMN_NAMES.toArray(new String[0]), data);
        textTable.setSort(0);

        return textTable;
    }

    private List<Object[]> getRowData(final List<CaseDataView> caseDataViews) {
        final List<Object[]> rowData = new ArrayList<>();

        caseDataViews.forEach(caseDataView -> {
            final Object[] caseDataFields = {caseDataView.getCaseType(), caseDataView.getCaseRef().toString(),
                    caseDataView.getState(), caseDataView.getLinkedCaseIds()};
            rowData.add(caseDataFields);
        });
        return rowData;
    }
}
