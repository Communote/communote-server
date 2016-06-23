package com.communote.common.matcher;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link AndMatcher}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AndMatcherTest {
    /**
     * Tests {@link AndMatcher}
     */
    @Test
    public void test() {
        AndMatcher<Long> matcher = new AndMatcher<Long>();
        Assert.assertTrue(matcher.matches(0L));
        matcher.addMatcher(new SmallerMatcher(1L));
        Assert.assertTrue(matcher.matches(0L));
        matcher.addMatcher(new SmallerMatcher(10L));
        Assert.assertTrue(matcher.matches(0L));
        matcher.addMatcher(new SmallerMatcher(0L));
        Assert.assertFalse(matcher.matches(0L));
    }
}
