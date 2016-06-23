package com.communote.plugins.activity.base.fe.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.BundleContext;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.common.util.Pair;
import com.communote.common.util.ParameterHelper;
import com.communote.plugins.activity.base.data.ActivityConfiguration;
import com.communote.plugins.activity.base.data.ActivityDefinition;
import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.plugins.activity.base.service.ActivityServiceException;
import com.communote.plugins.core.views.AdministrationViewController;
import com.communote.plugins.core.views.ViewControllerException;
import com.communote.plugins.core.views.annotations.Page;
import com.communote.plugins.core.views.annotations.UrlMapping;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.web.commons.MessageHelper;

/**
 * Controller for the administration configuration of the activities.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */

@Component
@Provides
@Instantiate(name = "ActivityAdminController")
@UrlMapping(value = "/*/admin/ui/activity")
@Page(menu = "extensions", submenu = "activity", menuMessageKey = "administration.title.submenu.activity",
        jsCategories = { "communote-core", "admin" }, cssCategories = { "admin" })
public class ActivityAdminController extends AdministrationViewController implements Controller {

    @Requires
    private ActivityService activityService;

    /**
     * @param bundleContext
     *            The current bundle context.
     */
    public ActivityAdminController(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws ViewControllerException {
        List<Pair<ActivityDefinition, ActivityConfiguration>> activities = null;
        try {
            activities = activityService
                    .getActivities(SessionHandler.instance().getCurrentLocale(
                            request));
            model.put("activities", activities);
            model.put("locale", SessionHandler.instance().getCurrentLocale(request));
        } catch (ActivityServiceException e) {
            throw new ViewControllerException(500, e.getMessage(), e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request,
            HttpServletResponse response, Map<String, Object> model)
            throws ViewControllerException {
        String action = request.getParameter("action");
        if ("activity-configuration-save".equals(action)) {
            Integer activityCount = Integer.valueOf(request.getParameter("activity-count"));
            List<ActivityConfiguration> configurations = new ArrayList<ActivityConfiguration>();
            try {
                List<Pair<ActivityDefinition, ActivityConfiguration>> activities = activityService
                        .getActivities(null);
                for (int n = 1; n <= activityCount; n++) {
                    for (Pair<ActivityDefinition, ActivityConfiguration> activity : activities) {
                        ActivityConfiguration configuration = activity.getRight();
                        if (configuration.getTemplateId().equals(request
                                .getParameter("activity-id-"
                                        + n))) {
                            if (!Boolean.parseBoolean(request
                                    .getParameter("activity-external-"
                                            + n))) {
                                configuration.setActive(BooleanUtils.toBoolean(request
                                        .getParameter("activityActivation-" + n)));
                            } else {
                                configuration.setActive(BooleanUtils.toBoolean(request
                                        .getParameter("_activityActivation-" + n)));
                            }
                            configuration.setDeletableByUser(BooleanUtils.toBoolean(request
                                    .getParameter("deletableByUser-" + n)));
                            configuration.setExpirationTimeout(
                                    ParameterHelper.getParameterAsLong(request.getParameterMap(),
                                            "expirationTimeout-" + n, 180L) * 86400000);
                            if (activity.getLeft() != null) {
                                configuration.setDeactivatableByManager(activity.getLeft()
                                        .isDeactivatableByManager());
                                if (!activity.getLeft().isDeactivatableByManager()) {
                                    configuration.setActive(true);
                                }
                                if (!activity.getLeft().isDeletable()) {
                                    configuration.setDeletable(false);
                                    configuration.setDeletableByUser(false);
                                }
                            }
                            configurations.add(configuration);
                            break;
                        }
                    }
                }
                activityService.storeActivityConfigurations(configurations);

                String message = MessageHelper
                        .getText(request,
                                "plugins.activity.configuration.success");
                MessageHelper.saveMessage(request, message);
            } catch (ActivityServiceException e) {
                throw new ViewControllerException(500, e.getMessage(), e);
            }

        }
        doGet(request, response, model);
    }

    @Override
    public String getContentTemplate() {
        return "/vm/activity-configuration-content.html.vm";
    }

}
