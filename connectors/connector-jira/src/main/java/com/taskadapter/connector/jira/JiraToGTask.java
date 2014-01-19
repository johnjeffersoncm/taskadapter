package com.taskadapter.connector.jira;

import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.IssueLink;
import com.atlassian.jira.rest.client.domain.TimeTracking;
import com.taskadapter.connector.Priorities;
import com.taskadapter.model.GRelation;
import com.taskadapter.model.GTask;
import com.taskadapter.model.GUser;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class JiraToGTask {
    private static final Logger logger = LoggerFactory.getLogger(JiraToGTask.class);

    private final Priorities priorities;

    public JiraToGTask(Priorities priorities) {
        this.priorities = priorities;
    }

    public List<GTask> convertToGenericTaskList(List<Issue> issues) {
        // TODO see http://jira.atlassian.com/browse/JRA-6896
//        logger.info("Jira: no tasks hierarchy is supported");

        List<GTask> rootLevelTasks = new ArrayList<GTask>();

        for (Issue issue : issues) {
            GTask genericTask = convertToGenericTask(issue);
            rootLevelTasks.add(genericTask);
        }
        return rootLevelTasks;
    }

    public GTask convertToGenericTask(Issue issue) {
        GTask task = new GTask();
        Integer intId = Integer.parseInt(issue.getId());
        task.setId(intId);
        task.setKey(issue.getKey());

        if (issue.getAssignee() != null) {
            String jiraUserLogin = issue.getAssignee().getName();
            String jiraDisplayName = issue.getAssignee().getDisplayName();

            GUser genericUser = new GUser();
            genericUser.setLoginName(jiraUserLogin);
            genericUser.setDisplayName(jiraDisplayName);
            task.setAssignee(genericUser);
        }

        task.setType(issue.getIssueType().getName());
        task.setSummary(issue.getSummary());
        task.setDescription(issue.getDescription());

        DateTime dueDate = issue.getDueDate();
        if (dueDate != null) {
            task.setDueDate(dueDate.toDate());
        }

        // TODO set Done Ratio
        // task.setDoneRatio(issue.getDoneRatio());

        String jiraPriorityName = null;
        if (issue.getPriority() != null) {
            jiraPriorityName = issue.getPriority().getName();
        }
        Integer priorityValue = priorities.getPriorityByText(jiraPriorityName);
        task.setPriority(priorityValue);

        TimeTracking timeTracking = issue.getTimeTracking();
        if (timeTracking != null) {
            Integer originalEstimateMinutes = timeTracking.getOriginalEstimateMinutes();
            if (originalEstimateMinutes != null
                    && !originalEstimateMinutes.equals(0)) {
                task.setEstimatedHours((float) (originalEstimateMinutes / 60.0));
            }
        }

        processRelations(issue, task);
        processParentTask(issue, task);
        return task;
    }

    private static void processParentTask(Issue issue, GTask task) {
        if (issue.getIssueType().isSubtask()) {
            Object parent = issue.getFieldByName("Parent").getValue();
            JSONObject json = (JSONObject) parent;
            try {
                String parentKey = (String) json.get("key");
                task.setParentKey(parentKey);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static void processRelations(Issue issue, GTask genericTask) {
        Iterable<IssueLink> links = issue.getIssueLinks();
        if (links != null) {
            for (IssueLink link : links) {
                if (link.isOutbound()) {
                    String name = link.getIssueLinkType().getName();
                    if (name.equals(JiraConstants.getJiraLinkNameForPrecedes())) {
                        GRelation r = new GRelation(issue.getId(), JiraUtils.getIdFromURI(link.getTargetIssueUri()), GRelation.TYPE.precedes);
                        genericTask.getRelations().add(r);
                    } else {
                        logger.error("relation type is not supported: " + link.getIssueLinkType());
                    }
                }
            }
        }
    }

}
