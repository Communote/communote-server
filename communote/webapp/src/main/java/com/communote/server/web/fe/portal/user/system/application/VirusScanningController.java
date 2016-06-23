package com.communote.server.web.fe.portal.user.system.application;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.common.virusscan.exception.InitializeException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.type.ApplicationPropertyVirusScanning;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.core.service.BuiltInServiceNames;
import com.communote.server.core.service.RestartServiceEvent;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class VirusScanningController extends BaseFormController {

    /** Logger. */
    private final static Logger LOG = Logger.getLogger(VirusScanningController.class);

    /** Action to save form input. */
    public static final String ACTION_SAVE = "save";
    /** Action to test connection. */
    public static final String ACTION_TEST = "test-connection";
    /** Action to start the service. */
    public static final String ACTION_START_SERVICE = "start-service";
    /** Action to stop the service. */
    public static final String ACTION_STOP_SERVICE = "stop-service";

    /** the host name */
    public static final String DEFAULT_CLAMAV_HOST = "localhost";
    /** the port number */
    public static final String DEFAULT_CLAMAV_PORT = "3310";
    /** directory for temporary files */
    public static final String DEFAULT_CLAMAV_TEMP_DIR = "";
    /** the specified connection timeout, in seconds */
    public static final String DEFAULT_CLAMAV_CONNECTION_TIMEOUT = "90";

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        VirusScanningForm form = new VirusScanningForm();

        String scannerType = ApplicationPropertyVirusScanning.VIRUS_SCANNER_FACTORY_TYPE
                .getValue(SupportedVirusScannerTypes.CLAMAV.getScannerClassName());

        if (SupportedVirusScannerTypes.CMDLINE.getScannerClassName().equals(scannerType)) {
            form.setScannerType(SupportedVirusScannerTypes.CMDLINE);
        } else {
            form.setScannerType(SupportedVirusScannerTypes.CLAMAV);
        }

        // Command Line Scanner
        form.setCmdCommand(ApplicationPropertyVirusScanning.COMMAND_LINE_STRING.getValue());
        form.setCmdExitCode(ApplicationPropertyVirusScanning.COMMAND_LINE_EXIT_CODE.getValue());
        form.setCmdTempDir(ApplicationPropertyVirusScanning.COMMAND_LINE_TEMP_DIR.getValue());
        form.setCmdTempFilePrefix(ApplicationPropertyVirusScanning.COMMAND_LINE_TEMP_FILE_PREFIX
                .getValue());
        form.setCmdTempFileSuffix(ApplicationPropertyVirusScanning.COMMAND_LINE_TEMP_FILE_SUFFIX
                .getValue());
        form.setCmdProcessTimeout(ApplicationPropertyVirusScanning.COMMAND_LINE_PROCESS_TIMEOUT
                .getValue());

        // Clam AntiVirus
        form.setClamHost(ApplicationPropertyVirusScanning.CLAMAV_SCANNER_HOST
                .getValue(DEFAULT_CLAMAV_HOST));
        form.setClamPort(ApplicationPropertyVirusScanning.CLAMAV_SCANNER_PORT
                .getValue(DEFAULT_CLAMAV_PORT));
        form.setClamTempDir(ApplicationPropertyVirusScanning.CLAMAV_SCANNER_TEMP_DIR
                .getValue(DEFAULT_CLAMAV_TEMP_DIR));
        form.setClamConnectionTimeout(ApplicationPropertyVirusScanning.CLAMAV_SCANNER_CONNECTION_TIMEOUT
                .getValue(DEFAULT_CLAMAV_CONNECTION_TIMEOUT));

        // current status of the service
        form.setEnabled(Boolean.valueOf(ApplicationPropertyVirusScanning.ENABLED.getValue()));

        return form;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        VirusScanningForm form = (VirusScanningForm) command;

        if (ACTION_SAVE.equals(form.getAction())) {
            return saveSettings(request, form);
        } else if (ACTION_TEST.equals(form.getAction())) {
            // testConnection(request, form);
        } else if (ACTION_START_SERVICE.equals(form.getAction())) {
            handleServiceManagementTasks(true, request, form);
        } else if (ACTION_STOP_SERVICE.equals(form.getAction())) {
            handleServiceManagementTasks(false, request, form);
        }

        return new ModelAndView(getSuccessView(), "command", command);
    }

    /**
     * Handles the tasks to enable or disable the service for anti virus scanning.
     *
     * @param enableService
     *            {@code true} if the service should be enabled
     * @param request
     *            the current servlet request
     * @param form
     *            the form object with request parameters bound onto it
     * @throws Exception
     *             thrown in case of errors
     */
    private void handleServiceManagementTasks(boolean enableService, HttpServletRequest request,
            VirusScanningForm form) throws Exception {
        Map<ApplicationConfigurationPropertyConstant, String> settings = null;
        settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationPropertyVirusScanning.ENABLED, Boolean.toString(enableService));

        // first update the property and then run service tasks
        // (service depends on the enabled property)
        try {
            CommunoteRuntime.getInstance().getConfigurationManager()
                    .updateApplicationConfigurationProperties(settings);
        } catch (ConfigurationUpdateException e) {
            MessageHelper.saveErrorMessageFromKey(request, "client.system.settings.error");
            LOG.error("Running a service task aborted because an incorrect update of "
                    + "an application property occured. Caused by "
                    + ApplicationPropertyVirusScanning.ENABLED);
            return;
        }
        ServiceLocator.findService(EventDispatcher.class).fire(
                new RestartServiceEvent(BuiltInServiceNames.VIRUS_SCANNER));

        if (enableService) {
            try {
                ServiceLocator.instance().getVirusScanner();

                MessageHelper.saveMessageFromKey(request,
                        "client.system.application.virusscanning.service.start.success");
            } catch (InitializeException e) {
                MessageHelper.saveErrorMessageFromKey(request,
                        "client.system.application.virusscanning.service.start.error");
            }
        } else {
            if (ServiceLocator.instance().getVirusScanner() == null) {
                MessageHelper.saveMessageFromKey(request,
                        "client.system.application.virusscanning.service.stop.success");
            } else {
                MessageHelper.saveErrorMessageFromKey(request,
                        "client.system.application.virusscanning.service.stop.error");
            }
        }

        form.setEnabled(enableService);
    }

    /**
     * Saves the settings.
     *
     * @param request
     *            Request
     * @param form
     *            Form
     * @return MaV.
     * @throws Exception
     *             Exception.
     */
    private synchronized ModelAndView saveSettings(HttpServletRequest request,
            VirusScanningForm form) throws Exception {

        Map<ApplicationConfigurationPropertyConstant, String> settings = null;
        settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();

        settings.put(ApplicationPropertyVirusScanning.VIRUS_SCANNER_FACTORY_TYPE, form
                .getScannerType().getScannerClassName());

        if (SupportedVirusScannerTypes.CMDLINE.equals(form.getScannerType())) {
            settings.put(ApplicationPropertyVirusScanning.COMMAND_LINE_STRING, form.getCmdCommand());

            settings.put(ApplicationPropertyVirusScanning.COMMAND_LINE_EXIT_CODE,
                    form.getCmdExitCode());

            settings.put(ApplicationPropertyVirusScanning.COMMAND_LINE_TEMP_DIR,
                    form.getCmdTempDir());

            settings.put(ApplicationPropertyVirusScanning.COMMAND_LINE_TEMP_FILE_PREFIX,
                    form.getCmdTempFilePrefix());

            settings.put(ApplicationPropertyVirusScanning.COMMAND_LINE_TEMP_FILE_SUFFIX,
                    form.getCmdTempFileSuffix());

            settings.put(ApplicationPropertyVirusScanning.COMMAND_LINE_PROCESS_TIMEOUT,
                    form.getCmdProcessTimeout());
        }

        if (SupportedVirusScannerTypes.CLAMAV.equals(form.getScannerType())) {
            settings.put(ApplicationPropertyVirusScanning.CLAMAV_SCANNER_TEMP_DIR,
                    form.getClamTempDir());

            settings.put(ApplicationPropertyVirusScanning.CLAMAV_SCANNER_HOST, form.getClamHost());

            settings.put(ApplicationPropertyVirusScanning.CLAMAV_SCANNER_PORT, form.getClamPort());

            settings.put(ApplicationPropertyVirusScanning.CLAMAV_SCANNER_CONNECTION_TIMEOUT,
                    form.getClamConnectionTimeout());
        }

        try {
            CommunoteRuntime.getInstance().getConfigurationManager()
                    .updateApplicationConfigurationProperties(settings);
        } catch (ConfigurationUpdateException e) {
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.application.virusscanning.action.save.error");
            return new ModelAndView(getSuccessView(), "command", form);
        }
        MessageHelper.saveMessageFromKey(request,
                "client.system.application.virusscanning.action.save.success");
        return new ModelAndView(getSuccessView(), "command", formBackingObject(request));
    }
}
