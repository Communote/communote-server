package com.communote.common.io;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link MimeTypeHelper}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class MimeTypeHelperTest {

    private static final String TEST_FILES_ROOT = "src/test/resources/mime_type_files";

    /**
     * Tests MimeTypeHelper#getMimeType.
     */
    @Test
    public void testGetMimeType() {
        int counter = 0;
        for (String filePath : new File(TEST_FILES_ROOT).list()) {
            File file = new File(TEST_FILES_ROOT + "/" + filePath);
            if (file.isFile() && !file.getName().equals("License.txt")) {
                counter++;
                String mimeType = MimeTypeHelper.getMimeType(file);
                boolean contains = file.getName().contains(
                        mimeType.replace("/", "_").replace("*", "_"));
                System.out.println("MimeTypeHelperTest: " + file.getName() + " => " + mimeType
                        + " " + contains + " ");
                Assert.assertTrue(contains, file.getName() + " => " + mimeType + "(Already "
                        + (counter - 1) + " successfully tested)");
            }
        }
        System.out.println("Tested " + counter + " files for MimeType detection.");
    }
}
