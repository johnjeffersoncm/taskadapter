package com.taskadapter.connector.redmine;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.taskadapter.connector.Field;
import com.taskadapter.connector.common.ConfigUtils;
import com.taskadapter.connector.definition.AvailableFields;
import com.taskadapter.connector.definition.Descriptor;
import com.taskadapter.connector.definition.PluginFactory;

import java.util.List;

public class RedmineFactory implements PluginFactory<RedmineConfig> {
    private static final Descriptor DESCRIPTOR = new Descriptor(RedmineConnector.ID, RedmineConfig.DEFAULT_LABEL);
    
    @Override
    public List<Field> getAvailableFields() {
        return RedmineField.fieldsAsJava();
    }

    @Override
    public RedmineConnector createConnector(RedmineConfig config) {
        return new RedmineConnector(config);
    }

    @Override
    public Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

	@Override
	public JsonElement writeConfig(RedmineConfig config) {
		return ConfigUtils.createDefaultGson().toJsonTree(config);
	}

	@Override
	public RedmineConfig readConfig(JsonElement config)
			throws JsonParseException {
		return ConfigUtils.createDefaultGson().fromJson(config,
				RedmineConfig.class);
	}

    @Override
    public RedmineConfig createDefaultConfig() {
        return new RedmineConfig();
    }
}
