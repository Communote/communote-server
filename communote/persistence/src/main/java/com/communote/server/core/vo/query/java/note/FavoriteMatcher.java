package com.communote.server.core.vo.query.java.note;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.core.blog.FavoriteManagement;


/**
 * Matcher to handle
 * {@link com.communote.server.core.vo.query.TimelineQueryParameters#isFavorites()}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FavoriteMatcher extends Matcher<NoteData> {

    private final FavoriteManagement favoriteManagement;

    /**
     * Constructor.
     * 
     * @param favoriteManagement
     *            The {@link FavoriteManagement} to use for checking, if the note is a favorite.
     */
    public FavoriteMatcher(FavoriteManagement favoriteManagement) {
        this.favoriteManagement = favoriteManagement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(NoteData entity) {
        return favoriteManagement.isFavorite(entity.getId());
    }
}
