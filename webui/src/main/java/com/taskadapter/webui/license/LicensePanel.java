package com.taskadapter.webui.license;

import com.taskadapter.license.License;
import com.taskadapter.license.LicenseException;
import com.taskadapter.license.LicenseExpiredException;
import com.taskadapter.webui.TALog;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;

public final class LicensePanel {

    private Logger log = TALog.log();

    private final LicenseFacade licenseManager;
    private final VerticalLayout contentPane;
    private final Panel ui;

    private LicensePanel(LicenseFacade licenseManager) {
        this.licenseManager = licenseManager;

        ui = new Panel("License Information");

        contentPane = new VerticalLayout();
        contentPane.setMargin(true);
        ui.setContent(contentPane);

        showLicense(licenseManager.getLicense().get());
    }

    /**
     * Applies a new license.
     * 
     * @param licenseText
     *            license text.
     */
    private void applyNewLicense(String licenseText) {
        try {
            licenseManager.install(licenseText);

            final License newLicense = licenseManager.getLicense().get();
            showLicense(newLicense);
            if (newLicense != null) {
                String text = "Successfully registered to: " + newLicense.getCustomerName();
                log.info(text);
                Notification.show(text);
            }
        } catch (LicenseExpiredException e) {
            log.error("Cannot install license: it is expired. License text: " + licenseText);
            Notification.show("License not accepted", e.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
        } catch (LicenseException e) {
            log.error("Cannot install license because of exception. "
                    + e.toString() + "\nLicense text:\n" + licenseText);
            Notification.show("License not accepted", "The license is invalid",
                    Notification.Type.ERROR_MESSAGE);
        }
    }

    /**
     * Uninstalls a license.
     */
    private void uninstallLicense() {
        licenseManager.uninstall();
        String string = "Removed license info";
        log.info(string);
        Notification.show(string);
    }

    /**
     * Shows a license.
     * 
     * @param license
     *            license to show.
     */
    private void showLicense(License license) {
        contentPane.removeAllComponents();
        if (license != null) {
            contentPane.addComponent(LicensePanels.renderLicense(license,
                    this::uninstallLicense));
        } else {
            contentPane.addComponent(LicensePanels
                    .createLicenseInputPanel(this::applyNewLicense));
        }
    }

    /**
     * Renders a new license panel for the model.
     * 
     * @param model
     *            model to render a panel for.
     * @return license UI.
     */
    public static Component renderLicensePanel(LicenseFacade model) {
        return new LicensePanel(model).ui;
    }
}
