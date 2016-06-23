package com.communote.server.core.vo.query.java.note;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;


/**
 * Matcher for
 * {@link com.communote.server.core.vo.query.TimelineQueryParameters#getUserAliases()}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserAliasesMatcher extends Matcher<NoteData> {

    private final String[] userAliases;

    /**
     * Constructor.
     * 
     * @param userAliases
     *            Array of user alias, might be null or empty, where every note matches.
     */
    public UserAliasesMatcher(String... userAliases) {
        this.userAliases = userAliases;
    }

    /**
     * {@inheritDoc}
     * 
     * @return True, if the author is in the list of user aliases or no list was given (all authors
     *         are allowed).
     * 
     */
    @Override
    public boolean matches(NoteData entity) {
        if (userAliases == null || userAliases.length == 0) {
            return true; // all authors are allowed.
        }
        for (String userAlias : userAliases) {
            if (userAlias.equals(entity.getUser().getAlias())) {
                return true;
            }
        }
        return false;
    }
}
