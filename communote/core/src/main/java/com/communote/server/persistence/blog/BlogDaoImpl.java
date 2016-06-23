package com.communote.server.persistence.blog;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.communote.server.api.core.blog.BlogData;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogConstants;
import com.communote.server.model.blog.BlogMemberConstants;
import com.communote.server.model.blog.UserToBlogRoleMappingConstants;
import com.communote.server.model.external.ExternalObjectConstants;
import com.communote.server.model.note.NoteConstants;
import com.communote.server.model.user.CommunoteEntityConstants;

/**
 * Class for implementing service methods of the Blog object.
 *
 * @see com.communote.server.model.blog.Blog
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogDaoImpl extends BlogDaoBase {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(BlogDaoImpl.class);

    private final static String QUERY_MOST_USED_BLOGS = "SELECT new " + BlogData.class.getName()
            + "(blog.id) FROM " + NoteConstants.CLASS_NAME
            + " as note left join note.blog as blog WHERE "
            + "(blog.allCanWrite = true OR blog.id IN (" + "SELECT DISTINCT blogId FROM "
            + UserToBlogRoleMappingConstants.CLASS_NAME
            + " WHERE userId = :userId AND numericRole>=2)" + ") AND note.user.id = :userId "
            + "AND note.lastModificationDate >= :lastModificationDate "
            + "GROUP BY blog.id,blog.nameIdentifier,"
            + "blog.title, blog.lastModificationDate order by count(blog.id) desc";

    private final static String QUERY_LAST_USED_BLOGS = "SELECT new " + BlogData.class.getName()
            + "(blog.id) FROM " + NoteConstants.CLASS_NAME
            + " as note left join note.blog as blog WHERE (blog.allCanWrite = true OR blog.id IN "
            + "(SELECT DISTINCT blogId FROM " + UserToBlogRoleMappingConstants.CLASS_NAME
            + " WHERE userId = :userId AND numericRole>=2)) AND note.user.id = :userId "
            + "group by blog.id,blog.nameIdentifier,blog.title"
            + " order by max(note.creationDate) desc ";

    /**
     * {@inheritDoc}
     *
     * @see BlogDao#blogListItemToEntity(com.communote.server.api.core.blog.BlogData.BlogListItem)
     */
    public Blog blogListItemToEntity(BlogData blogListItem) {
        Blog blog = null;
        if (blogListItem.getId() != null) {
            blog = load(blogListItem.getId());
        }
        if (blog == null) {
            blog = Blog.Factory.newInstance();
        }
        // blogListItemToEntity(blogListItem, blog, true);
        return blog;
    }

    /**
     * {@inheritDoc} Lower case the identifier
     */
    @Override
    public Object create(int transform, Blog blog) {
        if (blog == null) {
            throw new IllegalArgumentException("Blog.create - 'blog' can not be null");
        }
        blog.setLastModificationDate(blog.getCreationDate());
        this.getHibernateTemplate().save(blog);
        blog.setGlobalId(getGlobalIdDao().createGlobalId(blog));

        return this.transformEntity(transform, blog);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Blog findByNameIdentifier(String nameIdentifier) {
        if (nameIdentifier == null) {
            return null;
        }
        Blog blog = super.findByNameIdentifier(StringUtils.lowerCase(nameIdentifier));
        return blog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getBlogCount() {
        return (Long) getHibernateTemplate().find(
                "select count(*) from " + BlogConstants.CLASS_NAME).get(0);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.persistence.blog.BlogDao#findBlogs(Long[])
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<Blog> handleFindBlogs(final Long[] ids) {
        return getHibernateTemplate().executeFind(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from " + BlogConstants.CLASS_NAME + " where "
                        + BlogConstants.ID + " IN (:blog_ids)");
                query.setParameterList("blog_ids", ids);
                return query.list();
            }

        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Blog handleFindByExternalObject(Long internalExternalObjectId) {
        DetachedCriteria criteria = DetachedCriteria.forClass(Blog.class)
                .createAlias(BlogConstants.EXTERNALOBJECTS, "eo")
                .add(Restrictions.eq("eo." + ExternalObjectConstants.ID, internalExternalObjectId))
                .setFetchMode("eo", FetchMode.JOIN);
        List<Blog> blogs = getHibernateTemplate().findByCriteria(criteria);
        if (!CollectionUtils.isEmpty(blogs)) {
            return blogs.iterator().next();
        }
        return null;
    }

    @Override
    protected Blog handleFindByExternalObject(String externalSystemId, String externalId) {
        DetachedCriteria criteria = DetachedCriteria.forClass(Blog.class).createAlias(
                BlogConstants.EXTERNALOBJECTS, "eo");
        criteria.add(Restrictions.eq("eo." + ExternalObjectConstants.EXTERNALSYSTEMID,
                externalSystemId));
        criteria.add(Restrictions.eq("eo." + ExternalObjectConstants.EXTERNALID, externalId));
        criteria.setFetchMode("eo", FetchMode.JOIN);
        List<Blog> blogs = getHibernateTemplate().findByCriteria(criteria);
        if (!CollectionUtils.isEmpty(blogs)) {
            return blogs.iterator().next();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Blog> handleFindByExternalSystemId(String systemId) {
        DetachedCriteria criteria = DetachedCriteria.forClass(Blog.class)
                .createAlias(BlogConstants.EXTERNALOBJECTS, "eo")
                .add(Restrictions.eq("eo." + ExternalObjectConstants.EXTERNALSYSTEMID, systemId))
                .setFetchMode("eo", FetchMode.JOIN);
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Blog> handleFindDirectlyManagedBlogsOfUser(Long userId) {
        StringBuilder query = new StringBuilder("select blog from " + BlogConstants.CLASS_NAME
                + " as blog left join blog." + BlogConstants.MEMBERS + " as members where");
        query.append(" members." + BlogMemberConstants.ROLE + "='MANAGER'");
        query.append(" and members." + BlogMemberConstants.MEMBERENTITY + "."
                + CommunoteEntityConstants.ID + "=?");
        return getHibernateTemplate().find(query.toString(), userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Blog handleFindLatestBlog() {
        Criteria criteria = getSession().createCriteria(Blog.class);
        ProjectionList projList = Projections.projectionList();
        projList.add(Projections.max(BlogConstants.ID));
        criteria.setProjection(projList);
        List result = criteria.list();
        if (result != null && result.size() == 1 && result.get(0) != null) {
            Long id = (Long) result.get(0);
            return load(id);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<BlogData> handleGetLastUsedBlogs(Long userId, int maxResults) {
        Query query = getSession().createQuery(QUERY_LAST_USED_BLOGS);
        query.setLong("userId", userId);
        query.setMaxResults(maxResults);
        return query.list();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<BlogData> handleGetMostUsedBlogs(Long userId, int maxResults, int maxDays) {
        Query query = getSession().createQuery(QUERY_MOST_USED_BLOGS);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -maxDays);
        query.setDate("lastModificationDate", calendar.getTime());
        query.setLong("userId", userId);
        query.setMaxResults(maxResults);

        return query.list();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetGlobalPermissions() {
        // TODO although this operation is fast, it will invalidate the 2nd level cache of the
        // entire BlogCache region (see SingletonEhCacheProvider for contained entities). On SaaS
        // this targets all clients!
        int alteredEntries = getHibernateTemplate().bulkUpdate(
                "UPDATE " + BlogConstants.CLASS_NAME + " SET " + BlogConstants.ALLCANREAD
                        + " = false, " + BlogConstants.ALLCANWRITE + " = false");
        LOGGER.debug("The global permissions where removed from about {} topics.", alteredEntries);
    }
}
