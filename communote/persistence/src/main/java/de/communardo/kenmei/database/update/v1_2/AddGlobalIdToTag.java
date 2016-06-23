package de.communardo.kenmei.database.update.v1_2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.model.tag.Tag;
import com.communote.server.persistence.tag.TagDao;

/**
 * Update task that creates a global ID for tag entities that do not have one. Wrong globalIds
 * should be removed from tags before running this task.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AddGlobalIdToTag implements CustomTaskChange {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Database database) throws CustomChangeException, UnsupportedChangeException {
        // get Tag with highest id
        TagDao dao = ServiceLocator.findService(TagDao.class);
        Long tagId = null;
        Statement statement;
        try {
            statement = database.getConnection().createStatement();
            statement.execute("SELECT id FROM core_tag ORDER BY id DESC LIMIT 1");
            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                tagId = resultSet.getLong(1);
            }
        } catch (SQLException e) {
            throw new CustomChangeException(e);
        }
        TagManagement tagManagement = ServiceLocator.instance().getService(TagManagement.class);
        if (tagId != null) {
            for (long i = 0; i <= tagId; i++) {
                Tag tagToUpdate = dao.load(i);
                if (tagToUpdate != null) {
                    tagManagement.assignGlobalIdForTag(tagToUpdate);
                }
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
    public void setFileOpener(FileOpener fileOpener) {
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
    public void validate(Database database) throws InvalidChangeDefinitionException {
        // nothing

    }

}
