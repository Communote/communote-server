package com.communote.common.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test for {@link FileHelper}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FileHelperTest {

    /** Logger. */
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
            .getLogger(FileHelperTest.class);
    private int maxLineCount;
    private String lessLinesFilePath;
    private String exactLinesFilePath;

    private String moreLinesFilePath;

    /**
     * Removes the used files.
     */
    @AfterClass
    public void clean() {
        FileUtils.deleteQuietly(new File(lessLinesFilePath));
        FileUtils.deleteQuietly(new File(exactLinesFilePath));
        FileUtils.deleteQuietly(new File(moreLinesFilePath));
    }

    /**
     * Setup.
     *
     * @throws Exception
     *             Exception.
     */
    @BeforeClass
    public void setup() throws Exception {
        maxLineCount = 400 + new Random().nextInt(100);
        File lessLinesFile = File.createTempFile("lessLines", ".txt");
        File exactLinesFile = File.createTempFile("exactLines", ".txt");
        File moreLinesFile = File.createTempFile("moreLines", ".txt");
        Collection<String> lines = new ArrayList<String>();
        for (int i = 1; i < maxLineCount; i++) {
            lines.add("Line " + i);
        }
        FileUtils.writeLines(lessLinesFile, lines);
        lines.add("Line " + maxLineCount);
        FileUtils.writeLines(exactLinesFile, lines);
        for (int i = 1; i < 5 + new Random().nextInt(10); i++) {
            lines.add("Line " + (maxLineCount + i));
        }
        FileUtils.writeLines(moreLinesFile, lines);
        lessLinesFilePath = lessLinesFile.getCanonicalPath();
        exactLinesFilePath = exactLinesFile.getCanonicalPath();
        moreLinesFilePath = moreLinesFile.getCanonicalPath();

        LOGGER.info("Maximum lines to tail: " + maxLineCount);
        LOGGER.info("File with less lines: " + lessLinesFilePath);
        LOGGER.info("File with maximal lines: " + exactLinesFilePath);
        LOGGER.info("File with more lines: " + moreLinesFilePath);
    }

    /**
     * Regression for KENMEI-5528: endless-loop while showing Communote logs that contain a line
     * which exceeds a certain length
     *
     *
     * @throws IOException
     *             in case the test failed
     */
    @Test(timeOut = 100000)
    public void testForKENMEI5528() throws IOException {
        List<String> lines = new ArrayList<String>();
        for (int i = 1; i < 10; i++) {
            lines.add("Line " + i);
        }
        int chunkSize = 2000;
        String longLine = "";
        while (longLine.length() < chunkSize + 100) {
            longLine += UUID.randomUUID();
        }
        lines.add(longLine);
        for (int i = 1; i < 10; i++) {
            lines.add("Line " + i);
        }
        File file = File.createTempFile("longLines", ".txt");
        FileUtils.writeLines(file, lines);
        String[] readLines = FileHelper.tail(file.getAbsolutePath(), lines.size());
        Assert.assertEquals(readLines.length, lines.size());
        for (int i = 0; i < lines.size(); i++) {
            Assert.assertEquals(readLines[i].replaceAll("[\n\r]", ""), lines.get(i));
        }
    }

    /**
     * Test for {@link FileHelper#tail(String, int)}
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testTail() throws Exception {
        String[] lines = FileHelper.tail(lessLinesFilePath, maxLineCount);
        Assert.assertEquals(lines.length, maxLineCount - 1);
        lines = FileHelper.tail(exactLinesFilePath, maxLineCount);
        Assert.assertEquals(lines.length, maxLineCount);
        lines = FileHelper.tail(moreLinesFilePath, maxLineCount);
        Assert.assertEquals(lines.length, maxLineCount);
    }
}
