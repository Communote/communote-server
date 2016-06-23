package com.communote.plugins.api.rest.v22.resource.timelinenote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import com.communote.plugins.api.rest.v22.resource.RestApiMultivaluedMapWrapper;
import com.communote.plugins.api.rest.v22.resource.attachment.AttachmentResourceHelper;
import com.communote.plugins.api.rest.v22.resource.timelinenote.property.PropertyResource;
import com.communote.plugins.api.rest.v22.resource.timelinenote.timelinenotediscussion.TimelineNoteDiscussionResource;
import com.communote.plugins.api.rest.v22.resource.timelinenote.timelinenotetag.TimelineNoteTagResource;
import com.communote.plugins.api.rest.v22.resource.timelinetopic.TimelineTopicResource;
import com.communote.plugins.api.rest.v22.resource.timelineuser.TimelineUserHelper;
import com.communote.plugins.api.rest.v22.resource.timelineuser.TimelineUserResource;
import com.communote.server.api.core.blog.UserBlogData;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.user.UserData;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class TimelineNoteHelper {

    private final static Collection<String> COMMA_SEPERATED_PARAMETERS = new ArrayList<String>();

    static {
        COMMA_SEPERATED_PARAMETERS.add("f_userIds");
        COMMA_SEPERATED_PARAMETERS.add("f_tagIds");
    }

    /**
     * Builds the rights of the note.
     * 
     * @param noteListData
     *            The note.
     * @return List of rights for this note.
     */
    private static List<ERights> buildNoteRights(NoteData noteListData) {
        List<ERights> rights = new ArrayList<ERights>();
        if (noteListData.isEditable()) {
            rights.add(ERights.CAN_EDIT);
        }
        if (noteListData.isCommentable()) {
            rights.add(ERights.CAN_REPLY);
        }
        if (noteListData.isDeletable()) {
            rights.add(ERights.CAN_DELETE);
        }
        return rights;
    }

    /**
     * converts a note list data object into a note resource object
     * 
     * @param noteListData
     *            the source object to convert
     * @param target
     *            the target object to fill
     * @return the filled item (target)
     */
    public static TimelineNoteResource buildTimelineNoteResource(NoteData noteListData,
            TimelineNoteResource target) {
        target.setNoteId(noteListData.getId());
        target.setText(noteListData.getContent());
        target.setShortText(noteListData.getShortContent());
        target.setNumberOfChildNotes(noteListData.getNumberOfComments());
        target.setIsDirectMessage(noteListData.isDirect());
        target.setDiscussion(TimelineNoteHelper.getDiscussion(noteListData));
        target.setCreationDate(noteListData.getCreationDate());
        if (noteListData.getParent() != null) {
            target.setParentNoteId(noteListData.getParent().getId());
        }

        Collection<UserData> likers = noteListData.getProperty("usersWhichLikeThisPost");
        target.setNumberOfLikes(likers == null ? 0 : likers.size());

        List<EUserNoteProperties> userNoteProperties = new ArrayList<EUserNoteProperties>();
        if (noteListData.isFavorite()) {
            userNoteProperties.add(EUserNoteProperties.FAV);
        }
        Boolean currentUserLikesNote = noteListData.getProperty("currentUserLikesNote");
        if (currentUserLikesNote != null && currentUserLikesNote) {
            userNoteProperties.add(EUserNoteProperties.LIKE);
        }
        if (noteListData.isForMe()) {
            userNoteProperties.add(EUserNoteProperties.NOTIFY);
        }
        target.setUserNoteProperties(userNoteProperties);
        target.setRights(buildNoteRights(noteListData));
        target.setUsersToNotify(TimelineNoteHelper
                .getUsersToNotify(noteListData));
        target.setAuthor(TimelineUserHelper
                .buildTimelineUserResource(noteListData.getUser()));
        target.setAttachments(AttachmentResourceHelper
                .buildAttachmentResources(noteListData.getAttachments()));
        target.setTags(TimelineNoteHelper.getTags(noteListData));
        target.setTopic(TimelineNoteHelper.getTopic(noteListData));
        target.setProperties(TimelineNoteHelper.convertToPropertyResources(noteListData
                .getObjectProperties()));
        return target;
    }

    /**
     * Converts the property TOs into an array of property resource objects
     * 
     * @param properties
     *            collection of TOs to convert
     * @return an array that contains the converted resources or null if the input was null
     */
    public static PropertyResource[] convertToPropertyResources(
            Collection<StringPropertyTO> properties) {
        if (properties == null) {
            return null;
        }
        PropertyResource[] propertyResources = new PropertyResource[properties.size()];
        int i = 0;
        for (StringPropertyTO property : properties) {
            propertyResources[i] = new PropertyResource();
            propertyResources[i].setKeyGroup(property.getKeyGroup());
            propertyResources[i].setKey(property.getPropertyKey());
            propertyResources[i].setValue(property.getPropertyValue());
            i++;
        }
        return propertyResources;
    }

    /**
     * Converts the discussion details of a note list data object into the appropriate resource
     * object
     * 
     * @param noteListDataItem
     *            the source object to copy from
     * @return the discussion resource with details about the discussion
     */
    public static TimelineNoteDiscussionResource getDiscussion(
            NoteData noteListDataItem) {
        TimelineNoteDiscussionResource timelineNoteDiscussionResource = new TimelineNoteDiscussionResource();
        timelineNoteDiscussionResource.setDiscussionId(noteListDataItem.getDiscussionId());
        timelineNoteDiscussionResource.setDepth(noteListDataItem.getDiscussionDepth());
        timelineNoteDiscussionResource.setNumberOfDiscussionNotes(noteListDataItem
                .getNumberOfDiscussionNotes());
        return timelineNoteDiscussionResource;
    }

    /**
     * Converts the tags of a note list data object into resources
     * 
     * @param noteListDataItem
     *            the source object to copy from
     * @return an array that contains the converted resources
     */
    public static TimelineNoteTagResource[] getTags(NoteData noteListDataItem) {
        List<TimelineNoteTagResource> timelineNoteTagResources = new ArrayList<TimelineNoteTagResource>();
        Collection<TagData> tags = noteListDataItem.getTags();
        for (TagData tag : tags) {
            TimelineNoteTagResource timelineNoteTagResource = new TimelineNoteTagResource();
            timelineNoteTagResource.setTagId(tag.getId());
            timelineNoteTagResource.setName(tag.getName());
            timelineNoteTagResources.add(timelineNoteTagResource);
        }
        return timelineNoteTagResources.toArray(new TimelineNoteTagResource[0]);
    }

    /**
     * Extracts the details about a topic into a resource.
     * 
     * @param noteListDataItem
     *            the source object to extract the details from
     * @return the created resource
     */
    public static TimelineTopicResource getTopic(NoteData noteListDataItem) {
        TimelineTopicResource timelineTopicResource = new TimelineTopicResource();
        UserBlogData blog = noteListDataItem.getBlog();
        timelineTopicResource.setTopicId(blog.getId());
        timelineTopicResource.setAlias(blog.getNameIdentifier());
        timelineTopicResource.setTitle(blog.getTitle());
        return timelineTopicResource;
    }

    /**
     * Extracts and converts the users that have been notified into an array of user resources
     * 
     * @param noteListDataItem
     *            the source object to extract the users from
     * @return the converted resources
     */
    public static TimelineUserResource[] getUsersToNotify(NoteData noteListDataItem) {
        List<TimelineUserResource> timelineUserResources = new ArrayList<TimelineUserResource>();
        List<com.communote.server.api.core.user.DetailedUserData> users = noteListDataItem
                .getNotifiedUsers();
        for (UserData user : users) {
            timelineUserResources.add(TimelineUserHelper.buildTimelineUserResource(user));
        }
        return timelineUserResources.toArray(new TimelineUserResource[0]);
    }

    /**
     * @param multivaluedMap
     *            The multivalued map to wrap.
     * @return A {@link RestApiMultivaluedMapWrapper}
     */
    public static Map<String, String> toMap(MultivaluedMap<String, String> multivaluedMap) {
        return new RestApiMultivaluedMapWrapper<String>(multivaluedMap, COMMA_SEPERATED_PARAMETERS);
    }

    /**
     * Default constructor
     */
    private TimelineNoteHelper() {

    }
}
