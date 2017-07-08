package com.taskadapter.connector.redmine;

import com.taskadapter.connector.Priorities;
import com.taskadapter.model.GRelation;
import com.taskadapter.model.GTask;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.IssueRelationFactory;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.TrackerFactory;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.UserFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RedmineToGTaskTest {

    private RedmineToGTask toGTask;

    @Before
    public void beforeEachTest() {
        RedmineConfig config = new RedmineConfig();
        toGTask = new RedmineToGTask(config);
    }

    @Test
    public void summaryIsConverted() {
        Issue redmineIssue = new Issue();
        redmineIssue.setSubject("text 1");
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals("text 1", task.getSummary());
    }

    @Test
    public void descriptionIsConverted() {
        Issue redmineIssue = new Issue();
        redmineIssue.setDescription("description 1");
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals("description 1", task.getDescription());
    }

    @Test
    public void idIsConvertedIfSet() {
        Issue redmineIssue = IssueFactory.create(123);
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals((Integer) 123, task.getId());
    }

    @Test
    public void idIsIgnoredIfNull() {
        Issue redmineIssue = new Issue();
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertNull(task.getId());
    }

    @Test
    public void idIsSetToKey() {
        Issue redmineIssue = IssueFactory.create(123);
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals("123", task.getKey());
    }

    @Test
    public void parentIdIsConvertedIfSet() {
        Issue redmineIssue = new Issue();
        redmineIssue.setParentId(123);
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals("123", task.getParentKey());
    }

    @Test
    public void parentIdIsIgnoredIfNotSet() {
        Issue redmineIssue = new Issue();
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertNull(task.getParentKey());
    }

    @Test
    public void assigneeIsIgnoredIfNotSet() {
        Issue redmineIssue = new Issue();
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertNull(task.getValue(RedmineField.assignee()));
    }

    @Test
    public void assigneeIsConvertedIfSet() {
        Issue redmineIssue = new Issue();
        User assignee = UserFactory.create();
        assignee.setLogin("mylogin");
        redmineIssue.setAssignee(assignee);
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        User loadedAssignee = (User) task.getValue(RedmineField.assignee());
        assertEquals("mylogin", loadedAssignee.getLogin());
    }

    @Test
    public void trackerTypeIsConvertedIfSet() {
        Issue redmineIssue = new Issue();
        Tracker tracker = TrackerFactory.create(123, "something");
        redmineIssue.setTracker(tracker);
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals("something", task.getType());
    }

    @Test
    public void trackerTypeIsIgnoredIfNotSet() {
        Issue redmineIssue = new Issue();
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertNull(task.getType());
    }

    @Test
    public void statusIsConverted() {
        Issue redmineIssue = new Issue();
        redmineIssue.setStatusName("some status");
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals("some status", task.getValue(RedmineField.taskStatus()));
    }

    @Test
    public void estimatedHoursAreConverted() {
        Issue redmineIssue = new Issue();
        redmineIssue.setEstimatedHours(55f);
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals((Float) 55f, task.getEstimatedHours());
    }

    @Test
    public void doneRatioIsConverted() {
        Issue redmineIssue = new Issue();
        redmineIssue.setDoneRatio(75);
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals((Integer) 75, task.getDoneRatio());
    }

    @Test
    public void startDateIsConverted() {
        Issue redmineIssue = new Issue();
        Date time = getTime();
        redmineIssue.setStartDate(time);
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals(time, task.getStartDate());
    }

    private Date getTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 23, 0, 0, 0);
        return calendar.getTime();
    }

    @Test
    public void dueDateIsConverted() {
        Issue redmineIssue = new Issue();
        Date time = getTime();
        redmineIssue.setDueDate(time);
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals(time, task.getValue(RedmineField.dueDate()));
    }

    @Test
    public void createdOnIsConverted() {
        Issue redmineIssue = new Issue();
        Date time = getTime();
        redmineIssue.setCreatedOn(time);
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals(time, task.getCreatedOn());
    }

    @Test
    public void updatedOnIsConverted() {
        Issue redmineIssue = new Issue();
        Date time = getTime();
        redmineIssue.setUpdatedOn(time);
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals(time, task.getUpdatedOn());
    }

    @Test
    public void priorityIsAssignedDefaultValueIfNotSet() {
        Issue redmineIssue = new Issue();
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals(Priorities.DEFAULT_PRIORITY_VALUE, task.getPriority());
    }

    @Test
    public void priorityIsConvertedIfSet() {
        Issue redmineIssue = new Issue();
        redmineIssue.setPriorityText("High");
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals(700, task.getValue(RedmineField.priority()));
    }

    @Test
    public void priorityIsAssignedDefaultValueIfUnknownValueSet() {
        Issue redmineIssue = new Issue();
        redmineIssue.setPriorityText("some unknown text");
        GTask task = toGTask.convertToGenericTask(redmineIssue);
        assertEquals(Priorities.DEFAULT_PRIORITY_VALUE, task.getPriority());
    }

    @Test
    public void relationsAreConverted() {
        Issue redmineIssue = IssueFactory.create(10);
//        Issue blockedIssue = IssueFactory.create(20);
        IssueRelation relation = IssueRelationFactory.create();
        relation.setType(IssueRelation.TYPE.precedes.toString());
        relation.setIssueId(10);
        relation.setIssueToId(20);
        redmineIssue.addRelations(Collections.singletonList(relation));
        GTask task = toGTask.convertToGenericTask(redmineIssue);

        assertEquals(1, task.getRelations().size());
        GRelation gRelation = task.getRelations().get(0);
        assertEquals("10", gRelation.getTaskKey());
        assertEquals("20", gRelation.getRelatedTaskKey());
        assertEquals(GRelation.TYPE.precedes, gRelation.getType());
        assertNull(gRelation.getDelay());
    }
}
