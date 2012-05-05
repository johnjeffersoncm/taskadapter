package com.taskadapter.webui;

import com.taskadapter.license.LicenseChangeListener;
import com.taskadapter.license.LicenseManager;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;

/**
 * @author Alexey Skorokhodov
 */
public class Header extends HorizontalLayout implements LicenseChangeListener {
    private HorizontalLayout internalLayout = new HorizontalLayout();
    private VerticalLayout trialLayout = new VerticalLayout();
    private Navigator navigator;

    public Header(Navigator navigator) {
        this.navigator = navigator;
        buildMainLayout();
        checkLicense();
        LicenseManager.addLicenseChangeListener(this);
    }

    private void buildMainLayout() {
        internalLayout.setWidth(800, UNITS_PIXELS);
        addComponent(internalLayout);
        setComponentAlignment(internalLayout, Alignment.MIDDLE_CENTER);


        setSpacing(true);
        addStyleName("header-panel");

        addLogo();
        addMenuItems();
        addTrialSection();
    }

    private void addTrialSection() {
        trialLayout.setSizeFull();
        trialLayout.addStyleName("trial-mode-area");
        Label trialLabel = new Label("TRIAL MODE");
        trialLabel.setSizeUndefined();
        trialLabel.addStyleName("trial-mode-label");
        trialLayout.addComponent(trialLabel);
        trialLayout.setComponentAlignment(trialLabel, Alignment.MIDDLE_CENTER);

        Link buyLink = new Link("Buy now", new ExternalResource("http://www.taskadapter.com/buy"));
        buyLink.addStyleName("trial-mode-link");
        buyLink.setTargetName("_blank");

        trialLayout.addComponent(buyLink);
        trialLayout.setComponentAlignment(buyLink, Alignment.MIDDLE_CENTER);

        internalLayout.addComponent(trialLayout);
        internalLayout.setExpandRatio(trialLayout, 1f);
        trialLayout.setVisible(false);
    }

    private void addLogo() {
        Button logo = createButtonLink("Task Adapter", Navigator.HOME, "logo");
        internalLayout.addComponent(logo);
        internalLayout.setExpandRatio(logo, 2f);
    }

    private void addMenuItems() {
        HorizontalLayout menu = new HorizontalLayout();
        menu.setSpacing(true);
        menu.addComponent(createButtonLink("Configure", Navigator.CONFIGURE_SYSTEM_PAGE, "menu"));
        menu.addComponent(createButtonLink("Support", Navigator.FEEDBACK_PAGE, "menu"));
        internalLayout.addComponent(menu);
        internalLayout.setExpandRatio(menu, 1f);
        internalLayout.setComponentAlignment(menu, Alignment.MIDDLE_CENTER);
    }

    private Button createButtonLink(String caption, final String pageId, String additionalStyle) {
        Button button = new Button(caption);
        button.setStyleName(BaseTheme.BUTTON_LINK);
        button.addStyleName(additionalStyle);
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigator.show(pageId);
            }
        });
        return button;
    }

    private void checkLicense() {
        if (!LicenseManager.isTaskAdapterLicenseOK()) {
            trialLayout.setVisible(true);
        } else {
            trialLayout.setVisible(false);
        }
    }

    @Override
    public void licenseInfoUpdated() {
        checkLicense();
    }
}
