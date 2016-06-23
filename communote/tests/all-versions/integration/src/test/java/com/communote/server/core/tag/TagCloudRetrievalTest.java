package com.communote.server.core.tag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType.Types;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.filter.listitems.NormalizedRankListItem;
import com.communote.server.core.filter.listitems.RankTagListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagSuggestion;
import com.communote.server.core.tag.TagSuggestionManagement;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.core.vo.query.converters.RankTagListItemToRankTagListItemQueryResultConverter;
import com.communote.server.core.vo.query.tag.RankTagQueryParameters;
import com.communote.server.core.vo.query.tag.RelatedRankTagQuery;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Unittest for the Tag Cloud Related Queries and Filtering
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagCloudRetrievalTest extends CommunoteIntegrationTest {
    private User user;
    private Blog blog1;
    private String staticTag = "12";
    private Tag tag1, tag2;

    private QueryParametersParameterNameProvider nameProvider;

    /**
     * On setup create a random user that posts some note with random tags and the static tag
     */
    @BeforeTest
    public void setup() {
        user = TestUtils.createRandomUser(false);
        blog1 = TestUtils.createRandomBlog(true, true, user);
        staticTag = "12";
        String tag1 = UUID.randomUUID().toString();
        String tag2 = UUID.randomUUID().toString();
        String tag3 = UUID.randomUUID().toString();

        TestUtils.createAndStoreCommonNote(blog1, user.getId(), " #" + staticTag + " #" + tag1
                + " " + "#" + tag2);
        TestUtils.createAndStoreCommonNote(blog1, user.getId(), " #" + staticTag + "#" + tag1
                + " #" + tag2 + " #" + tag3);

        // find the tag to have the database id use it
        this.tag1 = ServiceLocator.findService(TagManagement.class).findTag(tag1, Types.NOTE);
        Assert.assertNotNull(tag1, "tag1 cannot be null!");

        // find the tag to have the database id use it
        this.tag2 = ServiceLocator.findService(TagManagement.class).findTag(tag2, Types.NOTE);
        Assert.assertNotNull(tag2, "tag2 cannot be null!");

        ServiceLocator.findService(TagManagement.class).findTag(tag3, Types.NOTE);
        Assert.assertNotNull(tag3, "tag2 cannot be null!");

        nameProvider = FilterWidgetParameterNameProvider.INSTANCE;

    }

    /**
     * Test the tag query definitions by filtering for the (database) tag id, that is using one of
     * the random created tags, execute it and check if the result is not empty
     */
    @Test
    public void testFilterTagsForTagsByTagId() {

        // let the created user do it
        AuthenticationTestUtils.setSecurityContext(user);

        // construct the query definition
        RelatedRankTagQuery definition = new RelatedRankTagQuery();
        RankTagQueryParameters instance = new RankTagQueryParameters(definition);
        instance.setResultSpecification(new ResultSpecification(0, 50));

        // filter for the (database) tag id
        instance.addTagId(tag1.getId());

        // execute the query
        PageableList<NormalizedRankListItem<RankTagListItem>> results = ServiceLocator.findService(
                QueryManagement.class).query(
                definition,
                instance,
                new RankTagListItemToRankTagListItemQueryResultConverter(Locale.ENGLISH,
                        ServiceLocator.instance().getService(TagManagement.class)));

        // check that we have results
        Assert.assertTrue(results.size() > 0, "result cannot be emty");
    }

    /**
     * Filtering for tags using the tag store id
     */
    @Test
    public void testFilterTagsForTagsByTagStoreId() {

        // let the user do it
        AuthenticationTestUtils.setSecurityContext(user);

        // construct the query definition / instance
        RelatedRankTagQuery definition = new RelatedRankTagQuery();
        RankTagQueryParameters instance = new RankTagQueryParameters(definition);
        instance.setResultSpecification(new ResultSpecification(0, 50));

        // set the tag store id
        instance.addTagStoreTagId(Types.NOTE.getDefaultTagStoreId(), "12");

        // exectue the query
        PageableList<NormalizedRankListItem<RankTagListItem>> results = ServiceLocator.findService(
                QueryManagement.class).query(
                definition,
                instance,
                new RankTagListItemToRankTagListItemQueryResultConverter(Locale.ENGLISH,
                        ServiceLocator.instance().getService(TagManagement.class)));

        // assert some results
        Assert.assertTrue(results.size() > 0, "result cannot be emty");
    }

    /**
     * Test the tag query definitions by filtering for the two (database) tag id, that is using one
     * of the random created tags, execute it and check if the result is not empty
     */
    @Test
    public void testFilterTagsForTagsByTwoTagIds() {

        // let the created user do it
        AuthenticationTestUtils.setSecurityContext(user);

        // construct the query definition
        RelatedRankTagQuery definition = new RelatedRankTagQuery();
        RankTagQueryParameters instance = new RankTagQueryParameters(definition);
        instance.setResultSpecification(new ResultSpecification(0, 50));

        // filter for the (database) tag id
        instance.addTagId(tag1.getId());
        // filter for the (database) tag id
        instance.addTagId(tag2.getId());

        // execute the query
        PageableList<NormalizedRankListItem<RankTagListItem>> results = ServiceLocator.findService(
                QueryManagement.class).query(
                definition,
                instance,
                new RankTagListItemToRankTagListItemQueryResultConverter(Locale.ENGLISH,
                        ServiceLocator.instance().getService(TagManagement.class)));

        // check that we have results
        Assert.assertTrue(results.size() > 0, "result cannot be emty");
    }

    /**
     * Test the tag suggestions by filtering for tag and get suggestions for a tag prefix.
     *
     * The tag suggestions should only contain tags
     */
    @Test
    public void testFilterTagsForTagSuggestions() {

        // let the created user do it
        AuthenticationTestUtils.setSecurityContext(user);

        // the prefix is a part of tag2
        String prefix = this.tag2.getDefaultName().substring(0, 3);

        // set up the filter parameters
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(nameProvider.getNameForTagPrefix(), prefix);

        // filter for tag1
        parameters.put(nameProvider.getNameForTagIds(), tag1.getId() + "");

        Collection<TagSuggestion> suggestions = ServiceLocator
                .instance()
                .getService(TagSuggestionManagement.class)
                .findTagSuggestions(Types.NOTE, null, null, true, parameters,
                        FilterWidgetParameterNameProvider.INSTANCE, Locale.ENGLISH);

        boolean tag2found = false;
        sug: for (TagSuggestion tagSuggestion : suggestions) {
            for (TagData tag : tagSuggestion.getTags()) {
                if (tag.getId().equals(tag2.getId())) {
                    tag2found = true;
                    break sug;
                }
                Assert.assertTrue(tag.getDefaultName().startsWith(prefix), " The returned tag "
                        + tag + " with name " + tag.getDefaultName()
                        + " does not start with the tag prefix " + prefix);
            }
        }
        Assert.assertTrue(tag2found, " Did not find tag " + tag2 + " by prefix " + prefix
                + " in TagSuggestions!");

    }

}
