package com.communote.server.core.vo.query.blog;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.model.blog.Blog;
import com.communote.server.persistence.blog.BlogDao;

/**
 * Giving access to blog object by requesting DB
 *
 *
 * @param <I>
 *            The object type of the returning temporary object
 * @param <O>
 *            The returning list object
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class DataAccessBlogConverter<I, O> extends QueryResultConverter<I, O> {

    /**
     * Getting blog object by id
     *
     * @param blogId
     *            the blog id
     * @return blog object
     */

    protected Blog getBlog(Long blogId) {
        return getBlogDao().load(blogId);
    }

    /**
     * Getting Instance of BlogDao
     *
     * @return BlogDao Instance
     */

    protected BlogDao getBlogDao() {
        return ServiceLocator.findService(BlogDao.class);
    }

}
