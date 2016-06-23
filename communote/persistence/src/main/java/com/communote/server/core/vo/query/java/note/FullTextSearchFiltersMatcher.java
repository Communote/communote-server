package com.communote.server.core.vo.query.java.note;

import org.hibernate.criterion.MatchMode;

import com.communote.common.converter.Converter;
import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.model.note.Note;
import com.communote.server.service.NoteService;

/**
 * <p>
 * Matcher for
 * {@link com.communote.server.core.vo.query.TimelineQueryParameters#getFullTextSearchFilters()} .
 * </p>
 * <b>Note</b>: This currently only searches the content and converts everything to lower case.
 * Results also may differ from database results.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FullTextSearchFiltersMatcher extends Matcher<NoteData> {

    private final String[] fullTextSearchFilters;
    private final MatchMode matchMode;
    private final NoteService noteService;

    /**
     * Constructor.
     * 
     * @param noteService
     *            Service used to get the note.
     * @param matchMode
     *            The match mode to use.
     * @param fullTextSearchFilters
     *            The search strings.
     */
    public FullTextSearchFiltersMatcher(NoteService noteService, MatchMode matchMode,
            String... fullTextSearchFilters) {
        this.noteService = noteService;
        this.matchMode = matchMode;
        this.fullTextSearchFilters = fullTextSearchFilters;
    }

    /**
     * {@inheritDoc}
     * 
     * @return True, if all search strings match or if no tags are given to match.
     * 
     */
    @Override
    public boolean matches(NoteData entity) {
        if (fullTextSearchFilters == null || fullTextSearchFilters.length == 0) {
            return true;
        }
        String content = noteService.getNote(entity.getId(), new Converter<Note, String>() {
            @Override
            public String convert(Note source) {
                return source.getContent().getContent().toLowerCase();
            }
        });
        boolean matches = false;
        if (content != null) {
            for (String searchFilter : fullTextSearchFilters) {
                searchFilter = searchFilter.toLowerCase();
                if (MatchMode.ANYWHERE.equals(matchMode)) {
                    matches = content.contains(searchFilter);
                } else if (MatchMode.START.equals(matchMode)) {
                    matches = content.startsWith(searchFilter);
                } else if (MatchMode.END.equals(matchMode)) {
                    matches = content.endsWith(searchFilter);
                } else if (MatchMode.EXACT.equals(matchMode)) {
                    matches = content.equals(searchFilter);
                }
                if (!matches) {
                    return false;
                }
            }
        }
        return matches;
    }
}
