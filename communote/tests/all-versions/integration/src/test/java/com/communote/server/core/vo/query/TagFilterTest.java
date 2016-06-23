package com.communote.server.core.vo.query;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.logical.AtomicTagFormula;
import com.communote.server.core.vo.query.logical.CompoundTagFormula;
import com.communote.server.core.vo.query.logical.CompoundTagFormula.CompoundFormulaType;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;


/**
 * This test tests the correct filtering for multiple tags (up to 10 Tags).
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagFilterTest extends CommunoteIntegrationTest {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TagFilterTest.class);

    private final String[] tags = { UUID.randomUUID().toString(),
            UUID.randomUUID().toString(), UUID.randomUUID().toString(),
            UUID.randomUUID().toString(), UUID.randomUUID().toString(),
            UUID.randomUUID().toString(), UUID.randomUUID().toString(),
            UUID.randomUUID().toString(), UUID.randomUUID().toString(),
            UUID.randomUUID().toString() };
    private User user;
    private Blog blog;
    private int numberOfMessagesWithoutNewTag;
    private QueryManagement queryManagement;
    private int numberOfAnswers;

    /**
     * @return The number of tags each iteration should try to detect.
     */
    @DataProvider(name = "getNumberOfTags")
    public Object[][] getNumberOfTags() {
        Object[][] result = new Object[tags.length][3];
        for (int i = 1; i <= tags.length; i++) {
            NoteQuery query = new NoteQuery();
            NoteQueryParameters queryParameters = new NoteQueryParameters();
            queryParameters.setTypeSpecificExtension(new TaggingCoreItemUTPExtension());
            queryParameters.setLogicalTags(new CompoundTagFormula(
                    CompoundFormulaType.CONJUNCTION, false));
            queryParameters.setLimitResultSet(false);
            result[i - 1][0] = i;
            result[i - 1][1] = queryParameters;
            result[i - 1][2] = query;
        }
        return result;
    }

    /**
     * Creates a lot of messages with tags.
     * 
     * @param minNumberOfMessages
     *            The minimal number of message to be created with a tag.
     * @param maxNumberOfMessages
     *            The maximal number of message to be created with a tag.
     * @param numberOfAnswers
     *            The number of answers, each post should have.
     * @param numberOfThreads
     *            The maximal number of threads to use for sending message in parallel.
     */
    @Parameters({ "minNumberOfMessages", "maxNumberOfMessages", "numberOfAnswers",
            "numberOfThreads" })
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup(@Optional("20") String minNumberOfMessages,
            @Optional("40") String maxNumberOfMessages, @Optional("25") String numberOfAnswers,
            @Optional("5") String numberOfThreads) {
        LOGGER.info(
                "Configuration: minNumberOfMessage={}; maxNumberOfMessages={}; numberOfAnswers={}; numberOfThreads={}",
                new Object[] { minNumberOfMessages, maxNumberOfMessages, numberOfAnswers,
                        numberOfThreads });
        this.numberOfAnswers = Integer.parseInt(numberOfAnswers);
        user = TestUtils.createRandomUser(false);
        blog = TestUtils.createRandomBlog(true, true, user);
        numberOfMessagesWithoutNewTag = Integer.parseInt(minNumberOfMessages)
                + RandomUtils.nextInt(Integer.parseInt(maxNumberOfMessages)
                        - Integer.parseInt(minNumberOfMessages) + 1);
        String message = UUID.randomUUID().toString();
        LOGGER.info("Create {} messages with {} answers for {} tags",
                new Object[] { numberOfMessagesWithoutNewTag, numberOfAnswers, tags.length });
        ExecutorService executor = Executors.newFixedThreadPool(Integer.parseInt(numberOfThreads));
        for (String tag : tags) {
            message += " #" + tag;
            for (int i = 0; i < numberOfMessagesWithoutNewTag; i++) {
                final Long noteId = TestUtils.createAndStoreCommonNote(blog, user.getId(), message
                        + " " + i);
                for (int e = 1; e <= this.numberOfAnswers; e++) {
                    final int innerI = i;
                    final int innerE = e;
                    final String innerMessage = message;
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            TestUtils.createAndStoreCommonNote(blog, user.getId(), innerMessage
                                    + " " + innerI + "." + innerE, noteId);
                        }
                    };
                    if (numberOfThreads.equals("1")) {
                        runnable.run();
                    } else {
                        // Some performance boost on inserting.
                        executor.execute(runnable);
                    }
                }
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.warn(e.getMessage());
            }
        }
        LOGGER.info("Messages created.");
        queryManagement = ServiceLocator.instance().getService(QueryManagement.class);
    }

    /**
     * Tests the correct filtering for tags. This also requires a timeout the test should finish.
     * 
     * @param numberOfTags
     *            The number of tags this test will check.
     * @param queryParameters
     *            The query parameters this test uses.
     * @param query
     *            The query to use.
     ** 
     */
    @Test(timeOut = 10000, dataProvider = "getNumberOfTags")
    public void testTagFiltering(int numberOfTags, NoteQueryParameters queryParameters,
            NoteQuery query) {
        for (int i = 0; i < numberOfTags; i++) {
            ((CompoundTagFormula) queryParameters.getLogicalTags())
                    .addAtomicFormula(new AtomicTagFormula(tags[i], false));
        }
        AuthenticationTestUtils.setSecurityContext(user);
        List<?> pageableList = queryManagement.executeQueryComplete(query, queryParameters);
        Assert.assertEquals(pageableList.size(), (tags.length + 1 - numberOfTags)
                * (numberOfMessagesWithoutNewTag + numberOfAnswers * numberOfMessagesWithoutNewTag));
    }
}
