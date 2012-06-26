package com.taskadapter.connector.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.definition.ProgressMonitor;
import com.taskadapter.connector.definition.SyncResult;
import com.taskadapter.connector.definition.TaskError;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.model.GRelation;
import com.taskadapter.model.GTask;

public abstract class AbstractTaskSaver<T extends ConnectorConfig> {

    protected final SyncResult<Throwable> syncResult = new SyncResult<Throwable>();

    private final List<GTask> totalTaskList = new ArrayList<GTask>();

    protected final T config;

    private ProgressMonitor monitor;

    public AbstractTaskSaver(T config) {
        super();
        this.config = config;
    }

    abstract protected Object convertToNativeTask(GTask task) throws ConnectorException ;

    abstract protected GTask createTask(Object nativeTask) throws ConnectorException;

    abstract protected void updateTask(String taskId, Object nativeTask) throws ConnectorException;
    
    /**
     * the default implementation does nothing.
     */
    protected void beforeSave() throws ConnectorException {
        // nothing here
    }

    public SyncResult<Throwable> saveData(List<GTask> tasks, ProgressMonitor monitor) throws ConnectorException {
        this.monitor = monitor;

        beforeSave();
        save(null, tasks);

        return syncResult;
    }

    /**
     * this method will go through children itself.
     */
    protected SyncResult<Throwable> save(String parentIssueKey, List<GTask> tasks) throws ConnectorException {
        Iterator<GTask> it = tasks.iterator();
        while (it.hasNext()) {
            GTask task = it.next();
            totalTaskList.add(task);

            String newTaskKey = null;
            try {
                if (parentIssueKey != null) {
                    task.setParentKey(parentIssueKey);
                }
                Object nativeIssueToCreateOrUpdate = convertToNativeTask(task);
                newTaskKey = submitTask(task, nativeIssueToCreateOrUpdate);
            } catch (ConnectorException e) {
                syncResult.addError(new TaskError<Throwable>(task, e));
            } catch (Throwable t) {
                syncResult.addError(new TaskError<Throwable>(task, t));               
            }
            reportProgress();

            if (!task.getChildren().isEmpty()) {
                save(newTaskKey, task.getChildren());
            }
        }

        if (parentIssueKey == null) {
            if (config.getSaveIssueRelations()) {
                List<GRelation> relations = buildNewRelations(totalTaskList);
                saveRelations(relations);
            }
        }

        return syncResult;
    }

    private void reportProgress() {
        if (monitor != null) {
            monitor.worked(1);
        }
    }

    protected List<GRelation> buildNewRelations(List<GTask> tasks) {
        List<GRelation> newRelations = new ArrayList<GRelation>();
        for (GTask task : tasks) {
            String newSourceTaskKey = syncResult.getRemoteKey(task.getId());
            for (GRelation oldRelation : task.getRelations()) {
                // XXX get rid of the conversion, it won't work with Jira,
                // which has String Keys like "TEST-12"
                Integer relatedTaskId = Integer.parseInt(oldRelation.getRelatedTaskKey());
                String newRelatedKey = syncResult.getRemoteKey(relatedTaskId);
                // #25443 Export from MSP fails when newRelatedKey is null (which is a valid case in MSP)
                if (newSourceTaskKey != null && newRelatedKey != null) {
                    newRelations.add(new GRelation(newSourceTaskKey, newRelatedKey, oldRelation.getType()));
                }
            }
        }
        return newRelations;
    }

    abstract protected void saveRelations(List<GRelation> relations) throws ConnectorException;

    /**
     * @return the newly created task's KEY
     * @throws ConnectorException 
     */
    protected String submitTask(GTask task, Object nativeTask) throws ConnectorException {
        String newTaskKey;
        if (task.getRemoteId() == null) {
            GTask newTask = createTask(nativeTask);

            // Need this to be passed as the parentIssueId to the recursive call below
            newTaskKey = newTask.getKey();
            syncResult.addCreatedTask(task.getId(), newTaskKey);
        } else {
            newTaskKey = task.getRemoteId();
            updateTask(newTaskKey, nativeTask);
            syncResult.addUpdatedTask(task.getId(), newTaskKey);
        }
        return newTaskKey;
    }
}
