package com.communote.server.web.fe.portal.user.client.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.fe.portal.user.client.forms.ClientProfileNotificationsForm;

/**
 * Controller for configuring notifications.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientProfileNotificationController {
    /**
     * Method to handle GET-Requests. This is default.
     *
     * @return The ModelAndView to use.
     */
    @RequestMapping
    public ModelAndView get() {
        ClientProfileNotificationsForm notificationSettings = new ClientProfileNotificationsForm();
        notificationSettings
        .setRenderAttachmentLinks(ClientProperty.NOTIFICATION_RENDER_ATTACHMENTLINKS
                .getValue(ClientProperty.DEFAULT_NOTIFICATION_RENDER_ATTACHMENTLINKS));
        notificationSettings.setRenderPermalinks(ClientProperty.NOTIFICATION_RENDER_PERMALINKS
                .getValue(ClientProperty.DEFAULT_NOTIFICATION_RENDER_PERMALINKS));
        notificationSettings
        .setRenderBlogPermalinkInInvitation(ClientProperty.INVITATION_RENDER_BLOG_PERMALINK
                .getValue(ClientProperty.DEFAULT_INVITATION_RENDER_BLOG_PERMALINK));
        notificationSettings.setMaxUsersToMention(ClientProperty.MAX_NUMBER_OF_MENTIONED_USERS
                .getValue(ClientProperty.DEFAULT_MAX_NUMBER_OF_MENTIONED_USERS));
        return new ModelAndView("main.microblog.client.profile.notifications", "command",
                notificationSettings);
    }

    /**
     * Method to save the send settings.
     *
     * @param request
     *            The current request.
     * @param notificationSettings
     *            The settings to save.
     * @param result
     *            The result to bind errors and so on to.
     * @return The ModelAndView to use.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView saveSettings(HttpServletRequest request,
            @ModelAttribute("command") ClientProfileNotificationsForm notificationSettings,
            BindingResult result) {
        Map<ClientConfigurationPropertyConstant, String> settings = new HashMap<ClientConfigurationPropertyConstant, String>();
        settings.put(ClientProperty.NOTIFICATION_RENDER_ATTACHMENTLINKS,
                Boolean.toString(notificationSettings.isRenderAttachmentLinks()));
        settings.put(ClientProperty.NOTIFICATION_RENDER_PERMALINKS,
                Boolean.toString(notificationSettings.isRenderPermalinks()));
        settings.put(ClientProperty.INVITATION_RENDER_BLOG_PERMALINK,
                Boolean.toString(notificationSettings.isRenderBlogPermalinkInInvitation()));
        settings.put(ClientProperty.MAX_NUMBER_OF_MENTIONED_USERS,
                Integer.toString(notificationSettings.getMaxUsersToMention()));
        CommunoteRuntime.getInstance().getConfigurationManager()
                .updateClientConfigurationProperties(settings);
        MessageHelper.saveMessageFromKey(request, "client.profile.notifications.success");
        return get();
    }
}
