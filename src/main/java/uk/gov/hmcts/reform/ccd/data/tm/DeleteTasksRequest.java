package uk.gov.hmcts.reform.ccd.data.tm;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@EqualsAndHashCode
@ToString
public class DeleteTasksRequest implements Serializable {

    private static final long serialVersionUID = 432973322;

    private final DeleteCaseTasksAction deleteCaseTasksAction;

    @JsonCreator
    public DeleteTasksRequest(@JsonProperty("deleteCaseTasksAction") @JsonAlias("delete_case_tasks_action")
                                  DeleteCaseTasksAction deleteCaseTasksAction) {
        this.deleteCaseTasksAction = deleteCaseTasksAction;
    }

    public DeleteCaseTasksAction getDeleteCaseTasksAction() {
        return deleteCaseTasksAction;
    }
}
