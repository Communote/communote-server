package com.communote.plugins.mq.provider.activemq.monitor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.BundleContext;

import com.communote.plugins.core.views.AdministrationViewController;
import com.communote.plugins.core.views.ViewControllerException;
import com.communote.plugins.core.views.annotations.Page;
import com.communote.plugins.core.views.annotations.UrlMapping;
import com.communote.plugins.mq.provider.activemq.MonitorableBroker;
import com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO;

/**
 * MQ Monitor Controller
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Provides
@Instantiate
@Page(menu = "extensions", submenu = "mq.monitor", menuMessageKey = "administration.title.submenu.mq.monitor")
@UrlMapping(value = "/*/admin/application/mq/monitor")
public class MQBrokerMonitorController extends AdministrationViewController {

    /**
     * broker active model parameter
     */
    private static final String P_BROKER_ACTIVE = "broker_active";

    /**
     * broker name model parameter
     */
    private static final String P_BROKER_NAME = "broker_name";

    /**
     * broker users model parameter
     */
    private static final String P_BROKER_USERS = "broker_users";
    /**
     * broker queues model parameter
     */
    private static final String P_BROKER_QUEUES = "broker_queues";

    /**
     * broker jmx model parameter
     */
    private static final String P_JMX_ENABLED = "broker_jmx";

    private static final String P_HAS_ERRORS = "has_errors";

    private static final String P_ERROR_MSG = "error_message";

    /** The broker. */
    @Requires
    private MonitorableBroker broker;

    @Requires(proxy = false)
    private MQSettingsDAO settingsDao;

    /**
     * @param bundleContext
     *            OSGi bundle context
     */
    public MQBrokerMonitorController(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> monitorModel) throws ViewControllerException {
        try {
            boolean isActive = broker.isActive();
            monitorModel.put(P_BROKER_ACTIVE, isActive);
            monitorModel.put(P_JMX_ENABLED, settingsDao.isJmxMonitoringEnabled());
            if (isActive) {
                monitorModel.put(P_BROKER_QUEUES, broker.getBrokerQueues());
                monitorModel.put(P_BROKER_NAME, broker.getBrokerName());
                monitorModel.put(P_BROKER_USERS,
                        broker.getMessageHandlerConsumers());
            }
        } catch (Exception e) {
            monitorModel.put(P_HAS_ERRORS, Boolean.TRUE);
            monitorModel.put(P_ERROR_MSG, e.getMessage());
        }
    }

    @Override
    public String getContentTemplate() {
        return "/vm/mq-monitor-content.html.vm";
    }

}
