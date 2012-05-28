package com.taskadapter.connector.mantis;

import com.taskadapter.connector.common.PriorityLoader;
import com.taskadapter.connector.common.ProjectLoader;
import com.taskadapter.connector.common.TaskLoader;
import com.taskadapter.connector.common.TaskSaver;
import com.taskadapter.connector.definition.AvailableFields;
import com.taskadapter.connector.definition.AvailableFieldsBuilder;
import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.definition.Descriptor;
import com.taskadapter.connector.definition.PluginFactory;
import com.taskadapter.model.GTaskDescriptor.FIELD;

import java.util.Arrays;
import java.util.Collection;

public class MantisDescriptor implements Descriptor {

    public static final MantisDescriptor instance = new MantisDescriptor();

    private static final String ID = "Mantis";

    private static final String INFO = "Mantis connector (supports Mantis v. 1.1.1+)";

    /**
     * Supported fields.
     */
    private static final AvailableFields SUPPORTED_FIELDS;
    
    static {
    	final AvailableFieldsBuilder builder = AvailableFieldsBuilder.start();
    	builder.addField(FIELD.SUMMARY, "Summary");
    	builder.addField(FIELD.DESCRIPTION, "Description");
    	builder.addField(FIELD.ASSIGNEE, "Assignee");
    	builder.addField(FIELD.DUE_DATE, "Due Date");
    	SUPPORTED_FIELDS = builder.end();
    }
    
    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getLabel() {
        return MantisConfig.DEFAULT_LABEL;
    }

    @Override
    public String getDescription() {
        return INFO;
    }

    @Override
    public ConnectorConfig createDefaultConfig() {
        return new MantisConfig();
    }

    @Override
    public Class<MantisConfig> getConfigClass() {
        return MantisConfig.class;
    }

    @Override
    public AvailableFields getAvailableFieldsProvider() {
        return SUPPORTED_FIELDS;
    }

    @Override
    public ProjectLoader getProjectLoader() {
        return new MantisProjectLoader();
    }

    @Override
    public TaskSaver<MantisConfig> getTaskSaver(ConnectorConfig config) {
        return new MantisTaskSaver((MantisConfig) config);
    }

    @Override
    public TaskLoader getTaskLoader() {
        return new MantisTaskLoader();
    }

    @Override
    public Collection<Feature> getSupportedFeatures() {
        return Arrays.asList(Feature.LOAD_TASK, Feature.SAVE_TASK);
    }

    @Override
    public PriorityLoader getPriorityLoader() {
        throw new RuntimeException("NOT READY");
    }

    @Override
    public PluginFactory getPluginFactory() {
        return new MantisFactory();
    }
}
