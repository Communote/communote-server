package com.communote.server.core.vo.query.filter;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.config.FilterApiParameterNameProvider;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PropertyFilterCommunoteIntegrationTest extends CommunoteIntegrationTest {
    /**
     * Test that filtering for properties is possible
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testPropertyFiltering() throws Exception {
        String propertyKeyGroup = random();
        String propertyKey = random();
        String propertyValue = random();
        ServiceLocator.instance().getService(PropertyManagement.class)
        .addObjectPropertyFilter(PropertyType.NoteProperty, propertyKeyGroup, propertyKey);
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(false, false, user);
        NoteStoringTO noteWithProperty = TestUtils.createCommonNote(blog, user.getId());
        noteWithProperty.setProperties(new HashSet<StringPropertyTO>());
        noteWithProperty.getProperties().add(
                new StringPropertyTO(propertyValue, propertyKeyGroup, propertyKey, new Date()));
        Long noteWithPropertyId = ServiceLocator.instance().getService(NoteService.class)
                .createNote(noteWithProperty, null).getNoteId();
        Long noteWithoutPropertyId = TestUtils.createAndStoreCommonNote(blog, user.getId(),
                random());
        QueryParametersParameterNameProvider nameProvider = new FilterApiParameterNameProvider();
        Map<String, String> parameters = new HashMap<String, String>();

        // Filter for the blog
        parameters.put(nameProvider.getNameForBlogIds(), Long.toString(blog.getId()));
        TimelineQueryParametersConfigurator<NoteQueryParameters> configurator = new TimelineQueryParametersConfigurator<NoteQueryParameters>(
                nameProvider);
        NoteQuery query = new NoteQuery();
        NoteQueryParameters queryInstance = query.createInstance();
        configurator.configure(parameters, queryInstance);
        QueryManagement queryManagement = ServiceLocator.instance().getService(
                QueryManagement.class);

        // No property filtering
        PageableList<SimpleNoteListItem> result = queryManagement.query(query, queryInstance);
        Assert.assertEquals(2, result.size());

        // [0:Property, 1:Group, 2:Key, 3:Value, 4:MatchMode, 5:Negate (Optional)]
        // Filtering for the property
        String[] propertyFilter = new String[] { "Note", propertyKeyGroup, propertyKey,
                propertyValue, PropertyFilter.MatchMode.EQUALS.name() };
        parameters.put(nameProvider.getNameForPropertyFilter(),
                "[\"" + StringUtils.join(propertyFilter, "\",\"") + "\"]");
        configurator.configure(parameters, queryInstance);
        result = queryManagement.query(query, queryInstance);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(noteWithPropertyId, result.get(0).getId());

        // Filtering for the property exclusion
        queryInstance.getPropertyFilters().clear();
        propertyFilter = new String[] { "Note", propertyKeyGroup, propertyKey, "",
                PropertyFilter.MatchMode.CONTAINS.name(), "true" };
        parameters.put(nameProvider.getNameForPropertyFilter(),
                "[\"" + StringUtils.join(propertyFilter, "\",\"") + "\"]");
        configurator.configure(parameters, queryInstance);
        result = queryManagement.query(query, queryInstance);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(noteWithoutPropertyId, result.get(0).getId());
    }
}
