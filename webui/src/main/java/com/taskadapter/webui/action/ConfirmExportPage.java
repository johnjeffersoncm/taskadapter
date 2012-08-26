package com.taskadapter.webui.action;

import com.taskadapter.connector.definition.Connector;
import com.taskadapter.model.GTask;
import com.taskadapter.web.configeditor.FieldsMappingPanel;
import com.taskadapter.webui.Navigator;
import com.taskadapter.webui.PageUtil;
import com.vaadin.ui.*;

import java.util.List;

public class ConfirmExportPage extends CustomComponent {
    private final Navigator navigator;
    private List<GTask> rootLevelTasks;
    private Connector connectorTo;
    private Button.ClickListener goListener;
    private FieldsMappingPanel fieldMappingPanel;
    private MyTree connectorTree;

    public ConfirmExportPage(Navigator navigator, List<GTask> rootLevelTasks, Connector destinationConnector, Button.ClickListener goListener) {
        this.navigator = navigator;
        this.rootLevelTasks = rootLevelTasks;
        this.connectorTo = destinationConnector;
        this.goListener = goListener;
        buildUI();
    }

    private void buildUI() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        Label text1 = new Label("Please confirm export to " + connectorTo.getConfig().getTargetLocation());
        layout.addComponent(text1);

        connectorTree = new MyTree();
        connectorTree.setSizeFull();
        connectorTree.setTasks(rootLevelTasks);
        layout.addComponent(connectorTree);

        Button goButton = new Button("Go");
        goButton.addListener(goListener);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.addComponent(goButton);
        buttonsLayout.addComponent(PageUtil.createButton(navigator, "Cancel", Navigator.HOME));
        layout.addComponent(buttonsLayout);

		this.fieldMappingPanel = new FieldsMappingPanel(
                connectorTo.getDescriptor().getAvailableFields(),
                connectorTo.getConfig().getFieldMappings()
        );
        layout.addComponent(fieldMappingPanel);

        setCompositionRoot(layout);
    }

    public boolean needToSaveConfig() {
        return fieldMappingPanel.haveChanges(); 
    }

    public List<GTask> getSelectedRootLevelTasks() {
        return connectorTree.getSelectedRootLevelTasks();
    }
}
