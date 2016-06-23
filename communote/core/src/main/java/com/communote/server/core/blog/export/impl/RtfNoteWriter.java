package com.communote.server.core.blog.export.impl;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.converter.Converter;
import com.communote.common.image.ImageSize;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.image.Image;
import com.communote.server.api.core.image.ImageDescriptor;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.ImageNotFoundException;
import com.communote.server.api.core.image.type.UserImageDescriptor;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.blog.NoteWriterException;
import com.communote.server.core.blog.export.FilterParameterResolver;
import com.communote.server.core.blog.export.NoteWriter;
import com.communote.server.core.image.type.ClientImageDescriptor;
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
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.model.user.User;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.service.NoteService;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.List;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.field.RtfPageNumber;
import com.lowagie.text.rtf.field.RtfTotalPageNumber;
import com.lowagie.text.rtf.headerfooter.RtfHeaderFooter;

/**
 * This abstract class provides methods for creating footer and header for PDF and RTF documents,
 * which both uses <a href="http://www.lowagie.com/iText/">iText</a> for creating the output
 * document.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RtfNoteWriter implements NoteWriter {

    private static final String RTF = "rtf";
    private static final String CONTENT_TYPE = "application/rtf";
    private final static Logger LOG = LoggerFactory.getLogger(RtfNoteWriter.class);
    private final static Font FONT_META_INFORMATION;
    static {
        FONT_META_INFORMATION = new Font();
        FONT_META_INFORMATION.setSize(8);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    /**
     * This message creates the description element at the beginning of the exported document.
     *
     * @param queryInstance
     *            The query instance to extract needed data from.
     * @param user
     *            The user.
     * @param fullDateFormatter
     *            Formatter for full dates.
     * @param dateFormatter
     *            Formatter for short dates.r.
     * @param resourcesManager
     *            The resources manager to retrieve resources from.
     * @return An object to be used as a description.
     * @throws NoteNotFoundException
     *             Exception.
     * @throws AuthorizationException
     *             Exception.
     */
    private Element getDescription(NoteQueryParameters queryInstance, User user,
            DateFormat dateFormatter, DateFormat fullDateFormatter,
            ResourceBundleManager resourcesManager) throws NoteNotFoundException,
            AuthorizationException {
        Collection<Object> elements = new ArrayList<Object>();
        Font boldFont = new Font();
        boldFont.setStyle(Font.BOLD);
        elements.add(RtfElementFactory.createChunk("\n"));
        Locale locale = user.getLanguageLocale();
        elements.add(RtfElementFactory.createChunk(
                resourcesManager.getText("export.postlist.filter.header", locale), boldFont));
        elements.add(RtfElementFactory.createChunk("\n"));
        elements.add(getFilters(queryInstance, user, dateFormatter, resourcesManager));
        return RtfElementFactory.createPhrase(elements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileExtension() {
        return RTF;
    }

    /**
     * This method returns the current filter settings as an unordered list.
     *
     * @param queryParameters
     *            The query instance with filter settings.
     * @param resourcesManager
     *            The {@link ResourceBundleManager}.
     * @param user
     *            The user.
     * @param dateFormatter
     *            Date formater.
     * @throws NoteNotFoundException
     *             NoteNotFoundException
     * @throws AuthorizationException
     *             Exception.
     * @return Filters as {@link List}.
     */
    private Element getFilters(NoteQueryParameters queryParameters, User user,
            DateFormat dateFormatter, ResourceBundleManager resourcesManager)
                    throws NoteNotFoundException, AuthorizationException {

        List filters = new List(List.UNORDERED);
        filters.setIndentationLeft(20);
        if (queryParameters.getNoteId() != null) {
            filters.add(RtfElementFactory.createListItem(resourcesManager.getText(
                    "export.postlist.filter.single.post", user.getLanguageLocale())));
            return filters;
        }

        handleFollowFavoriteDirectOnly(queryParameters, user.getLanguageLocale(), resourcesManager,
                filters);
        TaggingCoreItemUTPExtension utpExt = queryParameters.getTypeSpecificExtension();
        handleUsersFilter(resourcesManager, user.getLanguageLocale(), filters,
                queryParameters.getUserToBeNotified());
        handleParentPostFilter(resourcesManager, dateFormatter, user.getLanguageLocale(), filters,
                utpExt);
        handleDatesFilter(queryParameters, user.getLanguageLocale(), dateFormatter,
                resourcesManager, filters);
        handleBlogFilter(resourcesManager, user.getLanguageLocale(), filters, utpExt);
        handleDiscussionFilter(resourcesManager, user.getLanguageLocale(), dateFormatter,
                queryParameters.getDiscussionId(), filters);

        if (queryParameters.getUserIds() != null) {
            final Collection<String> authorList = new ArrayList<String>();
            for (Long id : queryParameters.getUserIds()) {
                ServiceLocator.instance().getService(UserManagement.class)
                .getUserById(id, new Converter<User, Object>() {
                    @Override
                    public Object convert(User source) {
                        authorList.add(UserNameHelper.getDetailedUserSignature(source));
                        return null;
                    }
                });
            }
            filters.add(RtfElementFactory.createListItem(resourcesManager.getText(
                    "export.postlist.filter.authors", user.getLanguageLocale(),
                    StringUtils.join(authorList, ", "))));
        }

        handleTagsFilter(queryParameters.getLogicalTags(), queryParameters.getTagIds(),
                user.getLanguageLocale(), resourcesManager, filters);
        handleUserSearchFilter(queryParameters.getUserSearchFilters(), user.getLanguageLocale(),
                resourcesManager, filters);

        if (queryParameters.getFullTextSearchFilters() != null) {
            filters.add(RtfElementFactory.createListItem(resourcesManager.getText(
                    "export.postlist.filter.text", user.getLanguageLocale(),
                    StringUtils.join(queryParameters.getFullTextSearchFilters(), " "))));
        }
        if (filters.size() == 0) {
            Font italic = new Font();
            italic.setSize(10);
            italic.setStyle(Font.ITALIC);
            Element empty = RtfElementFactory.createListItem(
                    resourcesManager.getText("export.postlist.filter.none",
                            user.getLanguageLocale()), italic);
            filters.add(empty);
        }
        return filters;
    }

    /**
     * This message creates the footer element for the exported document.
     *
     * @param queryInstance
     *            The query instance to extract needed data from.
     * @param user
     *            The user.
     * @param resourcesManager
     *            The resources manager to retreive resources from.
     * @return An object to be used as a header.
     * @throws MalformedURLException
     *             {@link MalformedURLException}.
     * @throws BadElementException
     *             {@link BadElementException}.
     */
    private Element getFooter(NoteQueryParameters queryInstance, User user,
            ResourceBundleManager resourcesManager) throws MalformedURLException,
            BadElementException {
        Table table = new Table(2);
        table.setWidths(new float[] { 60, 40 });
        table.setWidth(100);
        table.setPadding(5);
        table.setBorder(Table.TOP);
        Cell serviceCell = new Cell();
        serviceCell.setBorder(Cell.TOP);
        serviceCell.add(RtfElementFactory.createChunk(
                resourcesManager.getText("export.post.footer.service", user.getLanguageLocale())
                + " ", null));
        serviceCell.add(RtfElementFactory.createChunk(resourcesManager.getText(
                "export.post.footer.service.provider", user.getLanguageLocale())));
        Cell pageNumberCell = new Cell();
        pageNumberCell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
        pageNumberCell.setBorder(Cell.TOP);
        pageNumberCell.add(RtfElementFactory.createChunk(resourcesManager.getText(
                "export.post.footer.page", user.getLanguageLocale()) + " "));
        pageNumberCell.add(new RtfPageNumber());
        pageNumberCell
        .add(RtfElementFactory.createChunk(" "
                + resourcesManager.getText("export.post.footer.of",
                        user.getLanguageLocale()) + " "));
        pageNumberCell.add(new RtfTotalPageNumber());

        table.addCell(serviceCell);
        table.addCell(pageNumberCell);
        return table;
    }

    /**
     * This message creates the header element for the exported document.
     *
     * @param queryInstance
     *            The query instance to extract needed data from.
     * @param dateFormatter
     *            The date formater.
     * @param resourcesManager
     *            The resources manager to retreive resources from.
     * @throws BadElementException
     *             {@link BadElementException}
     * @throws IOException
     *             {@link IOException}
     * @return An object to be used as a header.
     */
    private Element getHeader(NoteQueryParameters queryInstance, DateFormat dateFormatter,
            ResourceBundleManager resourcesManager) throws BadElementException, IOException {
        Table table = new Table(2);
        table.setWidth(100);
        table.setWidths(new float[] { 60, 40 });
        table.setPadding(5);
        table.setBorder(Table.BOTTOM);

        String client = ClientHelper.getCurrentClient().getName();

        Cell clientCell = new Cell(RtfElementFactory.createChunk(client));
        clientCell.setBorder(Cell.NO_BORDER);

        Cell clientImageCell = new Cell();
        Image clientImage;
        try {
            clientImage = ServiceLocator.findService(ImageManager.class)
                    .getImage(
                            ClientImageDescriptor.IMAGE_TYPE_NAME,
                            new ImageDescriptor(new ImageSize(200, 150), ClientHelper
                                    .getCurrentClientId()));
            Element image = RtfElementFactory.createImage(clientImage.getBytes(), 200, 150);
            clientImageCell.add(image);
        } catch (AuthorizationException e) {
            LOG.error(e.getMessage());
        } catch (ImageNotFoundException e) {
            LOG.error(e.getMessage());
        }
        clientImageCell.setBorder(Cell.BOTTOM);
        clientImageCell.setRowspan(2);
        clientImageCell.setHorizontalAlignment(Cell.ALIGN_RIGHT);

        User currentUser = SecurityHelper.assertCurrentKenmeiUser();
        String fromText = resourcesManager.getText("export.post.header.by",
                currentUser.getLanguageLocale(),
                UserNameHelper.getDetailedUserSignature(currentUser),
                dateFormatter.format(new Date()));
        Cell infoCell = new Cell(RtfElementFactory.createChunk(fromText));
        infoCell.setBorder(Cell.BOTTOM);

        table.addCell(clientCell);
        table.addCell(clientImageCell);
        table.addCell(infoCell);

        return table;
    }

    /**
     * This method creates the information cell.
     *
     * @param post
     *            VO holding all information about the note to process ======= The note.
     * @param dateFormatter
     *            Date formatter.
     * @param resourcesManager
     *            the resource bundle manager to use for localization
     * @param locale
     *            The locale to use.
     * @return The information cell as {@link Cell}.
     */
    private Cell getInfoCell(NoteData post, Locale locale, DateFormat dateFormatter,
            ResourceBundleManager resourcesManager) {
        Paragraph paragraph = new Paragraph();
        paragraph.setLeading(10);
        paragraph.setFont(FONT_META_INFORMATION);
        paragraph.add(RtfElementFactory.createChunk("\n", FONT_META_INFORMATION));
        paragraph
        .add(RtfElementFactory.createChunk(
                resourcesManager.getText("export.post.title.author", locale),
                FONT_META_INFORMATION));
        paragraph.add(RtfElementFactory.createChunk(":\t" + post.getUser().getFirstName() + " "
                + post.getUser().getLastName() + " (" + post.getUser().getAlias() + ")",
                FONT_META_INFORMATION));
        handleUsersToBeNotified(post.getNotifiedUsers(), paragraph, locale, resourcesManager);
        paragraph.add(RtfElementFactory.createChunk("\n", FONT_META_INFORMATION));
        paragraph.add(RtfElementFactory.createChunk(
                resourcesManager.getText("export.post.title.blog", locale), FONT_META_INFORMATION));
        paragraph.add(RtfElementFactory.createChunk(":\t" + post.getBlog().getTitle(),
                FONT_META_INFORMATION));
        handleTags(post.getTags(), locale, resourcesManager, paragraph);
        handleParentNote(post, locale, dateFormatter, resourcesManager, paragraph);
        handleModificationDate(post, locale, dateFormatter, resourcesManager, paragraph);
        Cell infoCell = new Cell();
        infoCell.setBorder(Cell.BOTTOM | Cell.RIGHT);
        infoCell.add(paragraph);
        return infoCell;
    }

    /**
     * Returns a specific post as {@link Element}.
     *
     * @param note
     *            The note.
     * @param user
     *            The user.
     * @param fullDateFormatter
     *            Formatter for full dates.
     * @param dateFormatter
     *            Formatter for short dates.
     * @param resourcesManager
     *            {@link ResourceBundleManager}.
     * @return The post as {@link Element}.
     * @throws BadElementException
     *             Exception.
     */
    private Element getPost(NoteData note, User user, DateFormat dateFormatter,
            DateFormat fullDateFormatter, ResourceBundleManager resourcesManager)
                    throws BadElementException {
        Table table = new Table(2);
        table.setWidth(100);
        table.setUseVariableBorders(true);
        table.setWidths(new float[] { 20, 80 });
        table.setPadding(5);
        table.setBorder(Table.NO_BORDER);
        Cell dateCell = new Cell();
        dateCell.setRowspan(2);
        dateCell.setHorizontalAlignment(Cell.ALIGN_CENTER);
        dateCell.add(RtfElementFactory.createChunk(dateFormatter.format(note.getCreationDate())));
        dateCell.add(RtfElementFactory.createChunk("\n"));
        try {

            Image userImage = null;
            try {
                userImage = ServiceLocator.findService(ImageManager.class).getImage(
                        UserImageDescriptor.IMAGE_TYPE_NAME, note.getUser().getId().toString(),
                        ImageSizeType.LARGE);
                Element image = RtfElementFactory.createImage(userImage.getBytes(), 100, 100);
                dateCell.add(image);
            } catch (AuthorizationException e) {
                LOG.error(e.getMessage());
            } catch (ImageNotFoundException e) {
                LOG.error(e.getMessage());
            }
            if (note.isDirect()) {
                dateCell.setBackgroundColor(new Color(251, 229, 205));
                dateCell.add(RtfElementFactory.createChunk(
                        "\n"
                                + resourcesManager.getText("export.post.title.direct",
                                        user.getLanguageLocale()), FONT_META_INFORMATION));
            }
        } catch (BadElementException | IOException e) {
            LOG.debug("Problem adding user image: {}", e.getMessage());
        }

        Cell contentCell = new Cell();
        contentCell.setBorder(Cell.TOP | Cell.RIGHT);
        String content = note.getContent();
        contentCell.add(RtfElementFactory.createChunk(content));
        Cell infoCell = getInfoCell(note, user.getLanguageLocale(), dateFormatter, resourcesManager);
        table.addCell(dateCell);
        table.addCell(contentCell);
        table.addCell(infoCell);
        Collection<Object> elements = new ArrayList<Object>();
        elements.add(table);
        return RtfElementFactory.createPhrase(elements);
    }

    /**
     * This method handles the filtered blogs.
     *
     * @param resourcesManager
     *            {@link ResourceBundleManager}.
     * @param locale
     *            The locale.
     * @param filters
     *            {@link List}.
     * @param utpExt
     *            {@link TaggingCoreItemUTPExtension}.
     */
    private void handleBlogFilter(ResourceBundleManager resourcesManager, Locale locale,
            List filters, TaggingCoreItemUTPExtension utpExt) {
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
                filters.add(RtfElementFactory.createListItem(resourcesManager.getText(
                        "export.postlist.filter.blog", locale, StringUtils.join(titles, ", "))));
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
     * @param filters
     *            Filters as {@link List}.
     */
    private void handleDatesFilter(NoteQueryParameters queryInstance, Locale locale,
            DateFormat dateFormatter, ResourceBundleManager resourcesManager, List filters) {
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
                            filters.add(RtfElementFactory.createListItem(resourcesManager.getText(
                                    "export.postlist.filter.date." + msgKeyPart, locale, (Object[]) dateArgs)));
                        }
    }

    /**
     * Adds information about the discussion to the filter summary, if there is a discussion.
     *
     * @param messages
     *            The messages.
     * @param locale
     *            Locale of the user.
     * @param dateFormatter
     *            Formatter.
     * @param discussionId
     *            The discussion id.
     * @param filters
     *            The filters.
     */
    private void handleDiscussionFilter(ResourceBundleManager messages, Locale locale,
            DateFormat dateFormatter, Long discussionId, List filters) {
        if (discussionId == null) {
            return;
        }
        try {
            // no need to pass a render mode and trigger the pre-processors because we are only
            // interested in the author
            NoteData note = ServiceLocator.instance().getService(NoteService.class)
                    .getNote(discussionId, new NoteRenderContext(null, locale));
            filters.add(RtfElementFactory.createListItem(messages.getText(
                    "export.postlist.filter.discussion", locale,
                    UserNameHelper.getUserSignature(note.getUser(), UserNameFormat.MEDIUM),
                    dateFormatter.format(note.getCreationDate()))));
        } catch (NoteNotFoundException e) {
            LOG.error(e.getMessage());
        } catch (AuthorizationException e) {
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
     *            Filters as {@link List}.
     */
    private void handleFollowFavoriteDirectOnly(NoteQueryParameters queryInstance, Locale locale,
            ResourceBundleManager resourcesManager, List filters) {
        if (queryInstance.isRetrieveOnlyFollowedItems()) {
            filters.add(RtfElementFactory.createListItem(resourcesManager.getText(
                    "export.postlist.filter.follow", locale)));
        }
        if (queryInstance.isFavorites()) {
            filters.add(RtfElementFactory.createListItem(resourcesManager.getText(
                    "export.postlist.filter.favorite", locale)));
        }
        if (queryInstance.isDirectMessage()) {
            filters.add(RtfElementFactory.createListItem(resourcesManager.getText(
                    "export.postlist.filter.direct.only", locale)));
        }
    }

    /**
     * Checks for a last modification date.
     *
     * @param post
     *            The post.
     * @param locale
     *            The locale.
     * @param dateFormatter
     *            Date formater.
     * @param resourcesManager
     *            The used ResourceManager.
     * @param phrase
     *            The phrase to add the information.
     */
    private void handleModificationDate(NoteData post, Locale locale, DateFormat dateFormatter,
            ResourceBundleManager resourcesManager, Phrase phrase) {
        if (post.getLastModificationDate() != null
                && !post.getLastModificationDate().equals(post.getCreationDate())) {
            phrase.add(RtfElementFactory.createChunk("\n", FONT_META_INFORMATION));
            phrase.add(RtfElementFactory.createChunk(
                    resourcesManager.getText("export.post.title.modified", locale) + ": ",
                    FONT_META_INFORMATION));
            phrase.add(RtfElementFactory.createChunk(
                    dateFormatter.format(post.getLastModificationDate()), FONT_META_INFORMATION));
        }
    }

    /**
     * Checks if there is a parent post.
     *
     * @param post
     *            The post.
     * @param locale
     *            The locale.
     * @param dateFormatter
     *            Date formatter.
     * @param resourcesManager
     *            The used ResourceManager.
     *
     * @param phrase
     *            The phrase to add the information.
     */
    private void handleParentNote(NoteData post, Locale locale, DateFormat dateFormatter,
            ResourceBundleManager resourcesManager, Phrase phrase) {
        if (post.getParent() == null) {
            return;
        }
        phrase.add(RtfElementFactory.createChunk("\n", FONT_META_INFORMATION));
        phrase.add(RtfElementFactory.createChunk(
                resourcesManager.getText("export.post.title.reply.prefix", locale) + ": ",
                FONT_META_INFORMATION));
        String fromAt = resourcesManager.getText("blog.post.list.reply.link", locale, post
                .getParent().getUser().getFirstName()
                + " "
                + post.getParent().getUser().getLastName()
                + " ("
                + post.getParent().getUser().getAlias() + ")",
                dateFormatter.format(post.getParent().getCreationDate()));
        phrase.add(RtfElementFactory.createChunk(fromAt, FONT_META_INFORMATION));
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
     *            Filters as {@link List}.
     * @param utpExt
     *            {@link TaggingCoreItemUTPExtension}.
     * @throws NoteNotFoundException
     *             exception.
     * @throws AuthorizationException
     *             Exception.
     */
    private void handleParentPostFilter(ResourceBundleManager resourcesManager,
            DateFormat dateFormatter, Locale locale, List filters,
            TaggingCoreItemUTPExtension utpExt) throws NoteNotFoundException,
            AuthorizationException {
        if (utpExt.getParentPostId() == null) {
            return;
        }
        // no need to pass a render mode and trigger the pre-processors because we are only
        // interested in the author
        NoteData noteData = ServiceLocator.instance().getService(NoteService.class)
                .getNote(utpExt.getParentPostId(), new NoteRenderContext(null, locale));
        if (noteData != null) {
            filters.add(RtfElementFactory.createListItem(resourcesManager.getText(
                    "export.postlist.filter.parentpost", locale,
                    UserNameHelper.getDetailedUserSignature(noteData.getUser()),
                    dateFormatter.format(noteData.getCreationDate().getTime()))));
        }
    }

    /**
     * Checks the Tags
     *
     * @param tags
     *            List containing the tags.
     * @param locale
     *            The locale.
     * @param resourcesManager
     *            The used ResourceManager.
     * @param phrase
     *            The phrase to add the information.
     */
    private void handleTags(Collection<TagData> tags, Locale locale,
            ResourceBundleManager resourcesManager, Phrase phrase) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        ArrayList<String> tagsAsStringArray = new ArrayList<String>();
        for (TagData tag : tags) {
            tagsAsStringArray.add(tag.getName());
        }
        phrase.add(RtfElementFactory.createChunk("\n", FONT_META_INFORMATION));
        phrase.add(RtfElementFactory.createChunk(
                resourcesManager.getText("export.post.title.tags", locale) + ": "
                        + StringUtils.join(tagsAsStringArray, ","), FONT_META_INFORMATION));
    }

    /**
     * This method handles the set tags.
     *
     * @param tagFormula
     *            the tag formula
     * @param tagIds
     *            Id's of filtered tags.
     * @param locale
     *            The locale.
     * @param resourcesManager
     *            The {@link ResourceBundleManager}.
     * @param filters
     *            The filters to be added to.
     */
    private void handleTagsFilter(LogicalTagFormula tagFormula, Set<Long> tagIds, Locale locale,
            ResourceBundleManager resourcesManager, List filters) {
        String tags = FilterParameterResolver.getInstance().resolveTags(tagFormula);
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
            filters.add(RtfElementFactory.createListItem(resourcesManager.getText(
                    "export.postlist.filter.tags", locale, tags)));
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
     * @param filters
     *            The list of filters.
     */
    private void handleUserSearchFilter(String[] userSearchFilters, Locale locale,
            ResourceBundleManager resourcesManager, List filters) {
        if (!ArrayUtils.isEmpty(userSearchFilters)) {
            filters.add(RtfElementFactory.createListItem(resourcesManager.getText(
                    "export.postlist.filter.usertext", locale,
                    StringUtils.join(userSearchFilters, ", "))));
        }
    }

    /**
     * This method handles the users to be notified filter string.
     *
     * @param resourcesManager
     *            The {@link ResourceBundleManager}.
     * @param locale
     *            The locale.
     * @param filters
     *            The list of filters.
     * @param usersToBeNotifiedAsLong
     *            The list of users.
     */
    private void handleUsersFilter(ResourceBundleManager resourcesManager, Locale locale,
            List filters, Long[] usersToBeNotifiedAsLong) {
        if (!ArrayUtils.isEmpty(usersToBeNotifiedAsLong)) {
            final Collection<String> usersToBeNotified = new ArrayList<String>();
            for (Long id : usersToBeNotifiedAsLong) {
                ServiceLocator.findService(UserManagement.class).getUserById(id,
                        new Converter<User, Object>() {
                    @Override
                    public Object convert(User source) {
                        usersToBeNotified.add(UserNameHelper
                                .getDetailedUserSignature(source));
                        return null;
                    }
                });
            }
            if (!usersToBeNotified.isEmpty()) {
                filters.add(RtfElementFactory.createListItem(resourcesManager.getText(
                        "export.postlist.filter.usernotify", locale,
                        StringUtils.join(usersToBeNotified, ", "))));
            }
        }
    }

    /**
     * Handles the users to be notified of each post.
     *
     * @param list
     *            The post.
     * @param resourcesManager
     *            The used ResourceManager.
     *
     * @param phrase
     *            The phrase to add the information.
     * @param locale
     *            The locale.
     */
    private void handleUsersToBeNotified(Collection<DetailedUserData> list, Phrase phrase,
            Locale locale, ResourceBundleManager resourcesManager) {
        if (list == null || list.isEmpty()) {
            return;
        }
        phrase.add(RtfElementFactory.createChunk("\n", FONT_META_INFORMATION));
        phrase.add(RtfElementFactory.createChunk(
                resourcesManager.getText("export.post.title.users", locale) + ":\t",
                FONT_META_INFORMATION));
        String prefix = "";
        for (DetailedUserData user : list) {
            phrase.add(RtfElementFactory.createChunk(
                    prefix + user.getFirstName() + " " + user.getLastName() + " ("
                            + user.getAlias() + ")", FONT_META_INFORMATION));
            prefix = ", ";
        }
    }

    /**
     * @return <code>true</code>
     */
    @Override
    public boolean isAttachment() {
        return true;
    }

    @Override
    public boolean supportsHtmlContent() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean valid(String exportFormat) {
        return exportFormat != null && RTF.equals(exportFormat.toLowerCase());
    }

    /**
     * Export to RTF.
     *
     * {@inheritDoc}
     */
    @Override
    public void write(NoteQueryParameters queryInstance, OutputStream outputStream,
            Collection<NoteData> notes, String requestUrl) throws NoteWriterException {
        Document document = new Document();
        RtfWriter2.getInstance(document, outputStream);
        ResourceBundleManager resourcesManager = ResourceBundleManager.instance();
        document.open();
        try {
            User user = SecurityHelper.assertCurrentKenmeiUser();
            DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                    DateFormat.SHORT, user.getLanguageLocale());
            dateFormatter.setTimeZone(UserManagementHelper.getEffectiveUserTimeZone());
            DateFormat fullDateFormatter = DateFormat.getDateTimeInstance(DateFormat.FULL,
                    DateFormat.FULL, user.getLanguageLocale());
            fullDateFormatter.setTimeZone(UserManagementHelper.getEffectiveUserTimeZone());
            Element header = getHeader(queryInstance, dateFormatter, resourcesManager);
            document.setHeader(new RtfHeaderFooter(header));
            Element footer = getFooter(queryInstance, user, resourcesManager);
            document.setFooter(new RtfHeaderFooter(footer));
            Element description = getDescription(queryInstance, user, dateFormatter,
                    fullDateFormatter, resourcesManager);
            document.add(description);
            for (NoteData note : notes) {
                document.add(getPost(note, user, dateFormatter, fullDateFormatter, resourcesManager));
            }
            outputStream.flush();
        } catch (BadElementException e) {
            throw new NoteWriterException(
                    "Error writing to output stream(" + e.getMessage() + ").", e);
        } catch (IOException e) {
            throw new NoteWriterException(
                    "Error writing to output stream(" + e.getMessage() + ").", e);
        } catch (DocumentException e) {
            throw new NoteWriterException(
                    "Error writing to output stream(" + e.getMessage() + ").", e);
        } catch (NoteNotFoundException e) {
            throw new NoteWriterException(
                    "Error writing to output stream(" + e.getMessage() + ").", e);
        } catch (AuthorizationException e) {
            throw new NoteWriterException(
                    "Error writing to output stream(" + e.getMessage() + ").", e);
        } finally {
            document.close();
        }
    }
}
