package com.communote.plugins.activity.core;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.export.PermalinkGenerationManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ManagerGainedTopicAccessRightsChangedEventListenerTest extends
        CommunoteIntegrationTest {

    @Autowired
    private UserManagement userManagement;
    @Autowired
    private PermalinkGenerationManagement permalinkGenerationManagement;
    @Autowired
    private BlogManagement topicManagement;
    @Autowired
    private BlogRightsManagement topicRightsManagement;
    @Autowired
    private NoteDao noteDao;
    @Autowired
    private EventDispatcher eventDispatcher;

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getBundlePathsWithinMavenRepository() {
        return new String[] {
                "org/apache/felix/org.apache.felix.ipojo.annotations"
                        + "/1.8.2/org.apache.felix.ipojo.annotations-1.8.2.jar",
                "org/apache/felix/org.apache.felix.ipojo/1.8.2/org.apache.felix.ipojo-1.8.2.jar",
                "com/communote/plugins/communote-plugins-core/" + COMMUNOTE_VERSION
                        + "/communote-plugins-core-" + COMMUNOTE_VERSION + ".jar",
                "com/communote/plugins/communote-plugin-activity-base"
                        + "/" + COMMUNOTE_VERSION
                        + "/communote-plugin-activity-base-" + COMMUNOTE_VERSION + ".jar",
                "com/communote/plugins/communote-plugin-activity-core"
                        + "/" + COMMUNOTE_VERSION
                        + "/communote-plugin-activity-core-" + COMMUNOTE_VERSION + ".jar"
        };
    }

    /**
     * This method tests, that the needed activity was created when the manager gained access.
     * 
     * @throws AuthorizationException
     *             The test should fail when this exception is thrown.
     * @throws BlogNotFoundException
     *             The test should fail when this exception is thrown.
     */
    @Test
    public void testActivityCreated() throws BlogNotFoundException, AuthorizationException {
        User user = TestUtils.createRandomUser(false);
        User manager = TestUtils.createRandomUser(true);
        Blog topic = TestUtils.createRandomBlog(false, false, user);

        Long lastNoteId = TestUtils.createAndStoreCommonNote(topic, user.getId(), "Test");
        AuthenticationTestUtils.setSecurityContext(manager);
        topicRightsManagement.assignManagementAccessToCurrentUser(topic.getId());

        Note note = noteDao.load(lastNoteId + 1);
        // TODO assert that note is an activity
        Assert.assertNotNull(note);
    }
}
