package com.communote.common.matcher;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link OrMatcher}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class OrMatcherTest {
    /**
     * Tests {@link AndMatcher}
     */
    @Test
    public void test() {
        OrMatcher<Long> matcher = new OrMatcher<Long>();
        Assert.assertTrue(matcher.matches(0L));
        matcher.addMatcher(new SmallerMatcher(-1L));
        Assert.assertFalse(matcher.matches(0L));
        matcher.addMatcher(new SmallerMatcher(0L));
        Assert.assertFalse(matcher.matches(0L));
        matcher.addMatcher(new SmallerMatcher(1L));
        Assert.assertTrue(matcher.matches(0L));
    }
}
