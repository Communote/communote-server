package com.communote.server.web.api.service.post;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.ServletRequestUtils;

import com.communote.common.util.HTMLHelper;
import com.communote.common.util.PageableList;
import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.ApplicationInformation;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.NoteManagementException;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.blog.OnlyCrosspostMarkupException;
import com.communote.server.core.blog.notes.DirectMessageConversionException;
import com.communote.server.core.blog.notes.ReplyNotDirectMessageException;
import com.communote.server.core.blog.notes.processors.exceptions.DirectMessageMissingRecipientException;
import com.communote.server.core.blog.notes.processors.exceptions.DirectMessageWrongRecipientForAnswerException;
import com.communote.server.core.blog.notes.processors.exceptions.MessageKeyNoteContentException;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.tag.TagParserFactory;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.persistence.blog.CreateBlogPostHelper;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.service.NoteService;
import com.communote.server.web.api.service.ApiException;
import com.communote.server.web.api.service.BaseRestApiController;
import com.communote.server.web.api.service.RequestedResourceNotFoundException;
import com.communote.server.web.api.service.post.convert.v1_0_1.ApiDetailNoteConverter;
import com.communote.server.web.api.to.ApiResult;
import com.communote.server.web.api.to.listitem.v1_0_1.DetailPostListItem;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.fe.portal.blog.helper.CreateBlogPostFeHelper;

/**
 * Controller for handling api request for posts.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead
 */
@Deprecated
public class PostApiController extends BaseRestApiController {

    private final static Logger LOGGER = LoggerFactory.getLogger(PostApiController.class);

    private static NoteQuery USER_TAGGED_POST_QUERY = QueryDefinitionRepository.instance()
            .getQueryDefinition(NoteQuery.class);

    private static final String PARAM_FILTER_HTML = "filterHtml";

    private NoteCreationSource creationSource;

    /**
     * Checks the min and max length of a note.
     *
     * @param request
     *            the request
     * @param text
     *            the content to check
     * @param type
     *            the content type
     * @throws ApiException
     *             in case the length requirements are not fulfilled
     */
    private void checkNoteLength(HttpServletRequest request, String text, NoteContentType type)
            throws ApiException {
        boolean noText;
        if (text != null && NoteContentType.HTML.equals(type)) {
            // check length of actual data
            noText = !HTMLHelper.containsNonEmptyTextNodes(text);
        } else {
            noText = StringUtils.isBlank(text);
        }
        if (noText) {
            throw new ApiException(MessageHelper.getText(request,
                    "error.blogpost.create.no.content"));
        }
        checkNoteMaxLength(request, text);
    }

    /**
     * Check the max length of the note content.
     *
     * @param request
     *            the request
     * @param postText
     *            the post content
     * @throws ApiException
     *             in case the length is exceeded
     */
    private void checkNoteMaxLength(HttpServletRequest request, String postText)
            throws ApiException {
        int postMaxLength = ServletRequestUtils.getIntParameter(request, "postMaxLength", 0);
        if (postMaxLength > 0 && postText.length() > postMaxLength) {
            String errorMessage = MessageHelper.getText(request,
                    "error.blogpost.content.max.length.exceeded",
                    new String[] { "" + postMaxLength });

            throw new ApiException(errorMessage);
        }
    }

    /**
     * Removes orphaned attachments that were previously uploaded but were not attached to the
     * created note.
     *
     * @param request
     *            the servlet request
     * @param attachmentIds
     *            the attachment IDs that were added to the note
     */
    private void cleanupAttachments(HttpServletRequest request, Long[] attachmentIds) {
        Set<Long> uploadedAttachments = CreateBlogPostFeHelper
                .getUploadedAttachmentsFromSession(request);
        if (uploadedAttachments != null) {
            // remove all IDs that were saved with note
            if (attachmentIds != null) {
                for (Long id : attachmentIds) {
                    uploadedAttachments.remove(id);
                }
            }
            try {
                ServiceLocator.findService(ResourceStoringManagement.class)
                .deleteOrphanedAttachments(uploadedAttachments);
            } catch (AuthorizationException e) {
                LOGGER.error(e.getMessage(), e);
            }
            CreateBlogPostFeHelper.removeUploadedAttachmentsFromSession(request);
        }
    }

