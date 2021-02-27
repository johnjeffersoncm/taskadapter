package com.taskadapter.connector;

import com.taskadapter.connector.definition.FieldMapping;
import com.taskadapter.model.AllFields;
import com.taskadapter.model.Description$;
import com.taskadapter.model.DueDate$;
import org.junit.Test;
import scala.Option;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NewConfigSuggesterTest {
    private static final FieldMapping summaryM = new FieldMapping(Option.apply(AllFields.summary()),
            Option.apply(AllFields.summary()), true, null);
    private static final FieldMapping descriptionM = new FieldMapping(Option.apply(Description$.MODULE$),
            Option.apply(Description$.MODULE$), true, null);
    private static final FieldMapping dueDateM = new FieldMapping(Option.apply(DueDate$.MODULE$),
            Option.apply(DueDate$.MODULE$), true, null);

    @Test
    public void suggestsElementsFromLeftConnector() {
        var list = NewConfigSuggester.suggestedFieldMappingsForNewConfig(
                List.of(AllFields.summary(), AllFields.description(), AllFields.assigneeLoginName()),
                List.of(AllFields.summary(), AllFields.description()));

        assertThat(list).containsOnly(summaryM, descriptionM);
    }

    @Test
    public void suggestsElementsFromRightConnector() {
        var list = NewConfigSuggester.suggestedFieldMappingsForNewConfig(
                java.util.List.of(AllFields.summary(), AllFields.description(), AllFields.dueDate()),
                java.util.List.of(AllFields.summary(), AllFields.description(), AllFields.priority(), AllFields.dueDate()));

        assertThat(list).containsOnly(summaryM, descriptionM, dueDateM);
    }

    @Test
    public void emptyListsGiveEmptyResult() {
        assertThat(
                NewConfigSuggester.suggestedFieldMappingsForNewConfig(List.of(), List.of()))
                .isEmpty();
    }

    @Test
    public void emptyLeftListGivesEmptyResults() {
        assertThat(
                NewConfigSuggester.suggestedFieldMappingsForNewConfig(List.of(), Arrays.asList(AllFields.summary())))
                .isEmpty();
    }

    @Test
    public void emptyRightListGivesEmptyResults() {
        assertThat(
                NewConfigSuggester.suggestedFieldMappingsForNewConfig(Arrays.asList(AllFields.summary()), java.util.List.of()))
                .isEmpty();
    }

}
