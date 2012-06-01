package com.taskadapter.web.configeditor;

import com.taskadapter.connector.definition.AvailableFields;
import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.definition.Mapping;
import com.taskadapter.connector.definition.Mappings;
import com.taskadapter.connector.definition.ValidationException;
import com.taskadapter.model.GTaskDescriptor;
import com.taskadapter.web.Messages;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;

import java.util.*;

/**
 * @author Alexey Skorokhodov
 */
public class FieldsMappingPanel extends Panel implements Validatable, ConfigPanel {
    private static final String PANEL_TITLE = "Task fields";
    private static final String COLUMN1_HEADER = "Task Adapter field";
    private static final String COLUMN2_HEADER = "System field or constraint";

    private Map<GTaskDescriptor.FIELD, CheckBox> fieldToButtonMap = new HashMap<GTaskDescriptor.FIELD, CheckBox>();
    private Map<GTaskDescriptor.FIELD, ComboBox> fieldToValueMap = new HashMap<GTaskDescriptor.FIELD, ComboBox>();

    private final AvailableFields availableFieldsProvider;
    
    /**
     * Mappings to edit.
     */
    private final Mappings mappings;
    
    private static final int COLUMNS_NUMBER = 2;
    private GridLayout gridLayout;
    private Resource helpIconResource = new ThemeResource("../runo/icons/16/help.png");

    public FieldsMappingPanel(AvailableFields availableFieldsProvider, Mappings mappings) {
        super("Task fields mapping");
        this.availableFieldsProvider = availableFieldsProvider;
        this.mappings = mappings;

        setDescription("Select fields to export when SAVING data to this system");
        addFields();
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
        label1.addStyleName("fieldsTitle");
        label1.setWidth("135px");
        gridLayout.addComponent(label1, 0, 0);
        Label label2 = new Label(COLUMN2_HEADER);
        label2.addStyleName("fieldsTitle");
        gridLayout.addComponent(label2, 1, 0);
        gridLayout.addComponent(new Label("<hr>", Label.CONTENT_XHTML), 0, 1, 1, 1);
    }

    private void addSupportedFields() {
        Collection<GTaskDescriptor.FIELD> supportedFields = availableFieldsProvider.getSupportedFields();
        for (GTaskDescriptor.FIELD field : supportedFields) {
            addField(field);
        }
    }

    // TODO refactor this complex method
    private void addField(GTaskDescriptor.FIELD field) {
        CheckBox checkbox = addCheckbox(field);

        if (!mappings.haveMappingFor(field)) {
            // means this config does not have a mapping for this field, which
            // availableFieldsProvider reported as "supported": probably OLD config
            return;
        }

        checkbox.setValue(mappings.isFieldSelected(field));

        String[] allowedValues = availableFieldsProvider.getAllowedValues(field);
        BeanItemContainer<String> container = new BeanItemContainer<String>(String.class);

        if (allowedValues.length > 1) {
            container.addAll(Arrays.asList(allowedValues));
            ComboBox combo = new ComboBox(null, container);
            combo.setWidth("160px");
            fieldToValueMap.put(field, combo);
            gridLayout.addComponent(combo);
            gridLayout.setComponentAlignment(combo, Alignment.MIDDLE_LEFT);
            combo.select(mappings.getMappedTo(field));
        } else if (allowedValues.length == 1) {
            Label label = new Label(allowedValues[0]);
            gridLayout.addComponent(label);
            gridLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
        } else {
            markFieldNotSupportedByThisConnector(checkbox);
        }
    }

    private CheckBox addCheckbox(GTaskDescriptor.FIELD field) {
        CheckBox checkbox = new CheckBox(GTaskDescriptor.getDisplayValue(field));

        String helpForField = getHelpForField(field);
        if (helpForField != null) {
            HorizontalLayout layout = addHelpTipToCheckbox(checkbox, helpForField);
            gridLayout.addComponent(layout);
            gridLayout.setComponentAlignment(layout, Alignment.MIDDLE_LEFT);
        } else {
            gridLayout.addComponent(checkbox);
            gridLayout.setComponentAlignment(checkbox, Alignment.MIDDLE_LEFT);
        }
        fieldToButtonMap.put(field, checkbox);

        return checkbox;
    }

    private String getHelpForField(GTaskDescriptor.FIELD field) {
        return Messages.getMessageDefaultLocale(field.toString());
    }

    private HorizontalLayout addHelpTipToCheckbox(CheckBox checkbox, String helpForField) {
        Embedded helpIcon = new Embedded(null, helpIconResource);
        helpIcon.setDescription(helpForField);
        HorizontalLayout layout = new HorizontalLayout();
        layout.addComponent(checkbox);
        layout.addComponent(helpIcon);
        return layout;
    }

    private void markFieldNotSupportedByThisConnector(CheckBox checkbox) {
        checkbox.setEnabled(false);
        checkbox.setValue(false);
    }

    public Mappings getResult() {
    	final Mappings result = new Mappings();
        for (GTaskDescriptor.FIELD f : availableFieldsProvider.getSupportedFields()) {
            boolean selected = fieldToButtonMap.get(f).booleanValue();
            String value = null;
            ComboBox combo = fieldToValueMap.get(f);
            if (combo != null) {
                value = (String) combo.getValue();
            }
            mappings.setMapping(f, selected, value);
        }
        return result;
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

    @Override
    public void setDataToConfig(ConnectorConfig config) {
        config.setFieldsMapping(getResult());
    }

    @Override
    public void initDataByConfig(ConnectorConfig config) {

    }
}

