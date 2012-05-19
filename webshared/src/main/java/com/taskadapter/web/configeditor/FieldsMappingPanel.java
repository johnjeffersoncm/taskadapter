package com.taskadapter.web.configeditor;

import com.taskadapter.connector.definition.AvailableFieldsProvider;
import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.definition.Mapping;
import com.taskadapter.connector.definition.ValidationException;
import com.taskadapter.model.GTaskDescriptor;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;

import java.util.*;

/**
 * @author Alexey Skorokhodov
 */
public class FieldsMappingPanel extends Panel implements Validatable {
    private static final String PANEL_TITLE = "Task fields";
    private static final String COLUMN1_HEADER = "Task Adapter field";
    private static final String COLUMN2_HEADER = "System field or constraint";

    private Map<GTaskDescriptor.FIELD, CheckBox> fieldToButtonMap = new HashMap<GTaskDescriptor.FIELD, CheckBox>();
    private Map<GTaskDescriptor.FIELD, ComboBox> fieldToValueMap = new HashMap<GTaskDescriptor.FIELD, ComboBox>();

    private final AvailableFieldsProvider availableFieldsProvider;
    private ConnectorConfig config;
    private static final int COLUMNS_NUMBER = 2;
    private GridLayout gridLayout;

    /**
     * Config Editors should NOT create this object directly, use ConfigEditor.addFieldsMappingPanel() method instead.
     *
     * @see ConfigEditor#addFieldsMappingPanel(com.taskadapter.connector.definition.AvailableFieldsProvider)
     */
    public FieldsMappingPanel(AvailableFieldsProvider availableFieldsProvider, ConnectorConfig config) {
        super("Task fields mapping");
        this.availableFieldsProvider = availableFieldsProvider;
        this.config = config;

        setDescription("Select fields to export when SAVING data to this system");
        addFields();

        addStyleName("panelexample");
        setWidth(DefaultPanel.WIDE_PANEL_WIDTH);
    }

    private void addFields() {
        createGridLayout();
        addTableHeaders();
        addSupportedFields();
    }

    private void createGridLayout() {
        gridLayout = new GridLayout();
        addComponent(gridLayout);
        gridLayout.setMargin(true);
        gridLayout.setSpacing(true);
        Collection<GTaskDescriptor.FIELD> supportedFields = availableFieldsProvider.getSupportedFields();
        gridLayout.setRows(supportedFields.size() + 2);
        gridLayout.setColumns(COLUMNS_NUMBER);
    }

    private void addTableHeaders() {
        Label label1 = new Label(COLUMN1_HEADER);
        gridLayout.addComponent(label1, 0, 0);
        label1.addStyleName("trial-mode-label");
        Label label2 = new Label(COLUMN2_HEADER);
        gridLayout.addComponent(label2, 1, 0);
        label2.addStyleName("trial-mode-label");
        gridLayout.addComponent(new Label("<hr>", Label.CONTENT_XHTML), 0, 1, 1, 1);
    }

    private void addSupportedFields() {
        Collection<GTaskDescriptor.FIELD> supportedFields = availableFieldsProvider.getSupportedFields();
        int row = 1;
        for (GTaskDescriptor.FIELD field : supportedFields) {
            CheckBox checkbox = new CheckBox(GTaskDescriptor.getDisplayValue(field));

            if (field == GTaskDescriptor.FIELD.REMOTE_ID) {
                row = addRemoteIdHelpTip(row, checkbox);
            } else {
                gridLayout.addComponent(checkbox, 0, ++row);
                gridLayout.setComponentAlignment(checkbox, Alignment.MIDDLE_LEFT);
            }

            fieldToButtonMap.put(field, checkbox);
            Mapping mapping = config.getFieldMapping(field);
            if (mapping == null) {
                // means this config does not have a mapping for this field, which
                // availableFieldsProvider reported as "supported": probably OLD config
                continue;
            }

            checkbox.setValue(mapping.isSelected());

            String[] allowedValues = availableFieldsProvider.getAllowedValues(field);
            BeanItemContainer<String> container = new BeanItemContainer<String>(String.class);

            if (allowedValues.length > 1) {
                container.addAll(Arrays.asList(allowedValues));
                ComboBox combo = new ComboBox(null, container);
                combo.setWidth("160px");
                fieldToValueMap.put(field, combo);
                gridLayout.addComponent(combo, 1, row);
                gridLayout.setComponentAlignment(combo, Alignment.MIDDLE_LEFT);
                combo.select(mapping.getCurrentValue());
            } else if (allowedValues.length == 1) {
                Label label = new Label(allowedValues[0]);
                gridLayout.addComponent(label, 1, row);
                gridLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
            } else {
                markFieldNotSupportedByThisConnector(checkbox);
            }
        }
    }

