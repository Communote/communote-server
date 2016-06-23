package com.communote.server.test;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.communote.server.core.query.QueryManagement;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.test.CommunoteIntegrationTest;


/**
 * A test for {@link CommunoteIntegrationTest}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteIntegrationTestTest extends CommunoteIntegrationTest {

    @Autowired
    private QueryManagement queryManagement;
    @Autowired
    private NoteDao noteDao;

    /**
     * Simple test, to test that services and daos can be injected via @Autowired.
     */
    @Test
    public void testAutowiring() {
        Assert.assertNotNull(queryManagement);
        Assert.assertNotNull(noteDao);
    }
}
