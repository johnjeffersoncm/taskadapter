package com.taskadapter.webui.pages;

import static com.taskadapter.license.LicenseManager.TRIAL_MESSAGE;
import static com.taskadapter.webui.Page.message;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taskadapter.connector.definition.exceptions.CommunicationException;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.model.GTask;
import com.taskadapter.web.uiapi.UISyncConfig;
import com.taskadapter.webui.ConfigOperations;
import com.taskadapter.webui.MonitorWrapper;
import com.taskadapter.webui.export.ConfirmExportFragment;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;

public final class UpdateFilePage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportPage.class);

    /**
     * Config operations.
     */
    private final ConfigOperations configOps;

    /**
     * Sync config.
     */
    private final UISyncConfig config;

    /**
     * Maximal number of transferred tasks.
     */
    private final int taskLimit;

    /**
     * "Process complete" handler.
     */
    private final Runnable onDone;

    private final VerticalLayout ui;
    private final Label errorMessage;
    private final VerticalLayout content;

    private UpdateFilePage(ConfigOperations configOps, UISyncConfig config,
            int taskLimit, Runnable onDone) {
        this.config = config;
        this.onDone = onDone;
        this.taskLimit = taskLimit;
        this.configOps = configOps;

        ui = new VerticalLayout();
        errorMessage = new Label("");
        errorMessage.addStyleName("errorMessage");
        errorMessage.setVisible(false);
        errorMessage.setWidth(500, Unit.PIXELS);
        ui.addComponent(errorMessage);

        content = new VerticalLayout();
        ui.addComponent(content);
        startLoading();
    }

    private void startLoading() {
        setContent(SyncActionComponents.renderLoadIndicator(config
                .getConnector1()));

        if (taskLimit < Integer.MAX_VALUE)
            LOGGER.info(TRIAL_MESSAGE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<GTask> tasks = config.loadTasksForUpdate();
                    if (tasks.isEmpty())
                        showNoDataLoaded();
                    else
                        showConfirmation(tasks);
                } catch (CommunicationException e) {
                    final String message = config.getConnector1()
                            .decodeException(e);
                    showLoadErrorMessage(message);
                    LOGGER.error("transport error: " + message, e);
                } catch (ConnectorException e) {
                    showLoadErrorMessage(config.getConnector1()
                            .decodeException(e));
                    LOGGER.error(e.getMessage(), e);
                } catch (RuntimeException e) {
                    showLoadErrorMessage("Internal error: " + e.getMessage());
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }).start();
    }

    /**
     * Shows tasks confirmation dialog.
     * 
     * @param tasks
     *            tasks to confirm.
     */
    private void showConfirmation(List<GTask> tasks) {
        final Component component = ConfirmExportFragment.render(configOps,
                config, tasks, new ConfirmExportFragment.Callback() {
                    @Override
                    public void onTasks(List<GTask> selectedTasks) {
                        performExport(selectedTasks);
                    }

                    @Override
                    public void onCancel() {
                        onDone.run();
                    }
                });

        VaadinSession.getCurrent().lock();
        try {
            setContent(component);
            content.setExpandRatio(component, 1f);
        } finally {
            VaadinSession.getCurrent().unlock();
        }
    }

    /**
     * Perofrms an export.
     * 
     * @param selectedTasks
     *            list of selected tasks.
     */
    private void performExport(final List<GTask> selectedTasks) {
        if (selectedTasks.isEmpty()) {
            Notification.show(message("action.pleaseSelectTasks"));
            return;
        }

        final ProgressIndicator saveProgress = SyncActionComponents
                .renderSaveIndicator(config.getConnector2());
        saveProgress.setValue(0f);
        setContent(saveProgress);

        final MonitorWrapper wrapper = new MonitorWrapper(saveProgress);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    showExportResult(config.updateTasks(selectedTasks, wrapper));
                } catch (CommunicationException e) {
                    final String message = config.getConnector1()
                            .decodeException(e);
                    showLoadErrorMessage(message);
                    LOGGER.error("transport error: " + message, e);
                } catch (ConnectorException e) {
                    showLoadErrorMessage(config.getConnector1()
                            .decodeException(e));
                    LOGGER.error(e.getMessage(), e);
                } catch (RuntimeException e) {
                    showLoadErrorMessage("Internal error: " + e.getMessage());
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }).start();
    }

    /**
     * Shows task export result.
     */
    private void showExportResult(int updatedTasks) {
        final VerticalLayout ui = new VerticalLayout();

        final String text = message("updatePage.result",
                updatedTasks+"", config.getConnector2().getDestinationLocation(),
                config.getConnector1().getSourceLocation());
        ui.addComponent(new Label(text));

        final Button button = new Button(message("action.backToHomePage"));
        button.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                onDone.run();
            }
        });
        ui.addComponent(button);

        VaadinSession.getCurrent().lock();
        try {
            setContent(ui);
        } finally {
            VaadinSession.getCurrent().unlock();
        }
    }

    /**
     * Shows "no data loaded" content.
     */
    private void showNoDataLoaded() {
        final VerticalLayout res = new VerticalLayout();
        final Label label = new Label(message("updatePage.noDataWasLoaded"));
        label.setWidth(800, Unit.PIXELS);

        final Button backButton = new Button(message("button.back"));
        backButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                onDone.run();
            }
        });
        res.addComponent(label);
        res.addComponent(backButton);

        setContent(res);
    }

    /**
     * Shows load error message.
     * 
     * @param message
     *            message to show.
     */
    private void showLoadErrorMessage(String message) {
        VaadinSession.getCurrent().lock();
        try {
            showErrorMessage(message);
            showNoDataLoaded();
        } finally {
            VaadinSession.getCurrent().unlock();
        }
    }

    /**
     * Shows or hides an error message.
     * 
     * @param message
     *            error message. If null, errors are hidden.
     */
    private void showErrorMessage(String message) {
        final boolean haveMessage = message != null;
        errorMessage.setValue(haveMessage ? message : "");
        errorMessage.setVisible(haveMessage);
    }

    /**
     * Sets new page content.
     * 
     * @param comp
     *            page content.
     */
    private void setContent(Component comp) {
        content.removeAllComponents();
        content.addComponent(comp);
    }

    /**
     * Renders an "update file" page.
     * 
     * @param configOps
     *            config operations.
     * @param config
     *            current config.
     * @param maxTasks
     *            maximal number of tasks.
     * @param onExit
     *            exit handler.
     * @return operation UI.
     */
    public static Component render(ConfigOperations configOps,
            UISyncConfig config, int maxTasks, Runnable onExit) {
        return new UpdateFilePage(configOps, config, maxTasks, onExit).ui;
    }

}