    private int addRemoteIdHelpTip(int row, CheckBox checkbox) {
        Resource res = new ThemeResource("../runo/icons/16/help.png");
        Embedded helpIcon = new Embedded(null, res);
        // TODO move to some resource file
        helpIcon.setDescription("<p>When you load an MSP file into Task Adapter and then export the "
                + "tasks into Redmine / Atlassian Jira / Chiliproject/etc.. , Task Adapter can save "
                + "the IDs of the tasks it creates in those task managment systems. These remote IDs "
                + "are stored in the MSP file itself (assuming that the option is <em>selected</em>). <br />"
                + "This way you can later edit the MSP file (say, using MS Project), load it to Task Adapter "
                + "again and re-export tasks to the same Redmine/Jira/..., so that the tasks without 'remote IDs'"
                + "will be <strong>created</strong> and the old ones (which have been previously created in "
                + "Redmine/jira/...) will be <strong>updated</strong>.</p>"
                + "More information about <a target='_blank' href='http://taskadapter.com/microsoft_project#save_remote_ids'>Remote ID</a>");

        HorizontalLayout remoteLayout = new HorizontalLayout();
        remoteLayout.addComponent(checkbox);
        remoteLayout.addComponent(helpIcon);
        gridLayout.addComponent(remoteLayout, 0, ++row);
        gridLayout.setComponentAlignment(remoteLayout, Alignment.MIDDLE_LEFT);
        return row;
    }

    private void markFieldNotSupportedByThisConnector(CheckBox checkbox) {
        checkbox.setEnabled(false);
        checkbox.setValue(false);
    }

    public Map<GTaskDescriptor.FIELD, Mapping> getResult() {
        Map<GTaskDescriptor.FIELD, Mapping> map = new TreeMap<GTaskDescriptor.FIELD, Mapping>();
        for (GTaskDescriptor.FIELD f : availableFieldsProvider.getSupportedFields()) {
            boolean selected = fieldToButtonMap.get(f).booleanValue();
            String value = null;
            ComboBox combo = fieldToValueMap.get(f);
            if (combo != null) {
                value = (String) combo.getValue();
            }
            Mapping mapping = new Mapping(selected, value);
            map.put(f, mapping);
        }
        return map;
    }

    @Override
    public void validate() throws ValidationException {
        // TODO copied from getResult just to compile the code. REFACTOR THIS!!!
        for (GTaskDescriptor.FIELD f : availableFieldsProvider.getSupportedFields()) {
            boolean selected = fieldToButtonMap.get(f).booleanValue();
            ComboBox combo = fieldToValueMap.get(f);
            if (combo != null) {
                // the field can be mapped to one of SEVERAL options, need to find the combobox
                String selectedOption = (String) combo.getValue();
                if (selected && (selectedOption == null)) {
                    throw new ValidationException(getRequiredFieldErrorMessage(f));
                }
            }
        }
    }

    private String getRequiredFieldErrorMessage(GTaskDescriptor.FIELD f) {
        return "Field \"" +
                GTaskDescriptor.getDisplayValue(f) +
                "\" is selected for export." +
                "\nPlease set the *destination* field or constraint in " + PANEL_TITLE + " section.";
    }
}

