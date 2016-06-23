package com.communote.server.core.blog.export.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.string.StringEscapeHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.attachment.AttachmentData;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.blog.NoteWriterException;
import com.communote.server.core.blog.export.FilterParameterResolver;
import com.communote.server.core.blog.export.NoteWriter;
import com.communote.server.core.blog.export.PermalinkGenerationManagement;
import com.communote.server.core.retrieval.helper.AttachmentHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.core.user.helper.UserNameFormat;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.logical.LogicalTagFormula;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.service.NoteService;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * Exporter for RSS.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RssNoteWriter implements NoteWriter {

    private static final String SEPARATOR = "; ";
    private static final String RSS = "xml";
    private static final String CONTENT_TYPE = "application/rss+xml";
    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(RssNoteWriter.class);
    private ResourceBundleManager messages;

    /**
     * Creates the feeds entry.
     *
     * @param item
     *            The note.
     * @param locale
     *            The locale.
     * @param dateFormatter
     *            The date formatter.
     * @return The entry as {@link SyndEntry}.
     */
    private SyndEntry createRssEntry(final NoteData item, Locale locale, DateFormat dateFormatter) {
        SyndEntry entry = new SyndEntryImpl();
        String noHtmlTitle = StringEscapeHelper.getSingleLineTextFromXML(item.getContent());
        int endIndex = Math.min(noHtmlTitle.length(), 100);
        String title = item.getBlog().getTitle() + ": " + noHtmlTitle.substring(0, endIndex);
        if (title.length() < noHtmlTitle.length()) {
            title += "...";
        }
        if (item.isDirect()) {
            title = messages.getText("rss.item.direct.message.prefix", locale) + " " + title;
        }
        entry.setTitle(title);
        entry.setLink(ServiceLocator.instance().getService(PermalinkGenerationManagement.class)
                .getNoteLink(item.getBlog().getAlias(), item.getId(), true));
        entry.setPublishedDate(item.getCreationDate());
        entry.setUpdatedDate(item.getLastModificationDate());
        String author = UserNameHelper.getCompleteSignature(item.getUser(), " ", item.getUser()
                .getAlias());
        entry.setAuthor(author);

        ArrayList<SyndCategory> categories = new ArrayList<SyndCategory>();
        for (TagData tag : item.getTags()) {
            SyndCategoryImpl category = new SyndCategoryImpl();
            category.setName(tag.getName());
            category.setTaxonomyUri(ServiceLocator.instance()
                    .getService(PermalinkGenerationManagement.class)
                    .getTagLink(tag.getName(), true));
            categories.add(category);
        }
        entry.setCategories(categories);
        SyndContent description = new SyndContentImpl();
        description.setType("text/html");
        StringBuilder content = new StringBuilder();
        content.append(messages.getText("newsfeed.microblog.content.metadata.author", locale));
        content.append(author);
        content.append(messages.getText(
                "newsfeed.microblog.content.metadata.author.date.separator", locale));
        content.append(dateFormatter.format(item.getCreationDate()));
        content.append(messages.getText(
                "newsfeed.microblog.content.metadata.date.content.separator", locale));
        content.append(item.getContent());

        if (CollectionUtils.isNotEmpty(item.getAttachments())) {
            content.append(messages.getText("rss.item.attachment.list", locale) + " ");
            for (AttachmentData attachment : item.getAttachments()) {
                content.append("<a href=\"");
                content.append(AttachmentHelper.determineAbsoluteAttachmentUrl(attachment, true));
                content.append("\">");
                content.append(attachment.getFileName());
                content.append("</a> ");
            }
        }
        description.setValue(content.toString());
        entry.setDescription(description);
        return entry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    /**
     *
     * @param user
     *            The user.
     * @param locale
     *            The locale.
     * @param queryInstance
     *            The query instance containing the filter configuration.
     * @param dateFormatter
     *            The date formatter.
     * @return The description of this feed.
     * @throws AuthorizationException
     *             Exception.
     * @throws NoteNotFoundException
     *             Exception.
     */
    private String getDescription(User user, NoteQueryParameters queryInstance, Locale locale,
            DateFormat dateFormatter) throws NoteNotFoundException, AuthorizationException {
        StringBuilder result = new StringBuilder();
        result.append(ClientHelper.getCurrentClient().getName());
        result.append(SEPARATOR);
        result.append(messages.getText("export.post.date", locale));
        result.append(dateFormatter.format(new Date()));
        result.append(SEPARATOR);
        result.append(messages.getText("newsfeed.microblog.description.unfiltered.is.direct."
                + queryInstance.isDirectMessage(), locale, new Object[] { queryInstance
                    .getResultSpecification().getNumberOfElements() })
                    + " ");
        result.append(messages.getText("export.post.exported.by", locale) + " ");
        result.append(user.getProfile().getFirstName() + " " + user.getProfile().getLastName()
                + " (" + user.getAlias() + ")");
        result.append(SEPARATOR);
        result.append(messages.getText("export.postlist.filter.header", locale) + " ");
        result.append(getFilters(queryInstance, user, dateFormatter, messages));
        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileExtension() {
        return RSS;
    }

    /**
     * This method returns the current filter settings as an unordered list.
     *
     * @param queryParameters
     *            The query instance with filter settings.
     * @param messages
     *            The {@link ResourceBundleManager}.
     * @param user
     *            The user.
     * @param dateFormatter
     *            Date formater.
     * @throws NoteNotFoundException
     *             NoteNotFoundException
     * @throws AuthorizationException
     *             Exception.
     * @return Filters as {@link StringBuilder}.
     */
    private String getFilters(NoteQueryParameters queryParameters, User user,
            DateFormat dateFormatter, ResourceBundleManager messages) throws NoteNotFoundException,
            AuthorizationException {
        if (queryParameters.getNoteId() != null) {
            return messages.getText("export.postlist.filter.single.post", user.getLanguageLocale());
        }

        StringBuilder result = new StringBuilder();
        handleFollowFavoriteDirectOnly(queryParameters, user.getLanguageLocale(), messages, result);
        TaggingCoreItemUTPExtension utpExt = queryParameters.getTypeSpecificExtension();
        handleUsersFilter(messages, user.getLanguageLocale(), result,
                queryParameters.getUserToBeNotified());
        handleParentPostFilter(messages, dateFormatter, user.getLanguageLocale(), result, utpExt);
        handleDatesFilter(queryParameters, user.getLanguageLocale(), dateFormatter, messages,
                result);
        handleBlogFilter(messages, user.getLanguageLocale(), result, utpExt);
        handleDiscussionFilter(messages, user.getLanguageLocale(), result, dateFormatter,
                queryParameters.getDiscussionId());

        if (queryParameters.getUserIds() != null) {
            Collection<String> authorList = new ArrayList<String>();
            for (Long id : queryParameters.getUserIds()) {
                User author = ServiceLocator.instance().getService(UserManagement.class)
                        .findUserByUserId(id);
                if (author != null) {
                    authorList.add(UserNameHelper.getDetailedUserSignature(author));
                }
            }
            result.append(messages.getText("export.postlist.filter.authors",
                    user.getLanguageLocale(), StringUtils.join(authorList, ", "))
                    + SEPARATOR);
        }

        handleTagsFilter(queryParameters.getLogicalTags(), queryParameters.getTagIds(),
                user.getLanguageLocale(), messages, result);
        handleUserSearchFilter(queryParameters.getUserSearchFilters(), user.getLanguageLocale(),
                messages, result);

        if (queryParameters.getFullTextSearchFilters() != null) {
            result.append(messages.getText("export.postlist.filter.text", user.getLanguageLocale(),
                    StringUtils.join(queryParameters.getFullTextSearchFilters(), " ")));
        }
        if (result.length() == 0) {
            result.append(messages.getText("export.postlist.filter.none", user.getLanguageLocale()));
        }
        return result.toString();
    }

    /**
     * @param locale
     *            The locale to use.
     *
     * @return The title of the feed.
     */
    public String getTitle(Locale locale) {
        messages = ResourceBundleManager.instance();
        String clientName = ClientHelper.getCurrentClient().getName();
        return messages.getText("newsfeed.microblog.title.client", locale, clientName);
    }

    /**
     * This method handles the filtered blogs.
     *
     * @param resourcesManager
     *            {@link ResourceBundleManager}.
     * @param locale
     *            The locale.
     * @param result
     *            Filters as {@link StringBuilder}..
     * @param utpExt
     *            {@link TaggingCoreItemUTPExtension}.
     */
    private void handleBlogFilter(ResourceBundleManager resourcesManager, Locale locale,
            StringBuilder result, TaggingCoreItemUTPExtension utpExt) {
        if (utpExt.getBlogFilter() != null) {
            Collection<Blog> blogs = ServiceLocator.instance().getService(BlogManagement.class)
                    .findBlogsById(utpExt.getBlogFilter());
            if (blogs != null && blogs.size() > 0) {
                Collection<String> titles = new ArrayList<String>(blogs.size());
                String localizedBlogTitle = StringUtils.EMPTY;
                for (Blog b : blogs) {
                    localizedBlogTitle = b.getTitle();
                    titles.add(localizedBlogTitle);
                }
                result.append(resourcesManager.getText("export.postlist.filter.blog", locale,
                        StringUtils.join(titles, ", ")) + SEPARATOR);
            }
        }
    }

    /**
     * This method handles the start and end date of filters.
     *
     * @param queryInstance
     *            The {@link NoteQueryParameters}
     * @param locale
     *            The locale.
     * @param dateFormatter
     *            Date formater.
     * @param resourcesManager
     *            {@link ResourceBundleManager}.
     * @param result
     *            Filters as {@link StringBuilder}.
     */
    private void handleDatesFilter(NoteQueryParameters queryInstance, Locale locale,
            DateFormat dateFormatter, ResourceBundleManager resourcesManager, StringBuilder result) {
        String startDateString = (queryInstance.getLowerTagDate() != null) ? dateFormatter
                .format(queryInstance.getLowerTagDate()) : null;
                String endDateString = (queryInstance.getUpperTagDate() != null) ? dateFormatter
                        .format(queryInstance.getUpperTagDate()) : null;
                        String msgKeyPart = null;
                        String[] dateArgs = null;
                        if (StringUtils.isNotEmpty(startDateString)) {
                            msgKeyPart = "after";
                            dateArgs = new String[] { startDateString };
                        }
                        if (StringUtils.isNotEmpty(endDateString)) {
                            if (msgKeyPart != null) {
                                if (endDateString.equals(startDateString)) {
                                    msgKeyPart = "at";
                                } else {
                                    dateArgs = new String[] { startDateString, endDateString };
                                    msgKeyPart = "between";
                                }
                            } else {
                                msgKeyPart = "before";
                                dateArgs = new String[] { endDateString };
                            }
                        }
                        if (msgKeyPart != null) {
                            result.append(resourcesManager.getText("export.postlist.filter.date." + msgKeyPart,
                                    locale, (Object[]) dateArgs) + SEPARATOR);
                        }
    }

    /**
     * Adds information about the discussion to the filter summary, if there is a discussion.
     *
     * @param messages
     *            The messages.
     * @param locale
     *            Locale of the user.
     * @param result
     *            The result.
     * @param dateFormatter
     *            Formatter.
     * @param discussionId
     *            The discussion id.
     */
    private void handleDiscussionFilter(ResourceBundleManager messages, Locale locale,
            StringBuilder result, DateFormat dateFormatter, Long discussionId) {
        if (discussionId == null) {
            return;
        }
        try {
            // no need to pass a render mode and trigger the pre-processors because we are only
            // interested in the author
            NoteData note = ServiceLocator.instance().getService(NoteService.class)
                    .getNote(discussionId, new NoteRenderContext(null, locale));
            result.append(messages.getText("export.postlist.filter.discussion", locale,
                    UserNameHelper.getUserSignature(note.getUser(), UserNameFormat.MEDIUM),
                    dateFormatter.format(note.getCreationDate()))
                    + SEPARATOR);
        } catch (NoteNotFoundException e) {
            // TODO add some special text if discussion does not exist
            LOG.error(e.getMessage());
        } catch (AuthorizationException e) {
            // TODO add some special text if the user has no access (anymore)
            LOG.error(e.getMessage());
        }
    }

    /**
     * Handles the flags denoting whether only favorite, followed or direct items are to be
     * retrieved.
     *
     * @param queryInstance
     *            The {@link NoteQueryParameters}
     * @param locale
     *            The locale.
     * @param resourcesManager
     *            {@link ResourceBundleManager}.
     * @param filters
     *            The filters to append this filter to.
     */
    private void handleFollowFavoriteDirectOnly(NoteQueryParameters queryInstance, Locale locale,
            ResourceBundleManager resourcesManager, StringBuilder filters) {
        if (queryInstance.isRetrieveOnlyFollowedItems()) {
            filters.append(resourcesManager.getText("export.postlist.filter.follow", locale)
                    + SEPARATOR);
        }
        if (queryInstance.isFavorites()) {
            filters.append(resourcesManager.getText("export.postlist.filter.favorite", locale)
                    + SEPARATOR);
        }
        if (queryInstance.isDirectMessage()) {
            filters.append(resourcesManager.getText("export.postlist.filter.direct.only", locale)
                    + SEPARATOR);
        }
    }

    /**
     * This method handles the parent post.
     *
     * @param resourcesManager
     *            {@link ResourceBundleManager}.
     * @param dateFormatter
     *            Date formatter.
     * @param locale
     *            The local.
     * @param filters
     *            Filters as {@link StringBuilder}.
     * @param utpExt
     *            {@link TaggingCoreItemUTPExtension}.
     * @throws NoteNotFoundException
     *             exception.
     * @throws AuthorizationException
     *             Exception.
     */
    private void handleParentPostFilter(ResourceBundleManager resourcesManager,
            DateFormat dateFormatter, Locale locale, StringBuilder filters,
            TaggingCoreItemUTPExtension utpExt) throws NoteNotFoundException,
            AuthorizationException {
        if (utpExt.getParentPostId() != null) {
            // no need to pass a render mode and trigger the pre-processors because we are only
            // interested in the author
            NoteData noteData = ServiceLocator.instance().getService(NoteService.class)
                    .getNote(utpExt.getParentPostId(), new NoteRenderContext(null, locale));
            // TODO add some text if the note does not exist
            if (noteData != null) {
                filters.append(resourcesManager.getText("export.postlist.filter.parentpost",
                        locale, UserNameHelper.getDetailedUserSignature(noteData.getUser()),
                        dateFormatter.format(noteData.getCreationDate().getTime()))
                        + SEPARATOR);
            }
        }
    }

    /**
     * This method handles the set tags.
     *
     * @param f
     *            the tag formula
     * @param tagIds
     *            Id's of filtered tags.
     * @param locale
     *            The locale.
     * @param resourcesManager
     *            The {@link ResourceBundleManager}.
     * @param result
     *            The filters to be added to.
     */
    private void handleTagsFilter(LogicalTagFormula f, Set<Long> tagIds, Locale locale,
            ResourceBundleManager resourcesManager, StringBuilder result) {
        String tags = FilterParameterResolver.getInstance().resolveTags(f);
        if (tagIds != null && !tagIds.isEmpty()) {
            tags = StringUtils.isBlank(tags) ? "" : tags + ",";
            TagManagement tagManagement = ServiceLocator.instance().getService(TagManagement.class);
            String prefix = "";
            for (Long tagId : tagIds) {
                Tag storedTag = tagManagement.findTag(tagId);
                if (storedTag != null) {
                    tags = tags + prefix + storedTag.getName();
                    prefix = ",";
                }
            }
        }
        if (StringUtils.isNotBlank(tags)) {
            result.append(resourcesManager.getText("export.postlist.filter.tags", locale, tags)
                    + SEPARATOR);
        }
    }

    /**
     * This method handles the user search filter.
     *
     * @param userSearchFilters
     *            List of users to be searched for.
     * @param locale
     *            The locale.
     * @param resourcesManager
     *            The {@link ResourceBundleManager}.
     * @param result
     *            The list of filters.
     */
    private void handleUserSearchFilter(String[] userSearchFilters, Locale locale,
            ResourceBundleManager resourcesManager, StringBuilder result) {
        if (!ArrayUtils.isEmpty(userSearchFilters)) {
            result.append(resourcesManager.getText("export.postlist.filter.usertext", locale,
                    StringUtils.join(userSearchFilters, ", ")) + SEPARATOR);
        }
    }

    /**
     * This method handles the users to be notified filter string.
     *
     * @param resourcesManager
     *            The {@link ResourceBundleManager}.
     * @param locale
     *            The locale.
     * @param result
     *            The list of filters.
     * @param usersToBeNotifiedAsLong
     *            The list of users.
     */
    private void handleUsersFilter(ResourceBundleManager resourcesManager, Locale locale,
            StringBuilder result, Long[] usersToBeNotifiedAsLong) {
        if (!ArrayUtils.isEmpty(usersToBeNotifiedAsLong)) {
            Collection<String> usersToBeNotified = new ArrayList<String>();
            for (Long id : usersToBeNotifiedAsLong) {
                User userToBeNotified = ServiceLocator.instance().getService(UserManagement.class)
                        .findUserByUserId(id);
                if (userToBeNotified != null) {
                    usersToBeNotified
                    .add(UserNameHelper.getDetailedUserSignature(userToBeNotified));
                }
            }
            if (!usersToBeNotified.isEmpty()) {
                result.append(resourcesManager.getText("export.postlist.filter.usernotify", locale,
                        StringUtils.join(usersToBeNotified, ", ")) + SEPARATOR);
            }
        }
    }

    /**
     * @return <code>false</code>
     */
    @Override
    public boolean isAttachment() {
        return false;
    }

    @Override
    public boolean supportsHtmlContent() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean valid(String exportFormat) {
        return exportFormat != null && RSS.equals(exportFormat.toLowerCase());
    }

    /**
     * Exports to RSS.
     *
     * {@inheritDoc}
     */
    @Override
    public void write(NoteQueryParameters queryInstance, OutputStream outputStream,
            Collection<NoteData> notes, String requestUrl) throws NoteWriterException {
        User user = SecurityHelper.assertCurrentKenmeiUser();
        Locale locale = user.getLanguageLocale();
        DateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",
                Locale.ENGLISH);
        dateFormatter.setTimeZone(UserManagementHelper.getEffectiveUserTimeZone());
        try {
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType("rss_2.0");
            feed.setTitle(getTitle(locale));
            feed.setLink(requestUrl);
            feed.setDescription(getDescription(user, queryInstance, locale, dateFormatter));
            ArrayList<SyndEntry> entries = new ArrayList<SyndEntry>();
            for (NoteData note : notes) {
                entries.add(createRssEntry(note, locale, dateFormatter));
            }
            feed.setEntries(entries);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer, CommunoteRuntime.getInstance().getConfigurationManager()
                    .getDevelopmentProperties().isDevelopement());
        } catch (IOException e) {
            LOG.error("There was an error writing to the stream.", e);
        } catch (FeedException e) {
            LOG.error("There was an error creating the feed.", e);
        } catch (NoteNotFoundException e) {
            throw new NoteWriterException(
                    "Error writing to output stream(" + e.getMessage() + ").", e);
        } catch (AuthorizationException e) {
            throw new NoteWriterException(
                    "Error writing to output stream(" + e.getMessage() + ").", e);
        } finally {
            // The stream may not be closed here.
            // IOUtils.closeQuietly(writer);
        }
    }
}