    /**
     * Creates a JSON response object modeled by a map containing the ID of the created/modified
     * note, its version and a warning message if there is one.
     *
     * @param apiResult
     *            The Api result.
     * @param result
     *            the modification result
     * @param locale
     *            the user's locale
     * @return the object
     * @throws ApiException
     *             in case the JSON object cannot be created
     */
    private Map<String, Object> createSuccessResponse(ApiResult apiResult,
            NoteModificationResult result, Locale locale) throws ApiException {

        HashMap<String, Object> responseWrapper = new HashMap<String, Object>();
        String warningMessage = CreateBlogPostHelper.getFeedbackMessageAfterModification(result,
                locale);
        if (warningMessage != null) {
            // resp.put("warningMessage", warningMessage);
            apiResult.setMessage(warningMessage);
            apiResult.setStatus(ApiResult.ResultStatus.WARNING.name());
        } else {
            apiResult.setMessage(ResourceBundleManager.instance().getText(
                    "notify.success.note.create.message", locale));
            apiResult.setStatus(ApiResult.ResultStatus.OK.name());
            // resp.put("successMessage",
            // ResourceBundleManager.instance().getText(
            // "notify.success.note.create.message", locale));
        }
        responseWrapper.put("version", result.getVersion());
        responseWrapper.put("noteId", result.getNoteId());
        return responseWrapper;

    }

