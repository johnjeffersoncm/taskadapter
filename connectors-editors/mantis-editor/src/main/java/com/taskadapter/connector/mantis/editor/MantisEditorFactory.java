package com.taskadapter.connector.mantis.editor;

import com.google.common.base.Strings;
import com.taskadapter.connector.ValidationErrorBuilder;
import com.taskadapter.connector.definition.FieldMapping;
import com.taskadapter.connector.definition.WebConnectorSetup;
import com.taskadapter.connector.definition.exceptions.BadConfigException;
import com.taskadapter.connector.definition.exceptions.ProjectNotSetException;
import com.taskadapter.connector.definition.exceptions.ServerURLNotSetException;
import com.taskadapter.connector.mantis.MantisConfig;
import com.taskadapter.connector.mantis.MantisConnector;
import com.taskadapter.web.ConnectorSetupPanel;
import com.taskadapter.web.DroppingNotSupportedException;
import com.taskadapter.web.PluginEditorFactory;
import com.taskadapter.web.configeditor.ProjectPanel;
import com.taskadapter.web.configeditor.server.ServerPanelFactory;
import com.taskadapter.web.data.Messages;
import com.taskadapter.web.service.Sandbox;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.VerticalLayout;
import scala.Option;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.List;

import static com.vaadin.server.Sizeable.Unit.PIXELS;

public class MantisEditorFactory implements PluginEditorFactory<MantisConfig, WebConnectorSetup> {
    private static final String BUNDLE_NAME = "com.taskadapter.connector.mantis.editor.messages";
    private static final Messages MESSAGES = new Messages(BUNDLE_NAME);

    @Override
    public boolean isWebConnector() {
        return true;
    }

    @Override
    public ConnectorSetupPanel getEditSetupPanel(Sandbox sandbox, WebConnectorSetup setup) {
        return ServerPanelFactory.withLoginAndPassword(MantisConnector.ID(), MantisConnector.ID(), setup);
    }

    @Override
    public WebConnectorSetup createDefaultSetup(Sandbox sandbox) {
        return new WebConnectorSetup(MantisConnector.ID(), Option.empty(), "My MantisBT", "http://",
                "", "", false, "");
    }

    @Override
    public String formatError(Throwable e) {
        if (e instanceof ProjectNotSetException) {
            return MESSAGES.get("error.projectNotSet");
        }
        if (e instanceof ServerURLNotSetException) {
            return MESSAGES.get("error.serverUrlNotSet");
        }
        if (e instanceof QueryParametersNotSetException) {
            return MESSAGES.get("error.queryParametersNotSet");
        }
        if (e instanceof UnsupportedOperationException) {
            final UnsupportedOperationException uop = (UnsupportedOperationException) e;
            if ("saveRelations".equals(uop.getMessage())) {
                return MESSAGES.get("error.unsupported.relations");
            }
        }
        return null;
    }

    @Override
    public ComponentContainer getMiniPanelContents(Sandbox sandbox, MantisConfig config, WebConnectorSetup setup) {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth(380, PIXELS);

        layout.addComponent(new ProjectPanel(new MethodProperty<>(config, "projectKey"),
                Option.apply(new MethodProperty<>(config, "queryId")),
                Option.empty(),
                new MantisProjectsListLoader(setup),
                null,
                new MantisQueryListLoader(config, setup),
                this));
        layout.addComponent(new OtherMantisFieldsPanel(config));

        return layout;
    }

    @Override
    public Seq<BadConfigException> validateForSave(MantisConfig config, WebConnectorSetup setup, Seq<FieldMapping<?>> fieldMappings) {
        List<BadConfigException> list = new ArrayList<>();
        if (Strings.isNullOrEmpty(setup.host())) {
            list.add(new ServerURLNotSetException());
        }

        if (config.getProjectKey() == null || config.getProjectKey().isEmpty()) {
            list.add(new ProjectNotSetException());
        }
        return JavaConverters.asScalaBuffer(list);
    }

    @Override
    public Seq<BadConfigException> validateForLoad(MantisConfig config, WebConnectorSetup setup) {
        ValidationErrorBuilder builder = new ValidationErrorBuilder();
        if (Strings.isNullOrEmpty(setup.host())) {
            builder.error(new ServerURLNotSetException());
        }
        if ((config.getProjectKey() == null || config.getProjectKey().isEmpty()) &&
                (config.getQueryId() == null)) {
            builder.error(new ProjectNotSetException());
        }
        return builder.build();
    }

    @Override
    public String describeSourceLocation(MantisConfig config, WebConnectorSetup setup) {
        return setup.host();
    }

    @Override
    public String describeDestinationLocation(MantisConfig config, WebConnectorSetup setup) {
        return describeSourceLocation(config, setup);
    }

    @Override
    public Messages fieldNames() {
        return MESSAGES;
    }

    @Override
    public void validateForDropInLoad(MantisConfig config) throws DroppingNotSupportedException {
        throw DroppingNotSupportedException.INSTANCE;
    }

}
