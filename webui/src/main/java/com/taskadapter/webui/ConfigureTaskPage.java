package com.taskadapter.webui;

import com.google.common.base.Strings;
import com.taskadapter.config.ConnectorDataHolder;
import com.taskadapter.config.TAFile;
import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.definition.ValidationException;
import com.taskadapter.web.PluginEditorFactory;
import com.taskadapter.web.configeditor.ConfigEditor;
import com.vaadin.ui.*;

/**
 * @author Alexey Skorokhodov
 */
public class ConfigureTaskPage extends Page {
    private VerticalLayout layout = new VerticalLayout();
    private TAFile file;
    private TextField name;
    private ConfigEditor panel1;
    private ConfigEditor panel2;
    private TabSheet tabSheet;
    private Label errorMessageLabel = new Label();
    private String activeTabLabel;

    public ConfigureTaskPage() {
    }

    private void buildUI() {
        layout.removeAllComponents();
        layout.setSpacing(true);
//        addLinkToTaskOverviewPage();
        HorizontalLayout buttonsLayout = new HorizontalLayout();

        Button saveButton = new Button("Save");
        saveButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                save();
            }
        });
        buttonsLayout.addComponent(saveButton);
        errorMessageLabel.addStyleName("error-message-label");
        buttonsLayout.addComponent(errorMessageLabel);
        layout.addComponent(buttonsLayout);

        name = new TextField("Name");
        name.setValue(file.getConfigLabel());
        layout.addComponent(name);

        tabSheet = new TabSheet();
        tabSheet.setSizeUndefined();

        ConnectorDataHolder leftConnectorDataHolder = file.getConnectorDataHolder1();
        panel1 = getPanel(leftConnectorDataHolder);
        tabSheet.addTab(panel1, getPanelCaption(leftConnectorDataHolder));

        ConnectorDataHolder rightConnectorDataHolder = file.getConnectorDataHolder2();
        panel2 = getPanel(rightConnectorDataHolder);
        tabSheet.addTab(panel2, getPanelCaption(rightConnectorDataHolder));

        if (!Strings.isNullOrEmpty(activeTabLabel)) {
            tabSheet.setSelectedTab(
                    activeTabLabel.equals(leftConnectorDataHolder.getData().getLabel())
                            ? panel1
                            : activeTabLabel.equals(rightConnectorDataHolder.getData().getLabel())
                                    ? panel2 : panel1
            );
        }

        layout.addComponent(tabSheet);
    }

//    private void addLinkToTaskOverviewPage() {
//        Button button = new Button(file.getConfigLabel());
//        button.setStyleName(BaseTheme.BUTTON_LINK);
//        button.addListener(new Button.ClickListener() {
//            @Override
//            public void buttonClick(Button.ClickEvent event) {
//                navigator.showTaskDetailsPage(file);
//            }
//        });
//        layout.addComponent(button);
//    }

    private String getPanelCaption(ConnectorDataHolder connectorDataHolder) {
        return connectorDataHolder.getData().getLabel();
    }

    public void setFile(TAFile file) {
        this.file = file;
    }

    private void save() {
        boolean valid = true;

        try {
            panel1.validateAll();
        } catch (ValidationException e) {
            errorMessageLabel.setValue(e.getMessage());
            tabSheet.setSelectedTab(panel1);
            valid = false;
        }

        if (valid) {
            try {
                panel2.validateAll();
            } catch (ValidationException e) {
                errorMessageLabel.setValue(e.getMessage());
                tabSheet.setSelectedTab(panel2);
                valid = false;
            }
        }

        if (valid) {
            updateFileWithDataInForm();
            services.getConfigStorage().saveConfig(file);
            navigator.showNotification("Saved", "All saved OK");

            errorMessageLabel.setValue("");
            navigator.show(Navigator.HOME);
        }
    }

    private void updateFileWithDataInForm() {
        ConnectorConfig c1 = panel1.getConfig();
        ConnectorConfig c2 = panel2.getConfig();
        ConnectorDataHolder d1 = new ConnectorDataHolder(file.getConnectorDataHolder1().getType(), c1);
        ConnectorDataHolder d2 = new ConnectorDataHolder(file.getConnectorDataHolder2().getType(), c2);

        file.setConfigLabel((String) name.getValue());
        file.setConnectorDataHolder1(d1);
        file.setConnectorDataHolder2(d2);
    }

    private ConfigEditor getPanel(ConnectorDataHolder dataHolder) {
        ConnectorConfig configData = dataHolder.getData();
        PluginEditorFactory editorFactory = services.getEditorManager().getEditorFactory(dataHolder.getType());
        return editorFactory.createEditor(configData, services);
    }

    @Override
    public String getPageTitle() {
        return "Configure \"" + file.getConfigLabel() + "\"";
    }

    @Override
    public Component getUI() {
        buildUI();
        return layout;
    }

    public void setActiveTabLabel(String dataHolderLabel) {
        activeTabLabel = dataHolderLabel;
    }

    public void setErrorMessage(String errorMessage) {
        errorMessageLabel.setValue(errorMessage);
    }
}
