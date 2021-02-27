package com.taskadapter.connector.basecamp.classic.editor;

import com.google.common.base.Strings;
import com.taskadapter.connector.basecamp.classic.BasecampClassicConfig;
import com.taskadapter.connector.basecamp.classic.BasecampClassicConnector;
import com.taskadapter.connector.basecamp.classic.BasecampConfigValidator;
import com.taskadapter.connector.basecamp.classic.BasecampUtils;
import com.taskadapter.connector.basecamp.classic.beans.BasecampProject;
import com.taskadapter.connector.basecamp.classic.beans.TodoList;
import com.taskadapter.connector.basecamp.classic.transport.BaseCommunicator;
import com.taskadapter.connector.basecamp.classic.transport.ObjectAPIFactory;
import com.taskadapter.connector.definition.FieldMapping;
import com.taskadapter.connector.definition.WebConnectorSetup;
import com.taskadapter.connector.definition.exceptions.BadConfigException;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.connector.definition.exceptions.ProjectNotSetException;
import com.taskadapter.model.NamedKeyedObject;
import com.taskadapter.model.NamedKeyedObjectImpl;
import com.taskadapter.web.ConnectorSetupPanel;
import com.taskadapter.web.DroppingNotSupportedException;
import com.taskadapter.web.PluginEditorFactory;
import com.taskadapter.web.callbacks.DataProvider;
import com.taskadapter.web.configeditor.EditorUtil;
import com.taskadapter.web.configeditor.server.ServerPanelWithPasswordAndAPIKey;
import com.taskadapter.web.data.Messages;
import com.taskadapter.web.service.Sandbox;
import com.taskadapter.web.uiapi.DefaultSavableComponent;
import com.taskadapter.web.uiapi.SavableComponent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import scala.collection.JavaConverters;

import java.util.ArrayList;
import java.util.List;

public class BasecampClassicEditorFactory implements PluginEditorFactory<BasecampClassicConfig, WebConnectorSetup> {
    private static String BUNDLE_NAME = "com.taskadapter.connector.basecamp.classic.editor.messages";
    private static Messages MESSAGES = new Messages(BUNDLE_NAME);
    private static ObjectAPIFactory factory = new ObjectAPIFactory(new BaseCommunicator());
    private static BasecampErrorFormatter formatter = new BasecampErrorFormatter();

    @Override
    public SavableComponent getMiniPanelContents(Sandbox sandbox, BasecampClassicConfig config, WebConnectorSetup setup) {
        Binder<BasecampClassicConfig> binder = new Binder<>(BasecampClassicConfig.class);

        Component projectPanel = createProjectPanel(binder, config, setup);
        binder.readBean(config);
        return new DefaultSavableComponent(projectPanel, () -> {
            try {
                binder.writeBean(config);
                return true;
            } catch (ValidationException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    private Component createProjectPanel(Binder<BasecampClassicConfig> binder, BasecampClassicConfig config,
                                         WebConnectorSetup setup) {

        Label projectLabel = new Label("Project key");
        TextField projectField = EditorUtil.textInput(binder, "projectKey");
        Button projectInfoButton = EditorUtil.createButton("Info", "View the project info",
                event -> ShowInfoElement.loadProject(config, setup, formatter, factory)
        );
        DataProvider<List<? extends NamedKeyedObject>> projectProvider = new DataProvider<List<? extends NamedKeyedObject>>() {
            @Override
            public List<? extends NamedKeyedObject> loadData() throws ConnectorException {
                List<BasecampProject> basecampProjects = BasecampUtils.loadProjects(factory, setup);
                List<NamedKeyedObject> objects = new ArrayList<>();
                for (BasecampProject project : basecampProjects) {
                    objects.add(new NamedKeyedObjectImpl(project.getKey(), project.getName()));
                }
                return objects;
            }
        };

        Button showProjectsButton = EditorUtil.createLookupButton(
                "...",
                "Show list of available projects on the server.",
                "Select a project",
                projectProvider,
                formatter,
                namedKeyedObject -> {
                    projectField.setValue(namedKeyedObject.getKey());
                    return null;
                }
        );

        Label todoListKeyLabel = new Label("Todo list key");
        TextField todoListKeyField = EditorUtil.textInput(binder, "todoKey");
        Button todoListInfoButton = EditorUtil.createButton("Info", "View the todo list info",
                event -> ShowInfoElement.showTodoListInfo(config, setup, formatter, factory)
        );

        DataProvider<List<? extends NamedKeyedObject>> todoListsProvider = () -> {
            List<TodoList> todoLists = BasecampUtils.loadTodoLists(factory, config, setup);
            List<NamedKeyedObject> objects = new ArrayList<>();
            for (TodoList todoList : todoLists) {
                objects.add(new NamedKeyedObjectImpl(todoList.getKey(), todoList.getName()));
            }
            return objects;
        };

        Button showTodoListsButton = EditorUtil.createLookupButton(
                "...",
                "Show Todo Lists",
                "Select a Todo list",
                todoListsProvider,
                formatter,
                namedKeyedObject -> {
                    todoListKeyField.setValue(namedKeyedObject.getKey());
                    return null;
                }
        );

        FormLayout layout = new FormLayout();
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("20em", 1),
                new FormLayout.ResponsiveStep("60em", 2),
                new FormLayout.ResponsiveStep("20em", 3),
                new FormLayout.ResponsiveStep("20em", 4));

        layout.add(projectLabel, projectField, projectInfoButton, showProjectsButton);

        layout.add(todoListKeyLabel, todoListKeyField, todoListInfoButton, showTodoListsButton);
        return layout;
    }

    @Override
    public boolean isWebConnector() {
        return false;
    }

    @Override
    public ConnectorSetupPanel getEditSetupPanel(Sandbox sandbox, WebConnectorSetup setup) {
        return new ServerPanelWithPasswordAndAPIKey(BasecampClassicConnector.ID(), BasecampClassicConnector.ID(), setup);
    }

    @Override
    public WebConnectorSetup createDefaultSetup(Sandbox sandbox) {
        return WebConnectorSetup.apply(BasecampClassicConnector.ID(),
                "My Basecamp Classic", "https://-my-project-name-here-.basecamphq.com",
                "", "", true, "");
    }

    @Override
    public List<BadConfigException> validateForSave(BasecampClassicConfig config, WebConnectorSetup setup, List<FieldMapping<?>> fieldMappings) {
        List<BadConfigException> list = new ArrayList<>();

        if (Strings.isNullOrEmpty(config.getProjectKey())) {
            list.add(new ProjectNotSetException());
        }
        return list;
    }

    @Override
    public List<BadConfigException> validateForLoad(BasecampClassicConfig config, WebConnectorSetup setup) {
        return JavaConverters.seqAsJavaList(BasecampConfigValidator.validateTodoListNoException(config));
    }

    @Override
    public void validateForDropInLoad(BasecampClassicConfig config) throws DroppingNotSupportedException {
        throw DroppingNotSupportedException.INSTANCE;
    }

    @Override
    public String describeSourceLocation(BasecampClassicConfig config, WebConnectorSetup setup) {
        return setup.getHost();
    }

    @Override
    public String describeDestinationLocation(BasecampClassicConfig config, WebConnectorSetup setup) {
        return describeSourceLocation(config, setup);
    }

    @Override
    public Messages fieldNames() {
        return MESSAGES;
    }

    @Override
    public String formatError(Throwable e) {
        return formatter.formatError(e);
    }

}
