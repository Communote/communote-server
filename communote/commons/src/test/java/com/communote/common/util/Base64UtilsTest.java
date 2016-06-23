package com.communote.common.util;

import java.util.Random;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.util.Base64Utils;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class Base64UtilsTest {

    /**
     * @return a random {@link String}.
     */
    private String createRandomString() {
        StringBuffer buffer = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < random.nextInt(100) + 5; i++) {
            buffer.append(UUID.randomUUID().toString());
            if (random.nextBoolean()) {
                buffer.append(System.getProperty("line.separator"));
            }
        }
        return buffer.toString();
    }

    /**
     * Test.
     */
    @Test
    public void test() {
        String value = createRandomString();
        String encoded = Base64Utils.encode(value.getBytes());
        String decoded = new String(Base64Utils.decode(encoded));
        Assert.assertEquals(decoded, value);
    }

    /**
     * Test 2.
     */
    @Test
    public void test2() {
        String value = createRandomString();
        byte[] encoded = Base64Utils.encodeToBytes(value.getBytes());
        byte[] decoded = Base64Utils.decode(encoded);
        String decodedValue = new String(decoded);
        Assert.assertEquals(decodedValue, value);
        Assert.assertEquals(decoded, value.getBytes());
    }
}
