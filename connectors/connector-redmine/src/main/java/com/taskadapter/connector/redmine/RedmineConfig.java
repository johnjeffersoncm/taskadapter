package com.taskadapter.connector.redmine;

import com.google.common.base.Objects;
import com.taskadapter.connector.Priorities;
import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.definition.Mappings;
import com.taskadapter.connector.definition.WebServerInfo;
import com.taskadapter.model.GTaskDescriptor;

import java.util.HashMap;

public class RedmineConfig extends ConnectorConfig {

    static final String DEFAULT_LABEL = "Redmine";

    private static final long serialVersionUID = 1L;
    private static final String TASK_TYPE_BUG = "Bug";

    private WebServerInfo serverInfo = new WebServerInfo();

    private String defaultTaskStatus = "New";
    private boolean findUserByName;
    private Integer queryId;
    private String projectKey;

    public RedmineConfig() {
        setLabel(DEFAULT_LABEL);
        setDefaultTaskType(TASK_TYPE_BUG);
    }

    public String getDefaultTaskStatus() {
        return defaultTaskStatus;
    }

    public void setDefaultTaskStatus(String defaultTaskStatus) {
        this.defaultTaskStatus = defaultTaskStatus;
    }

    // TODO move to Descriptor?
    @Override
    public Mappings generateDefaultFieldsMapping() {
    	final Mappings result = new Mappings();
        result.addField(GTaskDescriptor.FIELD.SUMMARY);
        result.addField(GTaskDescriptor.FIELD.TASK_TYPE);
        result.addField(GTaskDescriptor.FIELD.TASK_STATUS);
        result.addField(GTaskDescriptor.FIELD.ESTIMATED_TIME);
        result.addField(GTaskDescriptor.FIELD.DONE_RATIO);
        result.addField(GTaskDescriptor.FIELD.ASSIGNEE);
        result.addField(GTaskDescriptor.FIELD.DESCRIPTION);
        result.addField(GTaskDescriptor.FIELD.START_DATE);
        result.addField(GTaskDescriptor.FIELD.DUE_DATE);
        return result;
    }

    @Override
    protected Priorities generateDefaultPriorities() {
        return new Priorities(new HashMap<String, Integer>() {
            private static final long serialVersionUID = 516389048716909610L;

            {
                put("Low", 100);
                put("Normal", 500);
                put("High", 700);
                put("Urgent", 800);
                put("Immediate", 1000);
            }
        });
    }


    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(getDefaultTaskType(), defaultTaskStatus);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof RedmineConfig) {
            RedmineConfig other = (RedmineConfig) obj;
            return Objects.equal(getDefaultTaskType(), other.getDefaultTaskType())
                    && Objects.equal(defaultTaskStatus, other.defaultTaskStatus);
        } else {
            return false;
        }
    }

    public WebServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(WebServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public String getSourceLocation() {
        return serverInfo.getHost();
    }

    @Override
    public String getTargetLocation() {
        // target is the same as source for web-based configs
        return getSourceLocation();
    }

    public boolean isFindUserByName() {
        return findUserByName;
    }

    public void setFindUserByName(boolean findUserByName) {
        this.findUserByName = findUserByName;
    }

    public Integer getQueryId() {
        return queryId;
    }

    public void setQueryId(Integer queryId) {
        this.queryId = queryId;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }
}
