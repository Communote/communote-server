package com.communote.plugins.mq.message.core.handler;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.communote.common.converter.Converter;
import com.communote.plugins.mq.message.core.data.property.StringProperty;
import com.communote.plugins.mq.message.core.data.tag.Tag;
import com.communote.plugins.mq.message.core.data.topic.Topic;
import com.communote.plugins.mq.message.core.data.topic.TopicRights;
import com.communote.plugins.mq.message.core.message.topic.EditTopicMessage;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogIdentifierValidationException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.NonUniqueBlogIdentifierException;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.api.core.blog.MinimalBlogData;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.blog.BlogTO;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogProperty;

/**
 * Test for {@link EditTopicMessageHandler}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class EditTopicMessageHandlerTest {
    /**
     * BlogManagement for test.
     */
    private class TransparentBlogManagement implements BlogManagement {

        private BlogTO blogTO;

        @Override
        public Blog createBlog(CreationBlogTO arg0)
                throws NonUniqueBlogIdentifierException,
                BlogIdentifierValidationException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void deleteBlog(Long arg0, Long arg1)
                throws NoteManagementAuthorizationException,
                BlogNotFoundException {
            // TODO Auto-generated method stub

        }

        @Override
        public void deleteBlogs(Long[] arg0) throws AuthorizationException {
            // TODO Auto-generated method stub

        }


        @Override
        public Blog getBlogById(Long arg0, boolean arg1) {
            if (arg0.equals(topicId) && arg1) {
                return existingTopic;
            }
            return null;
        }

        @Override
        public Blog findBlogByIdentifier(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Blog findBlogByIdentifierWithoutAuthorizationCheck(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Blog findBlogByIdWithoutAuthorizationCheck(Long arg0) {
            // TODO Auto-generated method stub
            return null;
        }

       

        @Override
        public List<Blog> findBlogsById(Long[] arg0) {
            // TODO Auto-generated method stub
            return null;
        }

       

        @Override
        public String generateUniqueBlogAlias(String arg0, String arg1)
                throws NonUniqueBlogIdentifierException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T> T getBlogByAlias(String alias, Converter<Blog, T> converter)
                throws BlogAccessException {
            return null;
        }

        @Override
        public <T> T getBlogById(Long blogId, Converter<Blog, T> converter) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getBlogCount() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Long getBlogId(String alias) {
            return null;
        }

        /**
         * @return the blogTO
         */
        public BlogTO getBlogTO() {
            return blogTO;
        }

        @Override
        public List<BlogData> getLastUsedBlogs(int arg0, boolean arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public MinimalBlogData getMinimalBlogInfo(Long blogId) {
            return null;
        }

        @Override
        public List<BlogData> getMostUsedBlogs(int arg0, boolean arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void createDefaultBlog(String defaultBlogName)
                throws NonUniqueBlogIdentifierException,
                BlogIdentifierValidationException {
            // TODO Auto-generated method stub

        }

        @Override
        public void resetGlobalPermissions() {
            // TODO Auto-generated method stub

        }

        @Override
        public Blog updateBlog(Long blogId, BlogTO arg0)
                throws NonUniqueBlogIdentifierException,
                BlogIdentifierValidationException {
            if (blogId.equals(topicId)) {
                this.blogTO = arg0;
                Blog topic = Blog.Factory.newInstance();
                topic.setId(topicId);
                return topic;
            }
            return null;
        }

       

    }

    private Blog existingTopic;
    private EditTopicMessageHandler handler;
    private TransparentBlogManagement blogManagementMock;

    private EditTopicMessage message;
    private final Long topicId = 1L;
    private final String newTitle = "new title";
    private final String newDescription = "new description";
    private final String newAlias = "new alias";

    private final int newTagsAmount = 4;

    private final int existingTagsAmount = 4;
    private final int newPropertiesAmount = 4;
    private final int newExternalObjectAmount = 4;

    private final boolean allCanWrite = true;
    private final boolean allCanRead = false;

    /**
     * Init.
     * 
     * @throws AuthorizationException
     *             Thrown, when the user is not authorized to access the given entity.
     * @throws NotFoundException
     *             Thrown, when the entity can't be found.
     */
    @BeforeMethod
    public void initMocks() throws NotFoundException, AuthorizationException {
        blogManagementMock = new TransparentBlogManagement();
        ExternalObjectManagement externalObjectManagement = EasyMock
                .createMock(ExternalObjectManagement.class);
        SecurityHelperWrapper securityHelperMock = EasyMock.createMock(SecurityHelperWrapper.class);
        PropertyManagement propertyManagement = EasyMock.createMock(PropertyManagement.class);
        EasyMock.expect(
                propertyManagement.getAllObjectProperties(EasyMock.eq(PropertyType.BlogProperty),
                        EasyMock.anyLong())).andReturn(new HashSet<StringPropertyTO>()).anyTimes();
        EasyMock.replay(propertyManagement);

        Topic topic = new Topic();
        topic.setTopicId(topicId);
        topic.setTitle(newTitle);
        topic.setDescription(newDescription);
        topic.setTopicAlias(newAlias);

        Tag[] newTags = new Tag[newTagsAmount];

        for (int i = 0; i < newTagsAmount; i++) {
            Tag tag0 = new Tag();
            // tag0.setId(new Long(i + 1));
            newTags[i] = tag0;
        }

        topic.setTags(newTags);

        StringProperty[] newprops = new StringProperty[newPropertiesAmount];
        for (int i = 0; i < newPropertiesAmount; i++) {
            StringProperty prop = new StringProperty();
            prop.setKey("topic_prop_key" + i);
            prop.setValue("topic_prop_value" + i);
            prop.setGroup("prop_group");
            newprops[i] = prop;
        }
        topic.setProperties(newprops);

        com.communote.plugins.mq.message.core.data.topic.ExternalObject[] newExternalObject =
                new com.communote.plugins.mq.message.core.data.topic.ExternalObject[newExternalObjectAmount];
        for (int i = 0; i < newExternalObjectAmount; i++) {
            com.communote.plugins.mq.message.core.data.topic.ExternalObject externalObject =
                    new com.communote.plugins.mq.message.core.data.topic.ExternalObject();
            externalObject.setExternalObjectId("external_object_id_" + i);
            externalObject.setExternalObjectName("external_object_name_" + i);
            newExternalObject[i] = externalObject;
        }
        topic.setProperties(newprops);

        TopicRights topicRights = new TopicRights();
        topicRights.setAllCanRead(allCanRead);
        topicRights.setAllCanWrite(allCanWrite);

        topic.setTopicRights(topicRights);

        message = new EditTopicMessage();
        message.setTopic(topic);

        Set<com.communote.server.model.tag.Tag> existingTags =
                new HashSet<com.communote.server.model.tag.Tag>();

        for (int i = 0; i < existingTagsAmount; i++) {
            com.communote.server.model.tag.Tag tag0 = com.communote.server.model.tag.Tag.Factory
                    .newInstance();
            tag0.setId(new Long(newTagsAmount + 1 + i));
            existingTags.add(tag0);
        }

        Set<BlogProperty> existingProps = new HashSet<BlogProperty>();
        for (int i = 0; i < newTagsAmount; i++) {
            BlogProperty prop = BlogProperty.Factory.newInstance();
            prop.setPropertyKey("topic_prop_key" + (newPropertiesAmount + i));
            prop.setPropertyValue("topic_prop_value"
                    + (newPropertiesAmount + i));
            existingProps.add(prop);
        }
        existingTopic = Blog.Factory.newInstance();
        existingTopic.setId(topicId);
        existingTopic.setTags(existingTags);
        existingTopic.setProperties(existingProps);

        handler = new EditTopicMessageHandler();
        handler.setBlogManagement(blogManagementMock);
        handler.setExternalObjectManagement(externalObjectManagement);
        handler.setSecurityHelper(securityHelperMock);
        handler.setPropertyManagement(propertyManagement);

    }

    /**
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testHandleMessageDeleteTags()
            throws Exception {

        message.setDeleteAllTags(true);

        handler.handleMessage(message);

        Assert.assertNull(blogManagementMock.getBlogTO().getTags());
    }

    /**
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testHandleMessageMergeProperties()
            throws Exception {

        message.setMergeProperties(true);

        handler.handleMessage(message);

        Assert.assertEquals(
                /* existingPropertiesAmount + */newPropertiesAmount,
                blogManagementMock.getBlogTO().getProperties().size());
    }

    /**
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testHandleMessageMergeTags()
            throws Exception {

        message.setMergeTags(true);

        handler.handleMessage(message);

        Assert.assertEquals(existingTagsAmount + newTagsAmount,
                blogManagementMock.getBlogTO().getTags().size());
    }

    /**
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testHandleMessageSetTags()
            throws Exception {
        message.setSetTags(true);

        handler.handleMessage(message);

        Assert.assertEquals(newTagsAmount, blogManagementMock.getBlogTO().getTags()
                .size());
    }

    /**
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testHandleMessageUpdateAndSetSimpleProperties()
            throws Exception {

        message.setUpdateTitle(true);
        message.setUpdateDescription(true);
        message.setUpdateAlias(true);

        handler.handleMessage(message);

        Assert.assertNotNull(blogManagementMock.getBlogTO());
        Assert.assertEquals(newAlias,
                blogManagementMock.getBlogTO().getNameIdentifier());
        Assert.assertEquals(newDescription,
                blogManagementMock.getBlogTO().getDescription());
    }

}
