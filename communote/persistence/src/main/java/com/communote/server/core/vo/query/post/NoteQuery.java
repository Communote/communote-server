package com.communote.server.core.vo.query.post;

import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.model.note.NoteConstants;


/**
 * This query definition represents queries for notes.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteQuery extends AbstractNoteQuery<SimpleNoteListItem> {

    /**
     * the parameters of the list item constructor
     */
    private String[] constructorParameters;

    /**
     * Create a new empty query instance
     * 
     * @return the instance
     */
    @Override
    public NoteQueryParameters createInstance() {
        return new NoteQueryParameters();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getConstructorParameters() {
        return constructorParameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getConstructorParametersForRootNotes() {
        return new String[] {
                getRootNoteAliasPrefix() + NoteConstants.ID,
                getRootNoteAliasPrefix() + NoteConstants.LASTDISCUSSIONNOTECREATIONDATE
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<SimpleNoteListItem> getResultListItem() {
        return SimpleNoteListItem.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean needUserInQuery(NoteQueryParameters queryInstance) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupQueries() {
        constructorParameters = new String[] {
                getNoteAlias() + NoteConstants.ID,
                getNoteAlias() + NoteConstants.CREATIONDATE
        };
        super.setupQueries();
    }
}
