package com.communote.server.core.blog.helper;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.core.blog.BlogIdentifierValidationException;

/**
 * Test to check the validation of topic name identifiers.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class BlogManagementHelperTest {

    private boolean isValid(String identifier) {
        try {
            BlogManagementHelper.validateNameIdentifier(identifier);
            return true;
        } catch (BlogIdentifierValidationException e) {
            return false;
        }
    }

    @Test
    public void testInvalidNameIdentifier() {

        Assert.assertFalse(isValid(""));
        Assert.assertFalse(isValid(" "));
        Assert.assertFalse(isValid("\t"));
        Assert.assertFalse(isValid("  "));
        Assert.assertFalse(isValid(" abc"));
        Assert.assertFalse(isValid("abc "));
        Assert.assertFalse(isValid("abc def"));
        Assert.assertFalse(isValid("abc!def"));
        Assert.assertFalse(isValid("abc&def"));
        Assert.assertFalse(isValid("toolongidentifer.toolongidentifer.toolongidentifer.toolongidentifer."));
        Assert.assertFalse(isValid("a_"));
        Assert.assertFalse(isValid("a."));
        Assert.assertFalse(isValid("a-"));

    }

    /**
     * 
     */
    @Test
    public void testValidNameIdentifier() {

        Assert.assertTrue(isValid("a"));
        Assert.assertTrue(isValid("abc"));
        Assert.assertTrue(isValid("somtext.point"));
        Assert.assertTrue(isValid("a-b-c"));
        Assert.assertTrue(isValid("a-b-c"));
        Assert.assertTrue(isValid("a.b-c"));
        Assert.assertTrue(isValid("a.b"));
        Assert.assertTrue(isValid("a_b"));
        Assert.assertTrue(isValid("a-b"));
        Assert.assertTrue(isValid("_a"));
        Assert.assertTrue(isValid(".a"));
        Assert.assertTrue(isValid("-a"));

        Assert.assertTrue(isValid("verylongidentifier"));

    }

}
