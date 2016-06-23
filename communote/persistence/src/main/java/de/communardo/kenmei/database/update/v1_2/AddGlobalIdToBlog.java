package de.communardo.kenmei.database.update.v1_2;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.general.RunInTransaction;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.global.GlobalId;
import com.communote.server.persistence.blog.BlogDao;
import com.communote.server.persistence.global.GlobalIdDao;

/**
 * Update task that creates a global ID for blog entities that do not have one.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AddGlobalIdToBlog implements CustomTaskChange {

    /**
     * Helper class to update global id of a blog within a transaction
     */
    private class UpdateGlobalIdInTransactionTask implements RunInTransaction {
        private Long blogId;
        private final BlogDao blogDao = ServiceLocator.findService(BlogDao.class);
        private final GlobalIdDao gidDao = ServiceLocator.findService(GlobalIdDao.class);

        /**
         * {@inheritDoc}
         */
        @Override
        public void execute() throws TransactionException {
            Blog blogToUpdate = blogDao.load(blogId);
            if (blogToUpdate != null && blogToUpdate.getGlobalId() == null) {
                GlobalId globalId = gidDao.createGlobalId(blogToUpdate);
                blogToUpdate.setGlobalId(globalId);
            }
        }

        /**
         * @param blogId
         *            ID of blog to be processed
         */
        void setBlogId(Long blogId) {
            this.blogId = blogId;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Database arg0) throws CustomChangeException, UnsupportedChangeException {
        TransactionManagement tm = ServiceLocator.findService(TransactionManagement.class);
        // get Blog with highest id
        BlogDao dao = ServiceLocator.findService(BlogDao.class);
        Blog blog = dao.findLatestBlog();
        if (blog != null) {
            long max = blog.getId();
            UpdateGlobalIdInTransactionTask task = new UpdateGlobalIdInTransactionTask();
            for (long i = 0; i <= max; i++) {
                task.setBlogId(i);
                tm.execute(task);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFileOpener(FileOpener arg0) {
        // nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws SetupException {
        // nothing

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Database arg0) throws InvalidChangeDefinitionException {
        // nothing

    }

}
