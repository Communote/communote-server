package com.communote.plugins.api.rest.test;

import java.util.Map;
import java.util.UUID;

import com.communote.server.core.vo.user.preferences.UserPreference;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
/**
 * Test preference class.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TestUserPreference extends UserPreference {

    /** Key for some value. */
    public static final String KEY_SOME_VALUE = "someValue";
    private static final String KEY_SOME_OTHER_VALUE = "someOtherValue";
    /** Default value for some value. */
    public static final String SOME_DEFAULT_VALUE = UUID.randomUUID().toString();

    /**
     * @return the value for some value.
     */
    public String getAnotherValue() {
        return getValue(KEY_SOME_OTHER_VALUE, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> getDefaults() {
        Map<String, String> defaults = super.getDefaults();
        defaults.put(KEY_SOME_VALUE, SOME_DEFAULT_VALUE);
        defaults.put(KEY_SOME_OTHER_VALUE, KEY_SOME_OTHER_VALUE);
        return defaults;
    }

    /**
     * @return the value for some value.
     */
    public String getSomeValue() {
        return getValue(KEY_SOME_VALUE, "");
    }

    /**
     * @param value
     *            The new value of some.
     */
    public void setAnotherValue(String value) {
        // Ignore, we don't allow overwriting this value.
    }

    /**
     * @param value
     *            The new value of some.
     */
    public void setSomeValue(String value) {
        setValue(KEY_SOME_VALUE, value);
    }
}