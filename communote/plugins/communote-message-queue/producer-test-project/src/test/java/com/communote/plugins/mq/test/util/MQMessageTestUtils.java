package com.communote.plugins.mq.test.util;

import java.util.Random;
import java.util.UUID;

import com.communote.plugins.mq.message.base.data.security.Authentication;
import com.communote.plugins.mq.message.base.data.security.UserIdentity;
import com.communote.plugins.mq.message.base.data.security.UserIdentityContext;
import com.communote.plugins.mq.message.base.data.security.UsernamePasswordAuthentication;
import com.communote.plugins.mq.message.base.message.BaseMessage;
import com.communote.plugins.mq.message.base.message.ReplyType;
import com.communote.plugins.mq.message.core.data.note.Note;
import com.communote.plugins.mq.message.core.data.property.StringProperty;
import com.communote.plugins.mq.message.core.data.role.ExternalTopicRole;
import com.communote.plugins.mq.message.core.data.tag.Tag;
import com.communote.plugins.mq.message.core.data.topic.BaseTopic;
import com.communote.plugins.mq.message.core.data.topic.ExternalObject;
import com.communote.plugins.mq.message.core.data.topic.Topic;
import com.communote.plugins.mq.message.core.data.topic.TopicRights;
import com.communote.plugins.mq.message.core.message.note.CreateNoteMessage;
import com.communote.plugins.mq.message.core.message.topic.CreateTopicMessage;
import com.communote.plugins.mq.message.core.message.topic.DeleteTopicMessage;
import com.communote.plugins.mq.message.core.message.topic.EditTopicMessage;
import com.communote.plugins.mq.message.core.message.topic.SetTopicRolesMessage;
import com.communote.plugins.mq.message.core.message.topic.UpdateTopicRolesMessage;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.api.core.tag.TagStoreType.Types;


