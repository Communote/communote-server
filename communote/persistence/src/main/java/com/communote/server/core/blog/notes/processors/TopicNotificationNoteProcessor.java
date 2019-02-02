package com.communote.server.core.blog.notes.processors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;

import com.communote.common.converter.CollectionConverter;
import com.communote.common.converter.IdentityConverter;
import com.communote.common.util.PageableList;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorContext;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.blog.TooManyMentionedUsersNoteManagementException;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.core.vo.query.user.UserTaggingCoreQuery;
import com.communote.server.core.vo.query.user.UserTaggingCoreQueryParameters;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.blog.UserToBlogRoleMapping;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteStatus;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;

/**
 * Processor which handles topic mention flags.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicNotificationNoteProcessor extends NotificationNoteProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicNotificationNoteProcessor.class);

    private static final String PROPERTY_KEY_TOPIC_MENTIONS = PropertyManagement.KEY_GROUP
            + ".notification.topicMentionsToSkip";
    private static final String TOPIC_MENTION_READERS = "reader";
    private static final String TOPIC_MENTION_AUTHORS = "author";
    private static final String TOPIC_MENTION_MANAGERS = "manager";

    private static final int DEFAULT_AUTHOR_FETCH_SIZE = 50;
    
    private final BlogRightsManagement topicRightsManagement;
    private final UserManagement userManagement;
    private final QueryManagement queryManagement;
    private int authorFetchSize = DEFAULT_AUTHOR_FETCH_SIZE;

    /**
     * Constructor.
     *
     * @param topicRightsManagement
     *            Used to get users to notify.
     * @param userManagement
     *            Used to retrieve concrete users.
     * @param queryManagement
     *            Needed to execute queries.
     */
    public TopicNotificationNoteProcessor(BlogRightsManagement topicRightsManagement,
            UserManagement userManagement, QueryManagement queryManagement) {
        this.topicRightsManagement = topicRightsManagement;
        this.userManagement = userManagement;
        this.queryManagement = queryManagement;
    }

    private void assertNotTooManyUsers(Note note, Set<Long> userIdsToSkip, boolean includeReaders,
            boolean includeAuthors, boolean includeManagers) {
        int maxUsers = ClientProperty.MAX_NUMBER_OF_MENTIONED_USERS
                .getValue(ClientProperty.DEFAULT_MAX_NUMBER_OF_MENTIONED_USERS);
        if (maxUsers <= 0) {
            return;
        }
        // Shortcut for readers and allCanRead via active user count
        if (includeReaders && note.getBlog().isAllCanRead()
                && maxUsers < userManagement.getActiveUserCount() - userIdsToSkip.size()) {
            throw new TooManyMentionedUsersNoteManagementException();
        }
        Collection<User> usersToNotify = internalGetUsersToNotify(note, includeReaders,
                includeAuthors, includeManagers);
        if (maxUsers < usersToNotify.size()) {
            // fail-fast if still too many if skipping all
            if (maxUsers < usersToNotify.size() - userIdsToSkip.size()) {
                throw new TooManyMentionedUsersNoteManagementException();
            }
            // check if users to skip are actually among those to notify
            int usersToNotifyCount = 0;
            for (User user : usersToNotify) {
                if (!userIdsToSkip.contains(user.getId())) {
                    usersToNotifyCount++;
                }
            }
            if (maxUsers < usersToNotifyCount) {
                throw new TooManyMentionedUsersNoteManagementException();
            }
        }
    }

    @Override
    protected boolean isSendNotifications(Note note, NoteStoringTO orginalNoteStoringTO,
            Map<String, String> properties, NoteNotificationDetails resendDetails) {
        if (!note.isDirect()) {
            boolean includeAuthors;
            boolean includeManagers;
            boolean includeReaders;
            Set<Long> userIdsToSkip;
            if (resendDetails != null) {
                includeAuthors = note.isMentionTopicAuthors()
                        && !resendDetails.isMentionTopicAuthors();
                includeManagers = note.isMentionTopicManagers()
                        && !resendDetails.isMentionTopicManagers();
                includeReaders = note.isMentionTopicReaders()
                        && !resendDetails.isMentionTopicReaders();
                userIdsToSkip = resendDetails.getMentionedUserIds();
            } else {
                includeAuthors = note.isMentionTopicAuthors();
                includeManagers = note.isMentionTopicManagers();
                includeReaders = note.isMentionTopicReaders();
                userIdsToSkip = new HashSet<>();
            }
            if (includeAuthors || includeManagers || includeReaders) {
                assertNotTooManyUsers(note, userIdsToSkip, includeReaders, includeAuthors,
                        includeManagers);
                properties
                .put(PROPERTY_KEY_TOPIC_MENTIONS,
                        encodeMentionPropertyValue(includeReaders, includeAuthors,
                                includeManagers));
                return true;
            }
        }
        return false;
    }

    private String encodeMentionPropertyValue(boolean includeReaders, boolean includeAuthors,
            boolean includeManagers) {
        StringBuilder value = new StringBuilder();
        String separator = "";
        if (!includeReaders) {
            value.append(TOPIC_MENTION_READERS);
            separator = ",";
        }
        if (!includeAuthors) {
            value.append(separator);
            value.append(TOPIC_MENTION_AUTHORS);
            separator = ",";
        }

        if (!includeManagers) {
            value.append(separator);
            value.append(TOPIC_MENTION_MANAGERS);
        }
        return value.toString();
    }

    @Override
    public String getId() {
        return "topicMention";
    }

    private void collectAuthors(Note note, Collection<User> usersToNotify) {
        Long topicId = note.getBlog().getId();
        UserTaggingCoreQuery query = new UserTaggingCoreQuery();
        UserTaggingCoreQueryParameters parameters = new UserTaggingCoreQueryParameters(query);
        parameters.setLimitResultSet(false);
        int offset = 0;
        ResultSpecification resultSpecification = new ResultSpecification(offset, authorFetchSize, 1);
        parameters.setResultSpecification(resultSpecification);
        parameters.setExcludeNoteStatus(new NoteStatus[] { NoteStatus.AUTOSAVED });
        parameters.setTypeSpecificExtension(new TaggingCoreItemUTPExtension());
        parameters.getTypeSpecificExtension().setBlogId(topicId);
        parameters.getTypeSpecificExtension().setTopicAccessLevel(TopicAccessLevel.READ);
        parameters.getTypeSpecificExtension().setUserId(note.getUser().getId());
        parameters.setIncludeStatusFilter(new UserStatus[] { UserStatus.ACTIVE });
        // use the internal system user to include all direct messages
        SecurityContext securityContext = AuthenticationHelper.setInternalSystemToSecurityContext();
        try {
            while(collectAuthors(query, parameters, topicId, usersToNotify)) {
                offset += authorFetchSize;
                resultSpecification.setOffset(offset);
            }
        } finally {
            AuthenticationHelper.setSecurityContext(securityContext);
        }
    }

    private boolean collectAuthors(UserTaggingCoreQuery query, UserTaggingCoreQueryParameters parameters,
            Long topicId, Collection<User> usersToNotify) {
        PageableList<UserData> authors = queryManagement.query(query, parameters);
        for (UserData author : authors) {
            if (topicRightsManagement.userHasReadAccess(topicId,
                    author.getId(), false)) {
                User userToNotify = userManagement.getUserById(author.getId(),
                        new IdentityConverter<User>());
                if (userToNotify != null) {
                    usersToNotify.add(userToNotify);
                }
            }
        }
        return authors.getMinNumberOfAdditionalElements() > 0;
    }
    
    /**
     * @return 0
     */
    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    protected Collection<User> getUsersToNotify(Note note, NoteStoringPostProcessorContext context,
            Set<Long> userIdsToSkip) {
        if (note.isDirect()) {
            return null;
        }
        boolean includeAuthors;
        boolean includeManagers;
        boolean includeReaders;
        String value = context.getProperties().get(PROPERTY_KEY_TOPIC_MENTIONS);
        if (value == null) {
            includeAuthors = note.isMentionTopicAuthors();
            includeManagers = note.isMentionTopicManagers();
            includeReaders = note.isMentionTopicReaders();
        } else {
            includeAuthors = !value.contains(TOPIC_MENTION_AUTHORS);
            includeManagers = !value.contains(TOPIC_MENTION_MANAGERS);
            includeReaders = !value.contains(TOPIC_MENTION_READERS);
        }
        Collection<User> usersToNotify = internalGetUsersToNotify(note, includeReaders,
                includeAuthors, includeManagers);
        if (LOGGER.isDebugEnabled()) {
            StringBuilder message = new StringBuilder("Users to notify about note ");
            message.append(note.getId()).append(":");
            for (User user : usersToNotify) {
                message.append(" ").append(user.getAlias());
            }
            LOGGER.debug(message.toString());
        }
        return usersToNotify;
    }

    /**
     * Method to get all users to be notified.
     *
     * @param note
     *            The note to check.
     * @return Collection of users to be notified.
     */
    private Collection<User> internalGetUsersToNotify(Note note, boolean includeReaders,
            boolean includeAuthors, boolean includeManagers) {
        Collection<User> usersToNotify = new HashSet<User>();
        Set<BlogRole> roles = new HashSet<BlogRole>();
        if (includeReaders) {
            if (note.getBlog().isAllCanRead()) {
                return userManagement.findUsersByRole(UserRole.ROLE_KENMEI_USER, UserStatus.ACTIVE);
            }
            roles.add(BlogRole.VIEWER);
            roles.add(BlogRole.MEMBER);
            roles.add(BlogRole.MANAGER);
        } else if (includeManagers) {
            roles.add(BlogRole.MANAGER);
        }
        if (roles.size() > 0) {
            Collection<User> mappedUsers = topicRightsManagement.getMappedUsers(note.getBlog()
                    .getId(), new CollectionConverter<UserToBlogRoleMapping, User>() {
                @Override
                public User convert(UserToBlogRoleMapping source) {
                    return userManagement.getUserById(source.getUserId(),
                            new IdentityConverter<User>());
                }
            }, roles.toArray(new BlogRole[roles.size()]));
            usersToNotify.addAll(mappedUsers);
        }
        if (includeAuthors) {
            collectAuthors(note, usersToNotify);
        }
        return usersToNotify;
    }

    
    public void setAuthorFetchSize(int fetchSize) {
        if (fetchSize <= 0 || fetchSize > 100) {
            LOGGER.warn("Ignoring fetch size {}. Using default {}", fetchSize, DEFAULT_AUTHOR_FETCH_SIZE);
            fetchSize = DEFAULT_AUTHOR_FETCH_SIZE;
        }
        authorFetchSize = fetchSize;
    }
}
