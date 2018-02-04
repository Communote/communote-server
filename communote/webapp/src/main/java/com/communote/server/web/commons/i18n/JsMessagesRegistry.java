package com.communote.server.web.commons.i18n;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.event.EventListener;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.persistence.common.messages.ResourceBundleChangedEvent;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * Registry for localized Javascript messages with caching capabilities.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class JsMessagesRegistry implements EventListener<ResourceBundleChangedEvent> {

    /**
     * Built-in category for JS messages required in pages like login, full screen messages,
     * imprint, terms of use, registration
     */
    public static final String CATEGORY_COMMON = "common";
    /**
     * Built-in category for JS messages required in the administration pages
     */
    public static final String CATEGORY_ADMINISTRATION = "admin";
    /**
     * Built-in category for JS messages required in the portal page
     */
    public static final String CATEGORY_PORTAL = "portal";

    private static final Set<String> SHARED_MESSAGE_KEYS;
    static {
        SHARED_MESSAGE_KEYS = new HashSet<String>();
        SHARED_MESSAGE_KEYS.addAll(Arrays.asList(new String[] { "common.changes.save.failed",
                "common.changes.save.success", "common.error.unspecified", "common.more",
                "javascript.cropper.tooltip.move", "javascript.cropper.tooltip.move.doubleclick",
                "javascript.cropper.tooltip.resize", "javascript.dialog.title.error",
                "javascript.dialog.title.info", "javascript.dialog.button.label.ok",
                "javascript.dialog.button.label.cancel", "javascript.dialog.button.label.back",
                "javascript.dialog.button.label.next", "javascript.dialog.button.label.continue",
                "javascript.dialog.button.label.abort", "javascript.dialog.button.label.yes",
                "javascript.dialog.button.label.no", "javascript.dialog.button.close",
                "javascript.dateformatter.pattern.datetime.long",
                "widget.error.message.refresh.failed", "javascript.loading.message",
                "widget.globalid-upload-image.js.crop.dialog.accept",
                "widget.globalid-upload-image.js.crop.dialog.title", "string.validation.empty" }));
    }

    // mapping of buit-in category names to the message keys
    private final Map<String, Set<String>> builtInMessages;
    // mapping of category name to names (e.g. class names) of providers with their additional
    // message keys for the category
    private final Map<String, Map<String, Set<String>>> additionalMessages;

    // maps category names to localized JS objects which hold the message key to message mappings
    private final Map<String, Map<Locale, String>> cachedJsMessages;
    // maps category names to the timestamp the messages were added for that category
    private final Map<String, Long> cacheTimestamp;

    /**
     * Creates the registry
     */
    public JsMessagesRegistry() {
        builtInMessages = new HashMap<String, Set<String>>();
        initCommonMessages();
        initPortalMessages();
        initAdminMessages();
        additionalMessages = new HashMap<String, Map<String, Set<String>>>();
        cachedJsMessages = new HashMap<String, Map<Locale, String>>();
        cacheTimestamp = new HashMap<String, Long>();
        ServiceLocator.findService(EventDispatcher.class).register(this);
    }

    /**
     * Add message keys to the named category.
     *
     * @param providerId
     *            an identifier of the providing instance. This argument is required for removing
     *            the messages later on.
     * @param category
     *            the category to extend with further message keys. This can be one of the built-in
     *            categories (use the CATEGORY_* constants) or a new one provided by a plugin.
     * @param messageKeys
     *            the message keys to add
     * @throws IllegalArgumentException
     *             in case the category is null, empty or blank
     */
    synchronized public void addMessageKeys(String providerId, String category,
            Set<String> messageKeys) {
        if (StringUtils.isBlank(category)) {
            throw new IllegalArgumentException("Category cannot be null, empty or blank.");
        }
        Map<String, Set<String>> mappings = additionalMessages.get(category);
        if (mappings == null) {
            mappings = new HashMap<String, Set<String>>();
            additionalMessages.put(category, mappings);
        }
        mappings.put(providerId, messageKeys);
        // clear cache
        cachedJsMessages.remove(category);
        cacheTimestamp.remove(category);
    }

    /**
     * Method to add even more messages. Just needed for Checkstyle.
     *
     * @param messageKeys
     *            Set to add the messages to.
     */
    private void addMoreMessagesForPortal(HashSet<String> messageKeys) {
        messageKeys.add("blog.filter.period.dateformat");
        messageKeys.add("blog.filter.period.dateformat.picker");
        messageKeys.add("blog.filter.searchbox.title");
        messageKeys.add("blog.filter.summary.period.dateformat");
        messageKeys.add("blog.filter.summary.period.end");
        messageKeys.add("blog.filter.summary.period.start");
        messageKeys.add("blog.filter.summary.remove.author");
        messageKeys.add("blog.filter.summary.remove.author.search");
        messageKeys.add("blog.filter.summary.remove.blog");
        messageKeys.add("blog.filter.summary.remove.discussion");
        messageKeys.add("blog.filter.summary.remove.note");
        messageKeys.add("blog.filter.summary.remove.period.end");
        messageKeys.add("blog.filter.summary.remove.period.start");
        messageKeys.add("blog.filter.summary.remove.tag");
        messageKeys.add("blog.filter.summary.remove.tag.search");
        messageKeys.add("blog.filter.summary.remove.text.search");
        messageKeys.add("blog.overview.tab.admister.dialog.description");
        messageKeys.add("blog.overview.tab.admister.dialog.ok");
        messageKeys.add("blog.overview.tab.admister.dialog.success");
        messageKeys.add("note.move-discussion.description");
        messageKeys.add("note.move-discussion.search");
        messageKeys.add("note.move-discussion.selected");
        messageKeys.add("note.move-discussion.selected.none");
        messageKeys.add("note.move-discussion.title");
        messageKeys.add("follow.link.follow");
        messageKeys.add("hovercard.actions.menu");
        messageKeys.add("hovercard.actions.menu.permalink");
        messageKeys.add("hovercard.actions.topic.email.label");
        messageKeys.add("hovercard.actions.topic.email.tip");

        messageKeys.add("autosuggest.atat.all");
        messageKeys.add("autosuggest.atat.all.terms");
        messageKeys.add("autosuggest.atat.authors");
        messageKeys.add("autosuggest.atat.authors.terms");
        messageKeys.add("autosuggest.atat.discussion");
        messageKeys.add("autosuggest.atat.discussion.terms");
        messageKeys.add("autosuggest.atat.managers");
        messageKeys.add("autosuggest.atat.managers.terms");
        messageKeys.add("autosuggest.search.suggestion");
        messageKeys.add("autosuggest.title.topics");
        messageKeys.add("autosuggest.title.users");

        messageKeys.add("javascript.dialog.note.editor.preferences.intro");
        messageKeys.add("javascript.dialog.note.editor.preferences.suggest.tags");
        messageKeys.add("javascript.dialog.note.editor.preferences.suggest.users");
        messageKeys.add("javascript.dialog.note.editor.preferences.title");
        messageKeys.add("javascript.note.editor.setting.advanced");
        messageKeys.add("javascript.dialog.note.editor.preferences.autoresize");
        messageKeys.add("javascript.note.editor.setting.suggestions.enable");
        messageKeys.add("javascript.note.editor.setting.suggestions.disable");

        messageKeys.add("blog.post.list.head.view.type.classic");
        messageKeys.add("blog.post.list.head.view.type.comment");
        messageKeys.add("blog.post.list.export.rss");
        messageKeys.add("blog.post.list.export.rss.menu");
        messageKeys.add("blog.post.list.export.rtf");
        messageKeys.add("blog.post.list.export.rtf.menu");
        messageKeys.add("blog.overview.view-type.classic");
        messageKeys.add("blog.overview.view-type.hierarchy");

        messageKeys.add("portal.menu.mobile.profile.popup.title");

        messageKeys.add("widget.chronologicalPostList.favoriteMarker.title");
        messageKeys.add("widget.chronologicalPostList.note.action.comment.label");
        messageKeys.add("widget.chronologicalPostList.note.action.comment.title");
        messageKeys.add("widget.chronologicalPostList.note.action.delete.label");
        messageKeys.add("widget.chronologicalPostList.note.action.edit.label");
        messageKeys.add("widget.chronologicalPostList.note.action.edit.title");
        messageKeys.add("widget.chronologicalPostList.note.action.export.label");
        messageKeys.add("widget.chronologicalPostList.note.action.favor.label");
        messageKeys.add("widget.chronologicalPostList.note.action.like.label");
        messageKeys.add("widget.chronologicalPostList.note.action.move.label");
        messageKeys.add("widget.chronologicalPostList.note.action.permalink.label");
        messageKeys.add("widget.chronologicalPostList.note.action.repost.label");
        messageKeys.add("widget.chronologicalPostList.note.action.more.popup.title");

        messageKeys.add("widget.editTopicStructure.subtopics.remove.tooltip");

        messageKeys.add("create.note.dialog.discardChanges.question");
        messageKeys.add("create.note.dialog.discardChanges.title");

        messageKeys.add("common.tagManagement.remove.tag.tooltip");

        messageKeys.add("widget.tagManagement.replace.confirmation.title");
        messageKeys.add("widget.tagManagement.replace.confirmation.content");
        messageKeys.add("widget.tagManagement.replace.confirmation.content.hint");
        messageKeys.add("widget.tagManagement.delete.confirmation.title");
        messageKeys.add("widget.tagManagement.delete.confirmation.content");

        messageKeys.add("widget.mainPageVerticalNavigation.favorites.sort.error");
        messageKeys.add("widget.mainPageVerticalNavigation.favorites.remove.missing.confirm");
        messageKeys.add("widget.mainPageVerticalNavigation.favorites.remove.missing.details.tag");
        messageKeys.add("widget.mainPageVerticalNavigation.favorites.remove.missing.details.topic");
        messageKeys.add("widget.mainPageVerticalNavigation.favorites.remove.missing.details.user");
        messageKeys.add("widget.mainPageVerticalNavigation.favorites.remove.missing.title");

        messageKeys.add("widget.createNote.richTextEditor.linkDialog.link.title");
        messageKeys.add("widget.createNote.richTextEditor.linkDialog.link.url");
        messageKeys.add("widget.createNote.richTextEditor.linkDialog.title");
    }

    /**
     * Appends the mappings for the provided message keys to their localized messages to the string
     * builder.
     *
     * @param sb
     *            the string builder to append to
     * @param prefix
     *            the prefix to add before appending
     * @param messageKeys
     *            the message keys to process. Can be null.
     * @param locale
     *            the locale to use
     * @return the prefix to be used by further calls to this function
     */
    private String appendJsMessageMappings(StringBuilder sb, String prefix,
            Set<String> messageKeys, Locale locale) {
        if (messageKeys == null) {
            return prefix;
        }
        ResourceBundleManager bundleManager = ResourceBundleManager.instance();
        for (String key : messageKeys) {
            sb.append(prefix);
            sb.append("\"");
            sb.append(key);
            sb.append("\"");
            sb.append(":");
            sb.append("\"");
            sb.append(StringEscapeUtils.escapeJavaScript(bundleManager.getText(key, locale)));
            sb.append("\"");
            prefix = ",";
        }
        return prefix;
    }

    /**
     * Caches the string representation of the Javascript object with localized messages of the
     * given category if not yet cached.
     *
     * @param category
     *            the category
     * @param locale
     *            the locale to use
     * @return the string representation of the Javascript object
     */
    synchronized private String createCachedJsMessagesString(String category, Locale locale) {
        String jsMessagesString = null;
        Map<Locale, String> localizedMessages = cachedJsMessages.get(category);
        if (localizedMessages != null) {
            jsMessagesString = localizedMessages.get(locale);
        } else {
            // do not cache none existing categories
            if (!builtInMessages.containsKey(category) && !additionalMessages.containsKey(category)) {
                jsMessagesString = "{}";
            } else {
                localizedMessages = new HashMap<Locale, String>();
                // init cache for category
                cachedJsMessages.put(category, localizedMessages);
                cacheTimestamp.put(category, System.currentTimeMillis());
            }
        }
        if (jsMessagesString == null) {
            String prefix = "";
            StringBuilder sb = new StringBuilder("{");
            prefix = appendJsMessageMappings(sb, prefix, SHARED_MESSAGE_KEYS, locale);
            // check built-in messages
            Set<String> messageKeySet = builtInMessages.get(category);
            prefix = appendJsMessageMappings(sb, prefix, messageKeySet, locale);
            // check additional messages
            Map<String, Set<String>> additionalMessageKeySets = additionalMessages.get(category);
            if (additionalMessageKeySets != null) {
                for (Set<String> keySet : additionalMessageKeySets.values()) {
                    prefix = appendJsMessageMappings(sb, prefix, keySet, locale);
                }
            }
            sb.append("}");
            jsMessagesString = sb.toString();
            localizedMessages.put(locale, jsMessagesString);
        }
        return jsMessagesString;
    }

    /**
     * Returns a string representation of a Javascript object with the localized messages of the
     * given category. The object will be in the format: {"key1":"value1","key2":"value2", ... ,
     * "keyN":"valueN" }
     *
     * @param request
     *            the current request
     * @param category
     *            the category to retrieve. This can be one of the CATEGORY_* constants or a
     *            category defined by a plugin.
     * @return the string representation of the Javascript object
     */
    public String getJsMessages(HttpServletRequest request, String category) {
        if (StringUtils.isBlank(category)) {
            return "{}";
        }
        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        return getJsMessages(locale, category);
    }

    /**
     * Returns a string representation of a Javascript object with the localized messages of the
     * given category. The object will be in the format: {"key1":"value1","key2":"value2", ... ,
     * "keyN":"valueN" }
     *
     * @param locale
     *            the locale to use
     * @param category
     *            the category to retrieve. This can be one of the CATEGORY_* constants or a
     *            category defined by a plugin.
     * @return the string representation of the Javascript object
     */
    public String getJsMessages(Locale locale, String category) {
        String jsMessagesString = null;
        Map<Locale, String> localizedMessages = cachedJsMessages.get(category);
        if (localizedMessages != null) {
            jsMessagesString = localizedMessages.get(locale);
        }
        if (jsMessagesString == null) {
            jsMessagesString = createCachedJsMessagesString(category, locale);
        }
        return jsMessagesString;
    }

    /**
     * Get timestamp of the last modification of the messages of the category.
     *
     * @param category
     *            the name of the category
     * @return the timestamp
     */
    public synchronized long getLastModificationTimestamp(String category) {
        // do not cache none existing categories or blank categories
        if (StringUtils.isBlank(category)
                || (!builtInMessages.containsKey(category) && !additionalMessages
                        .containsKey(category))) {
            return CommunoteRuntime.getInstance().getApplicationInformation().getBuildTimestamp();
        }
        Long timestamp = cacheTimestamp.get(category);
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
            // init cache for category
            cachedJsMessages.put(category, new HashMap<Locale, String>());
            cacheTimestamp.put(category, timestamp);
        }
        return timestamp;
    }

    @Override
    public Class<ResourceBundleChangedEvent> getObservedEvent() {
        return ResourceBundleChangedEvent.class;
    }

    @Override
    public void handle(ResourceBundleChangedEvent event) {
        synchronized (this) {
            // clear all cached messages
            cachedJsMessages.clear();
            cacheTimestamp.clear();
        }
    }

    /**
     * initialize the message keys of the built-in category for the admin section
     */
    private void initAdminMessages() {
        HashSet<String> messageKeys = new HashSet<String>();
        messageKeys.add("client.user.management.delete.user.data.text");
        messageKeys.add("widget.user.management.profile.delete.mode.disable");
        messageKeys.add("widget.user.management.profile.delete.mode.anonymize");
        messageKeys.add("client.user.group.management.delete.question");
        messageKeys.add("client.user.group.management.group.toolbar.delete");
        messageKeys.add("autosuggest.found.all");
        messageKeys.add("autosuggest.found.more");
        messageKeys.add("autosuggest.found.nothing");
        messageKeys.add("autosuggest.searching");
        builtInMessages.put(CATEGORY_ADMINISTRATION, messageKeys);
    }

    /**
     * initialize the message keys of the built-in category common
     */
    private void initCommonMessages() {
        HashSet<String> messagesKeys = new HashSet<String>();
        builtInMessages.put(CATEGORY_COMMON, messagesKeys);
    }

    /**
     * initialize the message keys of the built-in category portal
     */
    private void initPortalMessages() {
        HashSet<String> messageKeys = new HashSet<String>();
        messageKeys.add("add.resource.delete");
        messageKeys.add("autosuggest.found.all");
        messageKeys.add("autosuggest.found.more");
        messageKeys.add("autosuggest.found.nothing");
        messageKeys.add("autosuggest.searching");
        messageKeys.add("blog.create.email.undefined.tooltip");
        messageKeys.add("blog.delete.choose.blog");
        messageKeys.add("blog.delete.confirmation.info");
        messageKeys.add("blog.delete.selected");
        messageKeys.add("blog.delete.selected.none");
        messageKeys.add("blog.follow.button.follow");
        messageKeys.add("blog.follow.button.unfollow");
        messageKeys.add("blog.follow.text.follow");
        messageKeys.add("blog.follow.text.unfollow");
        messageKeys.add("blog.management.delete.success");
        messageKeys.add("blog.management.error.select.blog.first");
        messageKeys.add("blog.member.invite.popup.title");
        messageKeys.add("blog.member.management.addentity.error.empty.entityid");
        messageKeys.add("blog.member.management.addentity.success");
        messageKeys.add("blog.member.management.details.hide");
        messageKeys.add("blog.member.management.details.show");
        messageKeys.add("blog.member.management.inviteuser.success");
        messageKeys.add("blog.member.management.public.updated");
        messageKeys.add("blog.member.management.remove.external.object");
        messageKeys.add("blog.member.management.remove.external.object.question");
        messageKeys.add("blog.member.management.removeuser.success");
        messageKeys.add("blog.member.management.selfdelete.question");
        messageKeys.add("blog.member.management.selfdelete.title");
        messageKeys.add("blog.member.management.selfmodify.question");
        messageKeys.add("blog.member.management.selfmodify.title");
        messageKeys.add("blog.member.public.access.update.success");
        messageKeys.add("blog.post.dm.overwrite.existing");
        messageKeys.add("blog.post.dm.overwrite.existing.title");
        messageKeys.add("blog.post.list.comment.refers_to");
        messageKeys.add("blog.post.list.comment.show_more.singular.false");
        messageKeys.add("blog.post.list.comment.show_more.singular.true");
        messageKeys.add("blog.post.list.autorefresh.title");
        messageKeys.add("blog.post.list.follow.follow.author");
        messageKeys.add("blog.post.list.follow.follow.author.menu");
        messageKeys.add("blog.post.list.follow.follow.blog");
        messageKeys.add("blog.post.list.follow.follow.blog.menu");
        messageKeys.add("blog.post.list.follow.follow.discussion");
        messageKeys.add("blog.post.list.follow.follow.discussion.menu");
        messageKeys.add("blog.post.list.follow.tag.follow");
        messageKeys.add("blog.post.list.follow.tag.unfollow");
        messageKeys.add("blog.post.list.follow.unfollow.author");
        messageKeys.add("blog.post.list.follow.unfollow.author.menu");
        messageKeys.add("blog.post.list.follow.unfollow.blog");
        messageKeys.add("blog.post.list.follow.unfollow.blog.menu");
        messageKeys.add("blog.post.list.follow.unfollow.discussion");
        messageKeys.add("blog.post.list.follow.unfollow.discussion.menu");
        messageKeys.add("blogpost.autosave.discard");
        messageKeys.add("blogpost.autosave.loaded");
        messageKeys.add("blogpost.autosave.saved");
        messageKeys.add("blogpost.autosave.saving");
        messageKeys.add("blogpost.create.attachments.remove.tooltip");
        messageKeys.add("blogpost.create.attachments.uploading");
        messageKeys.add("blogpost.create.blogs.remove.tooltip");
        messageKeys.add("blogpost.create.crosspost.remove.tooltip");
        messageKeys.add("blogpost.create.no.default.blog");
        messageKeys.add("blogpost.create.no.writable.blog.selected");
        messageKeys.add("blogpost.create.tags.remove.tooltip");
        messageKeys.add("blogpost.create.topics.hint");
        messageKeys.add("blogpost.create.topics.crosspost.hint");
        messageKeys.add("blogpost.create.users.remove.tooltip");
        messageKeys.add("blogpost.create.submit.confirm.unsaved.blog");
        messageKeys.add("blogpost.create.submit.confirm.unsaved.user");
        messageKeys.add("widget.createNote.unconfirmed.input.warning");
        messageKeys.add("widget.createNote.topics.unconfirmed.input.inputName");
        messageKeys.add("widget.createNote.mentions.unconfirmed.input.inputName");
        messageKeys.add("blogpost.delete.confirmation");
        messageKeys.add("blogpost.delete.popup.title");
        messageKeys.add("blogpost.delete.with.comments.confirmation");
        messageKeys.add("create.note.attachment.preview.title");
        messageKeys.add("create.note.autosave.discard.title");
        messageKeys.add("create.note.autosave.discard.question");
        messageKeys.add("error.blogpost.create.failed");
        messageKeys.add("error.blogpost.edit.remove-direct-user");
        messageKeys.add("javascript.calendar.day.hover.pattern");
        messageKeys.add("javascript.dialog.export.button.discussion");
        messageKeys.add("javascript.dialog.export.button.note");
        messageKeys.add("javascript.dialog.export.message");
        messageKeys.add("javascript.dialog.export.title");
        messageKeys.add("javascript.dialog.user.image.button.accept");
        messageKeys.add("javascript.dialog.user.image.title.crop");

        messageKeys.add("mediaboxAdvanced.download.title");
        messageKeys.add("mediaboxAdvanced.overview.title");
        messageKeys.add("user.profile.follow.button.follow");
        messageKeys.add("user.profile.follow.button.unfollow");
        messageKeys.add("user.profile.follow.text.follow");
        messageKeys.add("user.profile.follow.text.unfollow");
        messageKeys.add("user.profile.language.change.hint.content");
        messageKeys.add("user.profile.language.change.hint.title");
        messageKeys.add("custom.message.default.blog");
        addMoreMessagesForPortal(messageKeys);
        builtInMessages.put(CATEGORY_PORTAL, messageKeys);
    }

    /**
     * Remove all message keys of the given provider.
     *
     * @param provider
     *            the provider
     */
    synchronized public void removeMessageKeysOfProvider(String provider) {
        for (Map.Entry<String, Map<String, Set<String>>> messageEntry : additionalMessages
                .entrySet()) {
            String category = messageEntry.getKey();
            if (messageEntry.getValue().remove(provider) != null) {
                cachedJsMessages.remove(category);
                cacheTimestamp.remove(category);
            }
        }
    }
}
