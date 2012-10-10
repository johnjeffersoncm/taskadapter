package com.taskadapter.webui;

import com.google.common.base.Strings;
import com.taskadapter.config.TAFile;
import com.taskadapter.connector.common.ProgressMonitorUtils;
import com.taskadapter.connector.definition.*;
import com.taskadapter.connector.definition.exceptions.CommunicationException;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.core.ConnectorError;
import com.taskadapter.core.SyncRunner;
import com.taskadapter.model.GTask;
import com.taskadapter.web.PluginEditorFactory;
import com.taskadapter.web.configeditor.EditorUtil;
import com.taskadapter.web.configeditor.file.FileDownloadResource;
import com.taskadapter.webui.data.ExceptionFormatter;
import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ExportPage extends ActionPage {

    private final Logger logger = LoggerFactory.getLogger(ExportPage.class);

    private VerticalLayout donePanel = new VerticalLayout();

    // TODO i18n
    private static final String TRANSPORT_ERROR = "There was a problem communicating with the server. " +
            "Please check that the server name is valid and the server is accessible.";

    private SyncRunner runner;
    private SyncResult<TaskSaveResult, TaskErrors<ConnectorError<Throwable>>> result;
    private final String sourceConnectorId;
    private final String destinationConnectorId;

    private Mappings sourceMappings;

    public ExportPage(Connector connectorFrom, String sourceConnectorId,
                      Connector connectorTo, String destinationConnectorId,
                      TAFile taFile,
                      Mappings sourceMappings,
                      Mappings destinationMappings) {
        super(sourceConnectorId, connectorFrom, connectorTo,
                destinationConnectorId, taFile, sourceMappings,
                destinationMappings);
        this.sourceConnectorId = sourceConnectorId;
        this.destinationConnectorId = destinationConnectorId;
        this.sourceMappings = sourceMappings;
    }

    @Override
    public String getPageGoogleAnalyticsID() {
        return "export_confirmation";
    }

    @Override
    protected void loadData() {
        runner = new SyncRunner(services.getLicenseManager());
        runner.setConnectorFrom(connectorFrom, sourceConnectorId, sourceMappings);
        try {
            this.loadedTasks = runner.load(ProgressMonitorUtils.getDummyMonitor());
        } catch (CommunicationException e) {
            String message = getErrorMessageForException(e);
            showErrorMessageOnPage(message);
            logger.error("transport error: " + message, e);
        } catch (ConnectorException e) {
            showErrorMessageOnPage(ExceptionFormatter.format(e));
            logger.error(e.getMessage(), e);
        } catch (RuntimeException e) {
            showErrorMessageOnPage("Internal error: " + e.getMessage());
            logger.error(e.getMessage(), e);
        }
    }

    private String getErrorMessageForException(CommunicationException e) {
        if (EditorUtil.getRoot(e) instanceof UnknownHostException) {
            return "Unknown host";
        } else {
            return TRANSPORT_ERROR;
        }
    }

    private void showErrorMessageOnPage(String errorMessage) {
        Label errorLabel = new Label(errorMessage);
        errorLabel.addStyleName("errorMessage");
        mainPanel.addComponent(errorLabel);
    }

    @Override
    protected String getInitialText() {
        return "Will load data from " + connectorFrom.getConfig().getSourceLocation() + " (" + connectorFrom.getConfig().getLabel() + ")";
    }

    @Override
    protected String getNoDataLoadedText() {
        return "No data was loaded using the given criteria.";
    }

    // TODO Alexey: move this "done" thing into a separate Page class
    @Override
    protected VerticalLayout getDoneInfoPanel() {
        donePanel.setWidth("600px");
        donePanel.setStyleName("export-panel");

        addDateTimeInfo();
        addFromToPanel();
        final TaskSaveResult saveResult = result.getResult();
        if (saveResult != null) {
            addDownloadButtonIfServerMode(saveResult.getTargetFileAbsolutePath());
            addExportNumbersStats(saveResult);
            addFileInfoIfNeeded(saveResult);
        }

        if (result.getErrors().hasErrors()) {
            addErrors(result.getErrors());
        }
        return donePanel;
    }

    private void addDateTimeInfo() {
        String time = new SimpleDateFormat("MMMM dd, yyyy  HH:mm").format(Calendar.getInstance().getTime());
        // TODO externalize, i18n
        Label label = new Label("<strong>Export completed on</strong> <em>" + time + "</em>");
        label.setContentMode(Label.CONTENT_XHTML);

        donePanel.addComponent(label);
    }

    private void addFromToPanel() {
        donePanel.addComponent(createdExportResultLabel("From", connectorFrom.getConfig().getSourceLocation()));
    }

    private void addExportNumbersStats(TaskSaveResult result) {
        donePanel.addComponent(createdExportResultLabel("Created tasks", String.valueOf(result.getCreatedTasksNumber())));
        donePanel.addComponent(createdExportResultLabel("Updated tasks", String.valueOf(result.getUpdatedTasksNumber()) + "<br/><br/>"));
    }

    private HorizontalLayout createdExportResultLabel(String labelName, String labelValue) {
        Label lName = new Label("<strong>" + labelName + ":</strong>");
        lName.setContentMode(Label.CONTENT_XHTML);
        lName.setWidth("98px");

        Label lValue = new Label("<em>" + labelValue + "</em>");
        lValue.setContentMode(Label.CONTENT_XHTML);

        HorizontalLayout hl = new HorizontalLayout();
        hl.addComponent(lName);
        hl.addComponent(lValue);
        hl.addStyleName("export-result");

        return hl;
    }

    private void addErrors(TaskErrors<ConnectorError<Throwable>> result) {
        donePanel.addComponent(new Label("There were some problems during export:"));
        String errorText = "";
        for (ConnectorError<Throwable> e : result.getGeneralErrors()) {
            errorText += quot(decodeException(e)) + "<br/>";
        }
        for (TaskError<ConnectorError<Throwable>> e : result.getErrors()) {
            errorText += quot(getMessageForTask(e)) + "<br/>";
        }
        Label errorTextLabel = new Label(errorText);
        errorTextLabel.addStyleName("errorMessage");
        errorTextLabel.setContentMode(Label.CONTENT_XHTML);
        donePanel.addComponent(errorTextLabel);
    }

    private static String quot(String str) {
        return str.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private void addFileInfoIfNeeded(TaskSaveResult result) {
        if (result.getTargetFileAbsolutePath() != null && (services.getSettingsManager().isTAWorkingOnLocalMachine())) {
            Label label = new Label("<strong>Path to export file:</strong> <em>" + result.getTargetFileAbsolutePath() + "</em>");
            label.setContentMode(Label.CONTENT_XHTML);

            donePanel.addComponent(label);
        }
    }

    private void addDownloadButtonIfServerMode(String targetFileAbsolutePath) {
        if (!services.getSettingsManager().isTAWorkingOnLocalMachine() && !Strings.isNullOrEmpty(targetFileAbsolutePath)) {
            addDownloadButton(targetFileAbsolutePath);
        }
    }

    private void addDownloadButton(final String targetFileAbsolutePath) {
        Button downloadButton = new Button("Download");
        downloadButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                File file = new File(targetFileAbsolutePath);
                Application application = navigator.getApplication();
                FileDownloadResource resource = new FileDownloadResource(file, application);
                application.getMainWindow().open(resource);
            }
        });
        donePanel.addComponent(downloadButton);
    }

    private String getMessageForTask(TaskError<ConnectorError<Throwable>> e) {
        return "Task " + e.getTask().getId() + " (\"" + e.getTask().getSummary() + "\"): " + decodeException(e.getErrors());
    }

    private String decodeException(ConnectorError<Throwable> e) {
        final String connectorID = e.getConnectorId();

        final PluginEditorFactory factory = services.getEditorManager()
                .getEditorFactory(connectorID);

        String errorText = factory.formatError(e.getError());
        if (errorText == null) {
            errorText = ExceptionFormatter.format(e.getError());
        }

        return "Connector " + connectorID + " error : " + errorText;
    }

    @Override
    protected void saveData(List<GTask> tasks, Mappings sourceMappings, Mappings destinationMappings) throws ConnectorException {
        saveProgress.setValue(0);
        MonitorWrapper wrapper = new MonitorWrapper(saveProgress);
        runner.setDestination(connectorTo, destinationConnectorId,
                destinationMappings);
        runner.setTasks(tasks);
        try {
            result = runner.save(wrapper);
        } catch (ConnectorException e) {
            showErrorMessageOnPage(ExceptionFormatter.format(e));
            logger.error(e.getMessage(), e);
        }
    }

}
