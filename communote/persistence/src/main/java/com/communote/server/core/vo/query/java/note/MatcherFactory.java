package com.communote.server.core.vo.query.java.note;

import org.hibernate.criterion.MatchMode;

import com.communote.common.matcher.AndMatcher;
import com.communote.common.matcher.Matcher;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.core.blog.FavoriteManagement;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.service.NoteService;

/**
 * Factory for Matcher.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MatcherFactory {

    /**
     * Method to create a matcher for the given parameters.
     * 
     * @param parameters
     *            The parameters containing the set filters.
     * @return A matcher for the given parameters.
     */
    public static Matcher<NoteData> createMatcher(NoteQueryParameters parameters) {
        AndMatcher<NoteData> matcher = new AndMatcher<NoteData>();
        matcher.addMatcher(new TopicIdsMatcher(parameters.getTypeSpecificExtension()
                .getBlogFilter()));
        matcher.addMatcher(new TagIdsMatcher(parameters.getTagIds()));
        matcher.addMatcher(new UserIdsMatcher(parameters.getUserIds()));
        matcher.addMatcher(new NoteIdMatcher(parameters.getNoteId()));
        matcher.addMatcher(new DateRangeMatcher(parameters.getLowerTagDate(), parameters
                .getUpperTagDate()));
        matcher.addMatcher(new FullTextSearchFiltersMatcher(ServiceLocator.instance().getService(
                NoteService.class), MatchMode.ANYWHERE, parameters.getFullTextSearchFilters()));
        matcher.addMatcher(new UsersToBeNotifiedMatcher(parameters.getUserToBeNotified()));
        matcher.addMatcher(new PropertyFilterMatcher(ServiceLocator.instance().getService(
                PropertyManagement.class), parameters.getPropertyFilters()));
        if (parameters.isRetrieveOnlyFollowedItems()) {
            matcher.addMatcher(new FollowingMatcher(ServiceLocator.instance().getService(
                    FollowManagement.class)));
        }
        if (parameters.isFavorites()) {
            matcher.addMatcher(new FavoriteMatcher(ServiceLocator.instance().getService(
                    FavoriteManagement.class)));
        }
        return matcher;
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private MatcherFactory() {
        // Do nothing
    }
}
