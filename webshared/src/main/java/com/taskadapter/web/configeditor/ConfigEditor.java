package com.taskadapter.web.configeditor;

import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.definition.ValidationException;
import com.taskadapter.web.WindowProvider;
import com.taskadapter.web.service.Services;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

public abstract class ConfigEditor extends VerticalLayout implements WindowProvider {
    private List<Validatable> toValidate = new ArrayList<Validatable>();

    // TODO the parent editor class must save / load data itself instead of letting the children do this

    private final ConfigPanelContainer panelContainer = new ConfigPanelContainer();

    protected ConnectorConfig config;
    protected Services services;

    ConfigEditor(ConnectorConfig config, Services services) {
        this.config = config;
        this.services = services;
        setImmediate(false);
        setMargin(true);
        setSpacing(true);

        setWidth("840px");
    }

    protected void addPanelToLayout(Layout component, Panel panel) {
        //if layout supports Validatable interface add it to validation list
        if (panel instanceof Validatable) {
            toValidate.add((Validatable) panel);
        }

        component.addComponent(panel);
        panelContainer.add(panel);
    }

    public void validateAll() throws ValidationException {
        for (Validatable v : toValidate) {
            v.validate();
        }
        validate();
    }

    /**
     * the default implementation does nothing.
     */
    public void validate() throws ValidationException {
    }

    public ConnectorConfig getConfig() {
        return config;
    }

    public <T> T getPanel(Class<T> clazz) {
        return panelContainer.get(clazz);
    }
}
