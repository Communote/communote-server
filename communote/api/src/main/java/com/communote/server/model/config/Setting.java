package com.communote.server.model.config;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Setting implements java.io.Serializable {
    /**
     * Constructs new instances of {@link Setting}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link Setting}.
         */
        public static Setting newInstance() {
            return new Setting();
        }

    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -3148222450738298428L;

    private String settingKey;

    private String settingValue;

    private Long lastModificationTimestamp;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("settingKey='");
        sb.append(settingKey);
        sb.append("', ");

        sb.append("settingValue='");
        sb.append(settingValue);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an Setting instance and all identifiers for this
     * entity equal the identifiers of the argument entity. Returns <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Setting)) {
            return false;
        }
        final Setting that = (Setting) object;
        if (this.settingKey == null || that.getSettingKey() == null
                || !this.settingKey.equals(that.getSettingKey())) {
            return false;
        }
        return true;
    }

    /**
     * @return the time the setting was modified as the number of milliseconds since epoch. This
     *         will be null for properties which were stored before this feature was introduced.
     */
    public Long getLastModificationTimestamp() {
        return lastModificationTimestamp;
    }

    /**
     *
     */
    public String getSettingKey() {
        return this.settingKey;
    }

    /**
     *
     */
    public String getSettingValue() {
        return this.settingValue;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (settingKey == null ? 0 : settingKey.hashCode());

        return hashCode;
    }

    /**
     * Set the time of the last modification of the setting.
     *
     * @param lastModificationTimestamp
     *            the time of the last modification as the number of milliseconds since epoch
     */
    public void setLastModificationTimestamp(Long lastModificationTimestamp) {
        this.lastModificationTimestamp = lastModificationTimestamp;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

}