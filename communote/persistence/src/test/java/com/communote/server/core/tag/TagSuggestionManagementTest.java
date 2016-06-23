package com.communote.server.core.tag;

import java.util.Collection;
import java.util.HashSet;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.core.tag.TagStoreType.Types;

/**
 * Test for {@link TagSuggestionManagement}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagSuggestionManagementTest {

    private TagSuggestionManagement tagSuggestionManagement;

    /**
     * Setup.
     */
    @BeforeClass
    public void setup() {
        TagSuggestionProvider provider1 = new TestTagSuggestionsProvider("1", Types.NOTE,
                new TagSuggestion("1_1", "1_1", "msk1", null), new TagSuggestion("1_2", "1_2",
                        "msk1", null));
        TagSuggestionProvider provider2 = new TestTagSuggestionsProvider("2", Types.NOTE,
                new TagSuggestion("2_1", "2_1", "msk2", null), new TagSuggestion("2_2", "2_2",
                        "msk2", null));
        tagSuggestionManagement = new TagSuggestionManagement();
        tagSuggestionManagement.addTagSuggestionProvider(provider1);
        tagSuggestionManagement.addTagSuggestionProvider(provider2);
    }

    /**
     * Test that all known providers and suggestions are searched if no aliases are provided
     * (Regression test for KENMEI-4411)
     */
    @Test
    public void testForEmptyProviderAliasList() {
        Collection<TagSuggestion> tagSuggestions = tagSuggestionManagement.findTagSuggestions(
                Types.NOTE, null, null, false, null, null, null);
        Assert.assertEquals(tagSuggestions.size(), 4);
        tagSuggestions = tagSuggestionManagement.findTagSuggestions(Types.NOTE,
                new HashSet<String>(), new HashSet<String>(), false, null, null, null);
        Assert.assertEquals(tagSuggestions.size(), 4);
    }

    /**
     * Tests the filtering of tag suggestion providers.
     */
    @Test
    public void testProviderFiltering() {
        Collection<String> providerAliases = new HashSet<String>();
        providerAliases.add("1");
        Collection<TagSuggestion> tagSuggestions = tagSuggestionManagement.findTagSuggestions(
                Types.NOTE, providerAliases, null, false, null, null, null);
        Assert.assertEquals(tagSuggestions.size(), 2);
        for (TagSuggestion tagSuggestion : tagSuggestions) {
            Assert.assertTrue(tagSuggestion.getAlias().startsWith("1_"));
        }

        providerAliases.add("2");
        tagSuggestions = tagSuggestionManagement.findTagSuggestions(Types.NOTE, providerAliases,
                null, false, null, null, null);
        Assert.assertEquals(tagSuggestions.size(), 4);

        providerAliases.remove("1");
        tagSuggestions = tagSuggestionManagement.findTagSuggestions(Types.NOTE, providerAliases,
                null, false, null, null, null);
        Assert.assertEquals(tagSuggestions.size(), 2);
        for (TagSuggestion tagSuggestion : tagSuggestions) {
            Assert.assertTrue(tagSuggestion.getAlias().startsWith("2_"));
        }
    }

    /**
     * Tests the filtering of tag suggestions aliases.
     */
    @Test
    public void testSuggestionAliasFiltering() {
        Collection<String> aliases = new HashSet<String>();
        aliases.add("1_1");
        Collection<TagSuggestion> tagSuggestions = tagSuggestionManagement.findTagSuggestions(
                Types.NOTE, null, aliases, false, null, null, null);
        Assert.assertEquals(tagSuggestions.size(), 1);
        for (TagSuggestion tagSuggestion : tagSuggestions) {
            Assert.assertTrue(tagSuggestion.getAlias().equals("1_1"));
        }
    }
}
