package com.communote.server.core.vo.query.java.note;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.matcher.Matcher;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.core.vo.query.filter.PropertyFilter;
import com.communote.server.core.vo.query.filter.PropertyFilter.MatchMode;
import com.communote.server.core.vo.query.java.note.PropertyFilterMatcher;
import com.communote.server.model.note.Note;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PropertyFilterMatcherTest {
    /**
     * 
     * @param propertyManagement
     *            the mocked property management
     * @param note
     *            the note to use
     * @param keyGroup
     *            the keygroup to use
     * @param key
     *            the key to use
     * @param valueCore
     *            the value to use
     */
    private void testContains(PropertyManagement propertyManagement, NoteData note,
            String keyGroup, String key, String valueCore) {
        Matcher<NoteData> matcher;
        // Contains and !Contains
        PropertyFilter filter = new PropertyFilter(keyGroup, Note.class);
        filter.addProperty(key, valueCore, MatchMode.CONTAINS);
        matcher = new PropertyFilterMatcher(propertyManagement, filter);
        Assert.assertTrue(matcher.matches(note));

        filter = new PropertyFilter(keyGroup, Note.class, true);
        filter.addProperty(key, valueCore, MatchMode.CONTAINS);
        matcher = new PropertyFilterMatcher(propertyManagement, filter);
        Assert.assertFalse(matcher.matches(note));
    }

    /**
     * 
     * @param propertyManagement
     *            the mocked property management
     * @param note
     *            the note to use
     * @param keyGroup
     *            the keygroup to use
     * @param key
     *            the key to use
     */
    private void testExists(PropertyManagement propertyManagement, NoteData note,
            String keyGroup, String key) {
        Matcher<NoteData> matcher;
        PropertyFilter filter;
        // Exists
        filter = new PropertyFilter(keyGroup, Note.class);
        filter.addProperty(key, null, MatchMode.EXISTS);
        matcher = new PropertyFilterMatcher(propertyManagement, filter);
        Assert.assertTrue(matcher.matches(note));

        // !Exists
        filter = new PropertyFilter(keyGroup, Note.class, true);
        filter.addProperty(key, null, MatchMode.EXISTS);
        matcher = new PropertyFilterMatcher(propertyManagement, filter);
        Assert.assertFalse(matcher.matches(note));
    }

    /**
     * The test.
     * 
     * @throws Exception
     *             The test should fail on any thrown exception.
     */
    @Test
    public void testMatches() throws Exception {
        NoteData note = new NoteData();
        note.setId(0L);
        String keyGroup = UUID.randomUUID().toString();
        String key = UUID.randomUUID().toString();
        String valuePrefix = UUID.randomUUID().toString();
        String valueCore = UUID.randomUUID().toString();
        String valueSuffix = UUID.randomUUID().toString();
        String value = valuePrefix + valueCore + valueSuffix;
        StringPropertyTO property = new StringPropertyTO(value, keyGroup, key, new Date());
        Set<StringPropertyTO> properties = new HashSet<StringPropertyTO>();
        properties.add(property);
        PropertyManagement propertyManagement = EasyMock.createMock(PropertyManagement.class);
        EasyMock.expect(propertyManagement.getAllObjectProperties(PropertyType.NoteProperty, 0L))
                .andReturn(properties).anyTimes();
        EasyMock.replay(propertyManagement);

        Matcher<NoteData> matcher = new PropertyFilterMatcher(propertyManagement);
        Assert.assertTrue(matcher.matches(note));

        PropertyFilter filter;
        testContains(propertyManagement, note, keyGroup, key, valueCore);

        // Starts With and !Starts With
        filter = new PropertyFilter(keyGroup, Note.class);
        filter.addProperty(key, valuePrefix, MatchMode.STARTS_WITH);
        matcher = new PropertyFilterMatcher(propertyManagement, filter);
        Assert.assertTrue(matcher.matches(note));

        filter = new PropertyFilter(keyGroup, Note.class, true);
        filter.addProperty(key, valuePrefix, MatchMode.STARTS_WITH);
        matcher = new PropertyFilterMatcher(propertyManagement, filter);
        Assert.assertFalse(matcher.matches(note));

        // Ends With and !Ends With
        filter = new PropertyFilter(keyGroup, Note.class);
        filter.addProperty(key, valueSuffix, MatchMode.ENDS_WITH);
        matcher = new PropertyFilterMatcher(propertyManagement, filter);
        Assert.assertTrue(matcher.matches(note));

        filter = new PropertyFilter(keyGroup, Note.class, true);
        filter.addProperty(key, valueSuffix, MatchMode.ENDS_WITH);
        matcher = new PropertyFilterMatcher(propertyManagement, filter);
        Assert.assertFalse(matcher.matches(note));

        // Equals and !Equals
        filter = new PropertyFilter(keyGroup, Note.class);
        filter.addProperty(key, value, MatchMode.EQUALS);
        matcher = new PropertyFilterMatcher(propertyManagement, filter);
        Assert.assertTrue(matcher.matches(note));

        filter = new PropertyFilter(keyGroup, Note.class, true);
        filter.addProperty(key, value, MatchMode.EQUALS);
        matcher = new PropertyFilterMatcher(propertyManagement, filter);
        Assert.assertFalse(matcher.matches(note));

        // Not Equals and !Not Equals
        filter = new PropertyFilter(keyGroup, Note.class);
        filter.addProperty(key, value, MatchMode.NOT_EQUALS);
        matcher = new PropertyFilterMatcher(propertyManagement, filter);
        Assert.assertFalse(matcher.matches(note));

        filter = new PropertyFilter(keyGroup, Note.class, true);
        filter.addProperty(key, value, MatchMode.NOT_EQUALS);
        matcher = new PropertyFilterMatcher(propertyManagement, filter);
        Assert.assertTrue(matcher.matches(note));

        // Combined AND
        PropertyFilter filter1 = new PropertyFilter(keyGroup, Note.class);
        filter1.addProperty(key, valueCore, MatchMode.CONTAINS);
        PropertyFilter filter2 = new PropertyFilter(keyGroup, Note.class);
        filter2.addProperty(key, valuePrefix, MatchMode.STARTS_WITH);
        PropertyFilter filter3 = new PropertyFilter(keyGroup, Note.class);
        filter3.addProperty(key, valueSuffix, MatchMode.ENDS_WITH);
        matcher = new PropertyFilterMatcher(propertyManagement, filter1, filter2, filter3);
        Assert.assertTrue(matcher.matches(note));

        filter1 = new PropertyFilter(keyGroup, Note.class);
        filter1.addProperty(key, valueCore, MatchMode.CONTAINS);
        filter2 = new PropertyFilter(keyGroup, Note.class);
        filter2.addProperty(key, UUID.randomUUID().toString(), MatchMode.STARTS_WITH);
        matcher = new PropertyFilterMatcher(propertyManagement, filter1, filter2);
        Assert.assertFalse(matcher.matches(note));

        // Simple OR
        filter = new PropertyFilter(keyGroup, Note.class);
        filter.addProperty(key, UUID.randomUUID().toString(), MatchMode.EQUALS);
        matcher = new PropertyFilterMatcher(propertyManagement, filter);
        Assert.assertFalse(matcher.matches(note));
        filter.addProperty(key, value, MatchMode.EQUALS);
        Assert.assertTrue(matcher.matches(note));

        testExists(propertyManagement, note, keyGroup, key);
    }
}
