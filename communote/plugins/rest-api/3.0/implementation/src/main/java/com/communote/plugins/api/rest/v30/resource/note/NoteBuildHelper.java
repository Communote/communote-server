package com.communote.plugins.api.rest.v30.resource.note;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import com.communote.plugins.api.rest.v30.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v30.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v30.resource.ResourceHandlerHelper;
import com.communote.plugins.api.rest.v30.resource.note.property.PropertyResourceHelper;
import com.communote.plugins.api.rest.v30.response.ResponseHelper;
import com.communote.plugins.api.rest.v30.to.ApiResult.ResultStatus;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.lastmodifieddate.LastModificationDateManagement;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.model.user.UserProfile;
import com.communote.server.persistence.blog.CreateBlogPostHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteBuildHelper {

    /**
     * Create a resource object for a userListItem to send back only the values specified by the
     * webservice definition
     *
     * @param note
     *            is a note object that contains also user/author specific data
     * @return a noteResourceInstance that will be used to send back to the requester
     * @throws NoteNotFoundException
     *             in case the note was removed after filling the NoteData object
     * @throws AuthorizationException
     *             in case the access rights to the topic/note were changed after filling the
     *             NoteData object
     */
    public static NoteResource buildNoteResource(NoteData note) throws AuthorizationException,
    NoteNotFoundException {
        NoteResource noteResource = new NoteResource();
        noteResource.setTopicId(note.getBlog().getId());
        noteResource.setTopicName(note.getBlog().getTitle());

        noteResource.setDiscussionId(note.getDiscussionId());
        noteResource.setDiscussionDepth(note.getDiscussionDepth());

        noteResource.setLastModificationDate(note.getLastModificationDate());
        noteResource.setCreationDate(note.getCreationDate());
        noteResource.setNoteId(note.getId());
        noteResource.setNumberOfComments(note.getNumberOfComments());
        noteResource.setIsMentionDiscussionAuthors(note.isMentionDiscussionAuthors());
        noteResource.setIsMentionTopicAuthors(note.isMentionTopicAuthors());
        noteResource.setIsMentionTopicManagers(note.isMentionTopicManagers());
        noteResource.setIsMentionTopicReaders(note.isMentionTopicReaders());

        if (note.getParent() != null) {
            noteResource.setParentNoteId(note.getParent().getId());
        }

        long userId = note.getUser().getId();
        UserProfile user = ServiceLocator.findService(UserProfileManagement.class)
                .findUserProfileByUserId(userId);
        noteResource.setUserId(userId);
        noteResource.setUserName(note.getUser().getAlias());
        noteResource.setFirstName(user.getFirstName());
        noteResource.setLastName(user.getLastName());
        noteResource.setIsDirectMessage(note.isDirect());
        noteResource.setIsFavorite(note.isFavorite());
        noteResource.setIsLike(Boolean.TRUE.equals(note.getProperty("currentUserLikesNote")));

        NoteResourceHelper.buildNoteResourceNotifications(note, noteResource);

        noteResource.setNoteVersion(note.getVersion());

        FollowManagement followManagement = ServiceLocator.instance().getService(
                FollowManagement.class);
        List<String> followedItemsList = new ArrayList<String>();
        if (followManagement.followsUser(note.getUser().getId())) {
            followedItemsList.add("author");
        }
        if (followManagement.followsBlog(note.getBlog().getId())) {
            followedItemsList.add("blog");
        }
        if (followManagement.followsDiscussion(note.getDiscussionId())) {
            followedItemsList.add("discussion");
        }
        String[] followedItems = followedItemsList.toArray(new String[0]);
        noteResource.setFollowedItems(followedItems);
        NoteResourceHelper.buildNoteResourceAttachments(note, noteResource);

        noteResource.setTags(NoteResourceHelper.buildTags(note.getTags()));

        noteResource.setText(note.getContent());
        noteResource.setProperties(PropertyResourceHelper.convertToPropertyResources(note
                .getObjectProperties()));
        noteResource.setCrawlLastModificationDate(ServiceLocator.findService(
                LastModificationDateManagement.class)
                .getNoteCrawlLastModificationDate(note.getId()));
        return noteResource;
    }

    /**
     * Build the response of an note. Response status can be OK, WARNING or ERROR.
     *
     * @param request
     *            {@link Request}
     * @param result
     *            {@link NoteModificationStatus}
     * @return {@link Response}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    public static Response buildNoteResponse(Request request,
            NoteModificationResult result) throws ResponseBuildException,
            ExtensionNotSupportedException {
        String message = CreateBlogPostHelper.getFeedbackMessageAfterModification(result,
                ResourceHandlerHelper.getCurrentUserLocale(request));
        if (message == null || message.length() == 0) {
            return ResponseHelper.buildSuccessResponse(result.getNoteId(), request,
                    result.getMessageKey() == null ? "notify.success.note.create.message"
                            : result.getMessageKey());
        }
        result.setErrorCause(null);
        if (result.getStatus().equals(NoteModificationStatus.SUCCESS)) {
            return ResponseHelper.buildResponse(result.getNoteId(), message, request,
                    ResultStatus.WARNING);
        } else {
            return ResponseHelper.buildResponse(result, message, request, ResultStatus.ERROR,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Adds an list with attachment identifier to {@link NoteStoringTO}. The set of attachment
     * identifier gets this function from an specific session attribute. The key of the attribute is
     * the parameter of {@link NoteResource}.
     *
     * @param attribute
     *            of session
     * @param session
     *            {@link HttpSession}
     * @param noteStoringTO
     *            {@link NoteStoringTO}
     */
    @SuppressWarnings("unchecked")
    public static void setAttachmentsFromSession(String attribute, HttpSession session,
            NoteStoringTO noteStoringTO) {
        Set<Long> attachmentIds = (Set<Long>) session.getAttribute(attribute);
        if (attachmentIds == null) {
            attachmentIds = new HashSet<Long>();
            session.setAttribute(attribute, attachmentIds);
        }
        noteStoringTO.setAttachmentIds(attachmentIds.toArray(new Long[attachmentIds.size()]));
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private NoteBuildHelper() {
        // Do nothing
    }
}