/**
 * Helper class for tests.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MQMessageTestUtils {

    private final String authenticationUsername;
    private final String authenticationPassword;

    /**
     * Create a utils without authentication credentials. A client certificate must be accessible
     * than.
     */
    public MQMessageTestUtils() {
        this(null, null);
    }

    /**
     * 
     * @param authenticationUsername
     *            the authentication username
     * @param authenticationPassword
     *            the authentication password
     */
    public MQMessageTestUtils(String authenticationUsername, String authenticationPassword) {
        this.authenticationUsername = authenticationUsername;
        this.authenticationPassword = authenticationPassword;
    }

    /**
     * Adding external objects to a topic
     * 
     * @param topic
     *            the topic object to add
     */

    private void addExternalObjects(Topic topic) {
        ExternalObject[] externalObjects = new ExternalObject[new Random().nextInt(5) + 1];
        for (int i = 0; i < externalObjects.length; i++) {
            ExternalObject externalObject = new ExternalObject();
            externalObject.setExternalObjectId("objectId" + i + "_" + UUID.randomUUID().toString());
            externalObject.setExternalObjectName("objectName" + i + "_"
                    + UUID.randomUUID().toString());
            externalObjects[i] = externalObject;
        }
        topic.setExternalObjects(externalObjects);
    }

    /**
     * 
     * @return create an authentication to be used for the messages
     */
    public Authentication createAuthentication() {
        UsernamePasswordAuthentication authentication = null;
        if (this.authenticationUsername != null) {
            authentication = new UsernamePasswordAuthentication();
            authentication.setIdentity(new UserIdentity());
            authentication.getIdentity().setIdentity(authenticationUsername);
            authentication.setPassword(authenticationPassword.toCharArray());
        }
        return authentication;
    }

    /**
     * Method to create a message for topic creation.
     * 
     * @param userLogin
     *            The users login.
     * @param allCanRead
     *            True, if all can read.
     * @param allCanWrite
     *            True, if all can write.
     * @return The message.
     */
    public CreateTopicMessage createCreateTopicMessage(String userLogin, boolean allCanRead,
            boolean allCanWrite) {
        CreateTopicMessage ctMessage = new CreateTopicMessage();
        ctMessage.setExternalSystemId("DefaultLDAP");
        ctMessage.setTopic(createRandomTopic(allCanRead, allCanWrite));
        ctMessage.setIdentityContext(createUserIdentityContext(userLogin));
        ctMessage.setAuthentication(createAuthentication());
        return ctMessage;
    }

    /**
     * Method to create a message for deleting a topic.
     * 
     * @param userAlias
     *            Alias of the user, who will delete the topic.
     * @param topicId
     *            Id of the topic, which should be deleted.
     * @return The message.
     */
    public DeleteTopicMessage createDeleteTopicMessage(String userAlias, long topicId) {
        DeleteTopicMessage deleteTopicMessage = new DeleteTopicMessage();
        BaseTopic topic = new BaseTopic();
        topic.setTopicId(topicId);
        deleteTopicMessage.setTopic(topic);

        setStandardMessageValues(userAlias, deleteTopicMessage);

        return deleteTopicMessage;
    }

    /**
     * Creates a message to edit the topic with totally new random properties.
     * 
     * @param userAlias
     *            Alias of the user, which should be set within the context.
     * @param topicId
     *            Id of the topic to change. Set to 0 or less to disable this.
     * @param topicAlias
     *            Alias of the topic to change.
     * @param allCanRead
     *            True, if all can read.
     * @param allCanWrite
     *            True, if all can write.
     * @return Message for editing the topic.
     */
    public EditTopicMessage createEditTopicMessage(String userAlias, long topicId,
            String topicAlias, boolean allCanRead, boolean allCanWrite) {
        EditTopicMessage message = new EditTopicMessage();

        setStandardMessageValues(userAlias, message);

        message.setTopic(createRandomTopic(allCanRead, allCanWrite));
        if (topicId > 0) {
            message.getTopic().setTopicId(topicId);
        }
        message.getTopic().setTopicAlias(topicAlias);
        message.setUpdateTitle(true);
        message.setUpdateTopicRights(true);
        message.setSetTags(true);
        message.setMergeProperties(true);
        message.setMergeTags(false);
        return message;
    }

    /**
     * Creates a message with random content.
     * 
     * @param userAlias
     *            The user who should create the message.
     * @param topicId
     *            The topics id.
     * @return The message.
     */
    public CreateNoteMessage createRandomNoteMessage(String userAlias, long topicId) {
        CreateNoteMessage message = new CreateNoteMessage();
        message.setIdentityContext(createUserIdentityContext(userAlias));
        message.setAuthentication(createAuthentication());
        Note note = new Note();
        note.setContent(random());
        Tag[] tags = new Tag[new Random().nextInt(5)];
        for (int i = 0; i < tags.length; i++) {
            tags[i] = new Tag();
            tags[i].setName(random());
            tags[i].setDefaultName(random());
            tags[i].setTagStoreAlias(Types.NOTE.getDefaultTagStoreId());
        }
        note.setTags(tags);
        BaseTopic topic = new BaseTopic();
        topic.setTopicId(topicId);
        note.setTopics(new BaseTopic[] { topic });
        note.setContentType(Note.CONTENT_TYPE_PLAIN_TEXT);
        message.setNote(note);
        return message;
    }

    /**
     * @param allCanRead
     *            True, if all users should be able to read this topic.
     * @param allCanWrite
     *            True, if all users should be able to write to this topic.
     * @return A random topic.
     */
    public Topic createRandomTopic(boolean allCanRead, boolean allCanWrite) {
        Topic topic = new Topic();
        topic.setTitle(random());
        topic.setDescription(random());
        topic.setTopicId(-1L);
        topic.setTopicAlias(random());

        Tag[] newTags = new Tag[new Random().nextInt(5) + 1];

        for (int i = 0; i < newTags.length; i++) {
            Tag tag = new Tag();
            tag.setDefaultName("NewTagDefName" + i);
            tag.setName("NewTag" + i);
            tag.setTagStoreAlias(Types.BLOG.getDefaultTagStoreId());
            newTags[i] = tag;
        }

        topic.setTags(newTags);

        StringProperty[] newprops = new StringProperty[new Random().nextInt(5) + 1];
        for (int i = 0; i < newprops.length; i++) {
            StringProperty property = new StringProperty();
            property.setGroup("testGroup" + i);
            property.setKey("topic_prop_seriously_new_key" + i);
            property.setValue("topic_prop_new_value" + i);
            newprops[i] = property;
        }
        topic.setProperties(newprops);

        addExternalObjects(topic);

        TopicRights topicRights = new TopicRights();
        topicRights.setAllCanRead(allCanRead);
        topicRights.setAllCanWrite(allCanWrite);
        topic.setTopicRights(topicRights);
        return topic;
    }

    /**
     * Method to create a message for setting a role of a user for a given topic.
     * 
     * @param aliasOfTopicManager
     *            The alias of the topic manager.
     * @param aliasOfUserWithRole
     *            Alias of the user to set.
     * @param roleToSet
     *            The role to set.
     * @param topicId
     *            The topics id.
     * @return The message.
     */
    public SetTopicRolesMessage createSetTopicRolesMessage(String aliasOfTopicManager,
            String aliasOfUserWithRole, BlogRole roleToSet, long topicId) {
        SetTopicRolesMessage setTopicRolesMessage = new SetTopicRolesMessage();
        setTopicRolesMessage.setExternalSystemId("DefaultLDAP");
        BaseTopic baseTopic = new BaseTopic();
        baseTopic.setTopicId(topicId);
        setTopicRolesMessage.setTopic(baseTopic);
        setTopicRolesMessage.setIdentityContext(createUserIdentityContext(aliasOfTopicManager));
        setTopicRolesMessage.setAuthentication(createAuthentication());
        ExternalTopicRole role = new ExternalTopicRole();
        role.setExternalObjectId(random());
        role.setTopicRole(roleToSet.getValue());
        role.getEntity().setEntityAlias(aliasOfUserWithRole);
        setTopicRolesMessage.setRoles(new ExternalTopicRole[] { role });
        return setTopicRolesMessage;
    }

    /**
     * Method to create a message for updating a role of a user for a given topic.
     * 
     * @param aliasOfTopicManager
     *            The alias of the topic manager.
     * @param aliasOfUserWithRole
     *            Alias of the user to set.
     * @param roleToSet
     *            The role to set.
     * @param topicId
     *            The topics id.
     * @param topicAlias
     *            Alias of the topic to change.
     * @return The message.
     */
    public UpdateTopicRolesMessage createUpdateTopicRolesMessage(String aliasOfTopicManager,
            String aliasOfUserWithRole, BlogRole roleToSet, long topicId, String topicAlias) {
        BaseTopic topic = new BaseTopic();
        topic.setTopicId(topicId);
        topic.setTopicAlias(topicAlias);
        return updateTopicRoles(aliasOfTopicManager, aliasOfUserWithRole, roleToSet,
                topic);
    }

    /**
     * Method to create a message for updating a role of a user for a given topic (alias only).
     * 
     * @param aliasOfTopicManager
     *            The alias of the topic manager.
     * @param aliasOfUserWithRole
     *            Alias of the user to set.
     * @param roleToSet
     *            The role to set.
     * @param topicAlias
     *            Alias of the topic to change.
     * @return The message.
     */
    public UpdateTopicRolesMessage createUpdateTopicRolesMessage(String aliasOfTopicManager,
            String aliasOfUserWithRole, BlogRole roleToSet, String topicAlias) {
        BaseTopic topic = new BaseTopic();
        topic.setTopicId(null);
        topic.setTopicAlias(topicAlias);
        return updateTopicRoles(aliasOfTopicManager, aliasOfUserWithRole, roleToSet,
                topic);
    }

    /**
     * @param userAlias
     *            Alias of the user, which should be set within the context.
     * @return user identity context
     */
    public UserIdentityContext createUserIdentityContext(String userAlias) {
        UserIdentity userIdentity = new UserIdentity();
        userIdentity.setIdentity(userAlias);
        UserIdentityContext identityContext = new UserIdentityContext();
        identityContext.setIdentity(userIdentity);
        return identityContext;
    }

    /**
     * Creates a random string
     * 
     * @return A random string.
     */
    private String random() {
        return UUID.randomUUID().toString();
    }

    /**
     * Setting standard message values like replyType, authentication and the identity context
     * 
     * @param userAlias
     *            Alias of the user, which should be set within the context.
     * @param message
     *            the message to set
     */

    private void setStandardMessageValues(String userAlias, BaseMessage message) {
        message.setIdentityContext(createUserIdentityContext(userAlias));
        message.setAuthentication(createAuthentication());
        message.setReplyType(ReplyType.FULL);
    }

    /**
     * Method to create a message for updating a role of a user for a given topic.
     * 
     * @param aliasOfTopicManager
     *            The alias of the topic manager.
     * @param aliasOfUserWithRole
     *            Alias of the user to set.
     * @param roleToSet
     *            The role to set.
     * @param topic
     *            The topic to change.
     * @return The message.
     */
    private UpdateTopicRolesMessage updateTopicRoles(String aliasOfTopicManager,
            String aliasOfUserWithRole, BlogRole roleToSet, BaseTopic topic) {
        UpdateTopicRolesMessage updateTopicRolesMessage = new UpdateTopicRolesMessage();
        updateTopicRolesMessage.setExternalSystemId("DefaultLDAP");
        updateTopicRolesMessage.setTopic(topic);
        updateTopicRolesMessage.setIdentityContext(createUserIdentityContext(aliasOfTopicManager));
        updateTopicRolesMessage.setAuthentication(createAuthentication());
        ExternalTopicRole role = new ExternalTopicRole();
        role.setExternalObjectId(random());
        role.setTopicRole(roleToSet.getValue());
        role.getEntity().setEntityAlias(aliasOfUserWithRole);
        updateTopicRolesMessage.setRoles(new ExternalTopicRole[] { role });
        return updateTopicRolesMessage;
    }

}
