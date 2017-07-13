package com.taskadapter.core;

import com.taskadapter.connector.FieldRow;
import com.taskadapter.connector.NewConnector;
import com.taskadapter.connector.common.ProgressMonitorUtils;
import com.taskadapter.connector.definition.Mappings;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.connector.msp.MSPConfig;
import com.taskadapter.connector.msp.MSPConnector;
import com.taskadapter.connector.msp.MspField;
import com.taskadapter.connector.testlib.TestMappingUtils;
import com.taskadapter.integrationtests.MSPConfigLoader;
import com.taskadapter.model.GTask;
import com.taskadapter.model.GTaskDescriptor;
import net.sf.mpxj.TaskField;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TaskLoaderTest {

    @Test
    /**
     * This test should grant that Tasks ARE read as a tree by the SyncRunner. The code that does that
     * was moved to save(), but it cannot be done because the tree is not shown to the user.
     */
    public void tasksAreLoadedAsTree() throws URISyntaxException, IOException, ConnectorException {
        MSPConfig mspConfig = MSPConfigLoader.generateTemporaryConfig("com/taskadapter/integrationtests/ProjectWithTree.xml");
//        mappings.setMapping(GTaskDescriptor.FIELD.REMOTE_ID, true, TaskField.TEXT22.toString(), "default remote ID");

        NewConnector projectConnector = new MSPConnector(mspConfig);
        int maxTasksNumber = 999999;
        List<GTask> list = TaskLoader.loadTasks(maxTasksNumber, projectConnector, "project1", ProgressMonitorUtils.DUMMY_MONITOR);
        assertEquals(1, list.size());
    }

}
