package com.communote.server.service;

import java.util.Map;
import java.util.UUID;

import com.communote.server.core.vo.user.preferences.UserPreference;

/**
 * Test preference class.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TestUserPreference extends UserPreference {

    /** Key for some value. */
    public static final String KEY_SOME_VALUE = "someValue";
    /** Default value for some value. */
    public static final String SOME_DEFAULT_VALUE = UUID.randomUUID().toString();

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> getDefaults() {
        Map<String, String> defaults = super.getDefaults();
        defaults.put(KEY_SOME_VALUE, SOME_DEFAULT_VALUE);
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
    public void setSomeValue(String value) {
        setValue(KEY_SOME_VALUE, value);
    }
}