package com.taskadapter.connector.github;

import com.taskadapter.connector.common.PriorityLoader;
import com.taskadapter.connector.common.ProjectLoader;
import com.taskadapter.connector.definition.Connector;
import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.definition.Descriptor;
import com.taskadapter.connector.definition.PluginFactory;

public class GithubFactory implements PluginFactory {
    @Override
    public Connector createConnector(ConnectorConfig config) {
        return new GithubConnector((GithubConfig) config);
    }

    @Override
    public Descriptor getDescriptor() {
        return GithubDescriptor.instance;
    }
    
    @Override
    public ProjectLoader getProjectLoader() {
        return new GithubProjectLoader();
    }
    
    @Override
    public PriorityLoader getPriorityLoader() {
        throw new RuntimeException("NOT READY");
    }
}
