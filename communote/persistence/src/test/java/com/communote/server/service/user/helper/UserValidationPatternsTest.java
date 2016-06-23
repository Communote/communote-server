package com.communote.server.service.user.helper;

import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.core.user.helper.ValidationPatterns;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class UserValidationPatternsTest {

    /**
     * Tests for {@link ValidationPatterns#PATTERN_ALIAS}
     */
    @Test
    public void testAliasPattern() {
        // Format: { alias, isValid }
        String[][] aliases = new String[][] {
                { "kenmei", "true" }, { "ken-mei", "true" }, { "ken_mei", "true" },
                { "kenm\u00E4", "false" }, { "kenmei.", "false" }, { "ken.mei", "true" },
                { "kenmei1", "true" } };
        Pattern pattern = Pattern.compile(ValidationPatterns.PATTERN_ALIAS);
        for (String[] alias : aliases) {
            Assert.assertEquals(pattern.matcher(alias[0]).matches(), Boolean.valueOf(alias[1])
                    .booleanValue(), "Error with alias: " + alias[0]);
        }
    }

    /**
     * Tests for {@link ValidationPatterns#PATTERN_FIRSTNAME} and
     * {@link ValidationPatterns#PATTERN_LASTNAME}
     */
    @Test
    public void testNamePattern() {
        // Format: { name, isValid }
        String[][] aliases = new String[][] {
                { "kenmei", "true" }, { "ken-mei", "true" }, { "ken_mei", "true" },
                { "kenm\u00c4", "true" }, { "kenmei.", "true" }, { "ken.mei", "true" },
                { "kenmei1", "true" }, { "kenmei1\u0012", "false" }, { "kenmei1\u001B", "false" },
                { "\u00ED\u00E8\u0130\u016B\u00E3\'\u00B4`._-", "true" } };
        Pattern pattern = Pattern.compile(ValidationPatterns.PATTERN_FIRSTNAME);
        for (String[] alias : aliases) {
            Assert.assertEquals(pattern.matcher(alias[0]).matches(), Boolean.valueOf(alias[1])
                    .booleanValue(), "Error with first name: " + alias[0]);
        }
        pattern = Pattern.compile(ValidationPatterns.PATTERN_LASTNAME);
        for (String[] alias : aliases) {
            Assert.assertEquals(pattern.matcher(alias[0]).matches(), Boolean.valueOf(alias[1])
                    .booleanValue(), "Error with last name: " + alias[0]);
        }
    }
}