    /**
     * Do the get on a resource
     *
     * @param apiResult
     *            the api result for returning additional values
     * @param request
     *            the request
     * @param response
     *            the response
     * @return the post resource
     * @throws RequestedResourceNotFoundException
     *             the resource has not been found
     */
    @Override
    protected Object doGet(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws RequestedResourceNotFoundException {
        Long postId = getResourceId(request, true);
        boolean filterHtml = ParameterHelper.getParameterAsBoolean(request.getParameterMap(),
                PARAM_FILTER_HTML, Boolean.FALSE);

        NoteQueryParameters queryParameters;
        QueryResultConverter noteConverter;
        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        // note: the rendering pre processor extension takes care of plain-text conversion but
        // works slightly differently because it uses another plain-text
        // conversion method (htmlToPlaintextExt) than this method was using. To get a
        // similar result we do not set the beautify option.
        NoteRenderContext renderContext = new NoteRenderContext(filterHtml ? NoteRenderMode.PLAIN
                : NoteRenderMode.HTML, locale);
        queryParameters = USER_TAGGED_POST_QUERY.createInstance();
        if (compareVersions(request, ApplicationInformation.V_1_0_1) >= 0) {
            noteConverter = new ApiDetailNoteConverter(renderContext);
        } else {
            noteConverter = new com.communote.server.web.api.service.post.convert.v1_0_0.ApiDetailNoteConverter(
                    renderContext);
        }
        queryParameters.setNoteId(postId);
        TaggingCoreItemUTPExtension extension = queryParameters.getTypeSpecificExtension();
        extension.setTopicAccessLevel(TopicAccessLevel.READ);
        extension.setUserId(SecurityHelper.getCurrentUserId());

        PageableList<? extends IdentifiableEntityData> list = ServiceLocator.findService(
                QueryManagement.class)
                .query(USER_TAGGED_POST_QUERY, queryParameters, noteConverter);
        if (list.size() == 0) {
            throw new RequestedResourceNotFoundException(getResourceType(), postId.toString(),
                    "Resource not found!");
        } else if (list.size() > 1) {
            LOGGER.warn("Requesting Post with id={} resulted in a list of size={}", postId,
                    list.size());
        }
        DetailPostListItem postItem = (DetailPostListItem) list.iterator().next();

        if (compareVersions(request, ApplicationInformation.V_1_1_4) < 0) {
            if (postItem.getParentPostId() != null) {
                postItem.setParentPostId(postItem.getPostId());
            }

        }
        return postItem;
    }

    /**
     * Do a creation or edit of a post resource
     *
     * @param apiResult
     *            the API result
     * @param request
     *            the request
     * @param response
     *            the response
     * @return warning messages or null
     * @throws ApiException
     *             in case of an error
     */
    @Override
    protected Object doPost(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws ApiException {
        if (compareVersions(request, ApplicationInformation.V_1_1) >= 0) {
            return handleCreateModifyNote(apiResult, request);
        }
        Long postId = getResourceId(request, false);
        NoteStoringTO postStoringTO = new NoteStoringTO();

        Long[] blogs = ParameterHelper
                .getParameterAsLongArray(request.getParameterMap(), "blogIds");
        if (blogs == null || blogs.length == 0) {
            throw new ApiException(
                    MessageHelper.getText(request, "error.blogpost.create.no.blogid"));
        }

        Long parentPostId = ParameterHelper.getParameterAsLong(request.getParameterMap(),
                "parentPostId");

        String postText = request.getParameter("postText");
        if (StringUtils.isBlank(postText)) {
            throw new ApiException(MessageHelper.getText(request,
                    "error.blogpost.create.no.content"));
        }

        String tags = extractTags(request);

        Set<String> crossPostBlogAliasesSet = ParameterHelper.getParameterAsStringSet(
                request.getParameterMap(), "crossPostBlogAliases", ",");
        Set<String> notifyUserIds = ParameterHelper.getParameterAsStringSet(
                request.getParameterMap(), "notifyUserIds", ",");

        checkNoteMaxLength(request, postText);

        postStoringTO.setContentType(extractContentType(request));
        postStoringTO.setContent(postText);

        postStoringTO.setBlogId(blogs[0]);
        postStoringTO.setCreatorId(SecurityHelper.getCurrentUserId());
        postStoringTO.setCreationSource(this.creationSource);
        postStoringTO.setUsersToNotify(notifyUserIds);
        postStoringTO.setUnparsedTags(tags);
        postStoringTO.setPublish(true);
        postStoringTO.setVersion(0L);
        CreateBlogPostHelper.setDefaultFailLevel(postStoringTO);
        postStoringTO.setSendNotifications(true);
        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        postStoringTO.setLanguage(locale.getLanguage());

        // TODO attachments
        NoteModificationResult result = handlePosting(request, postStoringTO, postId, parentPostId,
                crossPostBlogAliasesSet, locale);
        return CreateBlogPostHelper.getFeedbackMessageAfterModification(result, locale);
    }

    /**
     * Returns the content type of the note.
     *
     * @param request
     *            the request
     * @return the content type
     */
    private NoteContentType extractContentType(HttpServletRequest request) {
        Boolean isHtml = ParameterHelper.getParameterAsBoolean(request.getParameterMap(), "isHtml");
        NoteContentType t;
        if (isHtml == null) {
            t = NoteContentType.UNKNOWN;
        } else if (isHtml) {
            t = NoteContentType.HTML;
        } else {
            t = NoteContentType.PLAIN_TEXT;
        }
        return t;
    }

    /**
     * Extracts the tags and default tags from the request.
     *
     * @param request
     *            the request object
     * @return the combined tag string
     * @throws ApiException
     *             in case the tags contain illegal characters
     */
    private String extractTags(HttpServletRequest request) throws ApiException {
        String tags = ParameterHelper.getParameterAsString(request.getParameterMap(), "tags");

        // default tags to allow the user to add must have tags easily
        String defaultTags = ParameterHelper.getParameterAsString(request.getParameterMap(),
                "defaultTags");
        if (StringUtils.isNotBlank(defaultTags)) {
            tags = TagParserFactory.instance().getDefaultTagParser().combineTags(tags, defaultTags);
        }
        if (StringUtils.contains(tags, '"') || StringUtils.contains(tags, '\'')) {
            throw new ApiException(MessageHelper.getText(request,
                    "error.blogpost.tags.unsupported.characters"));
        }
        return tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getResourceType() {
        return "post";
    }

    /**
     * Creates or edits a note or reply.
     *
     * @param apiResult
     *            the API result
     * @param request
     *            the request
     * @return a JSON object string describing the result. The object contains an optional warning
     *         message, the version of the note and the noteId.
     * @throws ApiException
     *             in case note creation failed
     */
    private Object handleCreateModifyNote(ApiResult apiResult, HttpServletRequest request)
            throws ApiException {
        Long targetBlog = ParameterHelper.getParameterAsLong(request.getParameterMap(),
                "targetBlogId");
        if (targetBlog == null) {
            throw new ApiException(MessageHelper.getText(request, "error.blogpost.blog.not.set"));
        }
        Long version = ServletRequestUtils.getLongParameter(request, "version", 0L);
        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        String tags = extractTags(request);
        Set<String> crossPostBlogAliasesSet = ParameterHelper.getParameterAsStringSet(
                request.getParameterMap(), "crossPostBlogAliases", ",");
        Set<String> notifyUserAliases = ParameterHelper.getParameterAsStringSet(
                request.getParameterMap(), "usersToNotify", ",");
        Long[] attachmentIds = ParameterHelper.getParameterAsLongArray(request.getParameterMap(),
                "attachmentIds");
        Long parentNoteId = ParameterHelper.getParameterAsLong(request.getParameterMap(),
                "parentPostId");
        Long noteId = ParameterHelper.getParameterAsLong(request.getParameterMap(), "postId");
        Long autosaveNoteId = ParameterHelper.getParameterAsLong(request.getParameterMap(),
                "autosaveNoteId");
        boolean publish = ParameterHelper.getParameterAsBoolean(request.getParameterMap(),
                "publish", true);

        NoteContentType contentType = extractContentType(request);
        String postText = request.getParameter("postText");
        checkNoteLength(request, postText, contentType);

        NoteStoringTO noteStoringTO = new NoteStoringTO();
        noteStoringTO.setCreationSource(this.creationSource);
        CreateBlogPostHelper.setDefaultFailLevel(noteStoringTO);
        noteStoringTO.setPublish(publish);
        noteStoringTO.setAutosaveNoteId(autosaveNoteId);
        noteStoringTO.setVersion(version);
        noteStoringTO.setContent(postText);
        noteStoringTO.setContentType(contentType);
        noteStoringTO.setBlogId(targetBlog);
        noteStoringTO.setUnparsedTags(tags);
        noteStoringTO.setUsersToNotify(notifyUserAliases);
        noteStoringTO.setAttachmentIds(attachmentIds);
        noteStoringTO.setCreatorId(SecurityHelper.getCurrentUserId());
        noteStoringTO.setSendNotifications(true);
        noteStoringTO.setIsDirectMessage(ServletRequestUtils.getBooleanParameter(request,
                "isDirectMessage", false));

        noteStoringTO.setLanguage(locale.getLanguage());

        NoteModificationResult result = handlePosting(request, noteStoringTO, noteId, parentNoteId,
                crossPostBlogAliasesSet, locale);
        if (publish) {
            cleanupAttachments(request, attachmentIds);
        }
        return createSuccessResponse(apiResult, result, locale);
    }

    /**
     * Executes the note/reply creation or update.
     *
     * @param request
     *            the request
     * @param noteStoringTO
     *            the TO holding the data for the note modification
     * @param noteId
     *            the ID of the note to edit, can be null if not an edit
     * @param parentNoteId
     *            the ID of the parentNote in case of a reply, can be null if not a reply
     * @param crossPostBlogAliasesSet
     *            optional set of crosspost blog aliases
     * @param locale
     *            the user locale for error message localization
     * @return the note modification result on success otherwise an exception will be thrown
     * @throws ApiException
     *             in case an error occurred
     */
    private NoteModificationResult handlePosting(HttpServletRequest request,
            NoteStoringTO noteStoringTO, Long noteId, Long parentNoteId,
            Set<String> crossPostBlogAliasesSet, Locale locale) throws ApiException {
        String errorMessage = null;
        NoteModificationResult result = null;
        NoteService noteManagement = ServiceLocator.instance().getService(NoteService.class);
        try {
            if (noteId == null) {
                noteStoringTO.setParentNoteId(parentNoteId);
                result = noteManagement.createNote(noteStoringTO, crossPostBlogAliasesSet);
            } else {
                result = noteManagement.updateNote(noteStoringTO, noteId, crossPostBlogAliasesSet,
                        true);
            }
            if (!result.getStatus().equals(NoteModificationStatus.SUCCESS)) {
                errorMessage = CreateBlogPostHelper.getFeedbackMessageAfterModification(result,
                        locale);
            }
        } catch (OnlyCrosspostMarkupException e) {
            errorMessage = MessageHelper.getText(request, "error.blogpost.create.no.real.content");
        } catch (BlogNotFoundException e) {
            String[] failedBlog = new String[1];
            failedBlog[0] = e.getBlogNameId() != null ? e.getBlogNameId() : e.getBlogId()
                    .toString();
            errorMessage = MessageHelper.getText(request, "error.blogpost.blog.not.found",
                    failedBlog);
        } catch (NoteManagementAuthorizationException e) {
            errorMessage = MessageHelper.getText(request, "error.blogpost.blog.no.write.access",
                    new String[] { e.getBlogTitle() });
        } catch (MessageKeyNoteContentException e) {
            errorMessage = MessageHelper.getText(request, e.getMessageKey(), e.getParameters());
        } catch (ReplyNotDirectMessageException e) {
            errorMessage = MessageHelper.getText(request, "error.blogpost.create.reply.not.direct");
        } catch (DirectMessageMissingRecipientException e) {
            errorMessage = e.getLocalizedMessage(locale);
        } catch (DirectMessageWrongRecipientForAnswerException e) {
            errorMessage = MessageHelper.getText(request,
                    "error.blogpost.blog.content.processing.failed.direct.wrong.recipient");
        } catch (DirectMessageConversionException e) {
            errorMessage = MessageHelper.getText(request,
                    "error.blogpost.convert.note.to.direct.failed");
        } catch (NoteStoringPreProcessorException e) {
            errorMessage = MessageHelper.getText(request,
                    "error.blogpost.blog.content.processing.failed");
        } catch (NoteNotFoundException e) {
            errorMessage = MessageHelper.getText(request, "error.blogpost.not.found");
        } catch (NoteManagementException e) {
            LOGGER.error("Error creating post: {}", e.getMessage(), e);
            errorMessage = MessageHelper.getText(request, "error.blogpost.create.failed");
        }

        if (errorMessage != null) {
            throw new ApiException(errorMessage);
        }
        return result;
    }

    /**
     * Set the creation source to be used by the controller.
     *
     * @param cs
     *            the creation source
     */
    public void setCreationSource(NoteCreationSource cs) {
        this.creationSource = cs;
    }
}
