package com.communote.server.core.converter.blog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.blog.helper.MailBasedPostingHelper;
import com.communote.server.core.filter.listitems.blog.DetailBlogListItem;
import com.communote.server.core.vo.query.converters.TagToTagDataQueryResultConverter;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogMember;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.model.user.User;
import com.communote.server.persistence.helper.dao.LazyClassLoaderHelper;

/**
 * Converter for converting a blog entity into a list item.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            the target type
 */
public class BlogToDetailBlogListItemConverter<T extends DetailBlogListItem> extends
        BlogToBlogDataConverter<T> {

    private final boolean includeTags;
    private final boolean includeMembers;
    private Locale locale;
    private BlogToDetailBlogListItemConverter<T> childConverter;
    private BlogToDetailBlogListItemConverter<T> parentConverter;
    private BlogRightsManagement topicRightsManagement;

    /**
     * Create a new converter
     * 
     * @param clazz
     *            the class of the target type
     * @param includeProperties
     *            whether to add the blog properties to the result. The property key will be
     *            constructed as follows &lt;groupKey&gt;.&lt;propertyKey&gt;
     * @param includeTags
     *            whether to add the tags as a comma separated string
     * @param includeMembers
     *            whether to include the members
     * @param includeChildren
     *            If true, the children of this topic, the user can access will be converted too.
     * @param topicRightsManagement
     *            This is only used when includeChildren is true.
     */
    public BlogToDetailBlogListItemConverter(Class<T> clazz, boolean includeProperties,
            boolean includeTags, boolean includeMembers, boolean includeChildren,
            boolean includeParents,
            BlogRightsManagement topicRightsManagement) {
        this(clazz, includeProperties, includeTags, includeMembers, includeChildren,
                includeParents,
                topicRightsManagement, Locale.ENGLISH);
    }

    /**
     * Create a new converter
     * 
     * @param clazz
     *            the class of the target type
     * @param includeProperties
     *            whether to add the blog properties to the result. The property key will be
     *            constructed as follows &lt;groupKey&gt;.&lt;propertyKey&gt;
     * @param includeTags
     *            whether to add the tags as a comma separated string
     * @param includeMembers
     *            whether to include the members
     * @param includeChildren
     *            If true, the direct children of this topic, the user can access will be converted
     *            too.
     * @param includeParents
     *            If true, the direct parents of this topic, the user can access will be converted
     *            too.
     * @param topicRightsManagement
     *            This is only used when includeChildren is true.
     * @param locale
     *            The locale used for translation of tags.
     */
    public BlogToDetailBlogListItemConverter(Class<T> clazz, boolean includeProperties,
            boolean includeTags, boolean includeMembers, boolean includeChildren,
            boolean includeParents, BlogRightsManagement topicRightsManagement, Locale locale) {
        super(clazz, includeProperties);
        this.includeTags = includeTags;
        this.includeMembers = includeMembers;
        this.topicRightsManagement = topicRightsManagement;
        this.locale = locale;
        if (includeChildren) {
            childConverter = new BlogToDetailBlogListItemConverter<T>(clazz, includeProperties,
                    includeTags, includeMembers, false, false, null);
        }
        if (includeParents) {
            parentConverter = new BlogToDetailBlogListItemConverter<T>(clazz, includeProperties,
                    includeTags, includeMembers, false, false, null);
        }
    }

    @Override
    public void convert(Blog source, T target) {
        super.convert(source, target);
        target.setAllCanRead(source.isAllCanRead());
        target.setAllCanWrite(source.isAllCanWrite());
        target.setToplevelTopic(source.isToplevelTopic());
        target.setRootTopic(source.getParents().isEmpty());
        target.setCreateSystemNotes(source.isCreateSystemNotes());
        target.setBlogEmail(MailBasedPostingHelper.getBlogEmailAddress(source.getNameIdentifier()));
        convertChildren(source, target);
        convertParents(source, target);
        if (includeTags && source.getTags() != null) {
            TagToTagDataQueryResultConverter tagConverter =
                    new TagToTagDataQueryResultConverter(locale);
            for (Tag tag : source.getTags()) {
                TagData tagListItem = new TagData();
                tagConverter.convert(tag, tagListItem);
                target.getTagItems().add(tagListItem);
            }
        }
        if (includeMembers) {
            List<String> readers = new ArrayList<String>();
            List<String> writers = new ArrayList<String>();
            List<String> managers = new ArrayList<String>();
            for (BlogMember member : source.getMembers()) {
                CommunoteEntity e = LazyClassLoaderHelper.deproxy(member.getMemberEntity(),
                        CommunoteEntity.class);
                if (e instanceof User) {
                    String memberUserId = e.getId().toString();
                    if (BlogRole.MANAGER.equals(member.getRole())) {
                        managers.add(memberUserId);
                    } else if (BlogRole.MEMBER.equals(member.getRole())) {
                        writers.add(memberUserId);
                    } else if (BlogRole.VIEWER.equals(member.getRole())) {
                        readers.add(memberUserId);
                    }
                }
            }
            target.setManagingUserIds(StringUtils.join(managers, ","));
            target.setReadingUserIds(StringUtils.join(readers, ","));
            target.setWritingUserIds(StringUtils.join(writers, ","));
        }
    }

    /**
     * If requested, this method converts the children of the given topic.
     * 
     * @param source
     *            The source topic.
     * @param target
     *            The target item.
     */
    private void convertChildren(Blog source, T target) {
        if (childConverter == null) {
            return;
        }
        for (Blog child : source.getChildren()) {
            if (topicRightsManagement.currentUserHasReadAccess(child.getId(), false)) {
                target.getChildren().add(childConverter.convert(child));
            }
        }
    }

    /**
     * If requested, this method converts the parents of the given topic.
     * 
     * @param source
     *            The source topic.
     * @param target
     *            The target item.
     */
    private void convertParents(Blog source, T target) {
        if (parentConverter == null) {
            return;
        }
        for (Blog parent : source.getParents()) {
            if (topicRightsManagement.currentUserHasReadAccess(parent.getId(), false)) {
                target.getParents().add(parentConverter.convert(parent));
            }
        }
    }
}
