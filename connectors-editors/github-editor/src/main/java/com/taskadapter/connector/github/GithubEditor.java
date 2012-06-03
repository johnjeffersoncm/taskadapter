package com.taskadapter.connector.github;

import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.web.configeditor.FieldsMappingPanel;
import com.taskadapter.web.configeditor.ProjectPanel;
import com.taskadapter.web.configeditor.ServerPanel;
import com.taskadapter.web.configeditor.TwoColumnsConfigEditor;
import com.taskadapter.web.service.Services;

public class GithubEditor extends TwoColumnsConfigEditor {

    public GithubEditor(ConnectorConfig config, Services services) {
        super(config, services);
        buildUI();
    }

    private void buildUI() {
        // top left and right
        createServerAndProjectPanelOnTopDefault(new GithubProjectProcessor(this));

        final ServerPanel serverPanel = getPanel(ServerPanel.class);
        serverPanel.disableServerURLField();

        final ProjectPanel projectPanel =  getPanel(ProjectPanel.class);
        projectPanel.setProjectKeyLabel("Repository ID");
        projectPanel.hideQueryId();

        // left
        addToLeftColumn(new OtherGithubFieldsPanel(this));

        //right
        addToRightColumn(new FieldsMappingPanel(GithubDescriptor.instance.getAvailableFields(), config.getFieldMappings()));
    }

    @Override
    public ConnectorConfig getPartialConfig() {
        return config;
    }
}
