package com.communote.server.service.common.data;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.util.VersionComparator;

/**
 * Tests for {@link ApplicationVersion}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ApplicationVersionTest {
    @Test
    public void testCompareVersions() {
        String version = "1";
        VersionComparator revisionVersionComp = new VersionComparator(true, true);
        VersionComparator versionComp = new VersionComparator();
        for (int i = 0; i < RandomUtils.nextInt(10) + 5; i++) {
            version += "." + i;
            Assert.assertEquals(revisionVersionComp.compare(version, version), 0);
            Assert.assertEquals(versionComp.compare(version, version), 0);
        }
        // With Revision
        Assert.assertTrue(revisionVersionComp.compare(version + ".1", version + ".2") < 0);
        Assert.assertTrue(revisionVersionComp.compare(version + ".2", version + ".1") > 0);
        Assert.assertTrue(revisionVersionComp.compare(version + ".1", version) > 0);
        Assert.assertTrue(revisionVersionComp.compare(version, version + ".1") < 0);
        Assert.assertTrue(revisionVersionComp.compare("2.1.4", "2.1.0.5") < 0);
        Assert.assertEquals(revisionVersionComp.compare("2.1.0.4", "2.1.0.0.4"), 0);
        Assert.assertEquals(revisionVersionComp.compare("2.1.0.4", "2.1.4"), 0);
        Assert.assertTrue(revisionVersionComp.compare("2.1.0", "2.1.0.0.0.0.0.0.1") < 0);
        Assert.assertTrue(revisionVersionComp.compare("2.1.1.11480", "2.1.9617") > 0); // KENMEI-4615
        Assert.assertTrue(revisionVersionComp.compare("2.1.0.abcde", "2.1.defgh") == 0);
        Assert.assertTrue(revisionVersionComp.compare("3.0.1.012345", "3.0.1.0abcde") > 0);
        
        revisionVersionComp = new VersionComparator(true, false);
        Assert.assertEquals(revisionVersionComp.compare("3.0.1.012345", "3.0.1.0abcde"), 0); // KENMEI-6778
        Assert.assertEquals(revisionVersionComp.compare("3.0.1.12345", "3.0.1.5678"), 0);

        // Without Revision
        Assert.assertTrue(versionComp.compare("1.1", "1.0.1") > 0); // KENMEI-4688
        Assert.assertTrue(versionComp.compare("1.0", "1.0.0") == 0);
        Assert.assertTrue(versionComp.compare("1.0.1", "1.1") < 0);
    }
}
