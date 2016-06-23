package com.communote.server.model.config;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ApplicationConfigurationSetting implements java.io.Serializable {
    /**
     * Constructs new instances of
     * {@link com.communote.server.model.config.ApplicationConfigurationSetting}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.config.ApplicationConfigurationSetting}.
         */
        public static com.communote.server.model.config.ApplicationConfigurationSetting newInstance() {
            return new com.communote.server.model.config.ApplicationConfigurationSettingImpl();
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.config.ApplicationConfigurationSetting}, taking all
         * possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.config.ApplicationConfigurationSetting newInstance(
                String settingValue) {
            final com.communote.server.model.config.ApplicationConfigurationSetting entity = new com.communote.server.model.config.ApplicationConfigurationSettingImpl();
            entity.setSettingValue(settingValue);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 4500132578034174468L;

    private String settingKey;

    private String settingValue;

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
     * Returns <code>true</code> if the argument is an ApplicationConfigurationSetting instance and
     * all identifiers for this entity equal the identifiers of the argument entity. Returns
     * <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ApplicationConfigurationSetting)) {
            return false;
        }
        final ApplicationConfigurationSetting that = (ApplicationConfigurationSetting) object;
        if (this.settingKey == null || that.getSettingKey() == null
                || !this.settingKey.equals(that.getSettingKey())) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * the key of the setting
     * </p>
     */
    public String getSettingKey() {
        return this.settingKey;
    }

    /**
     * <p>
     * the value of the setting
     * </p>
     */
    public String getSettingValue() {
        return this.settingValue;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (settingKey == null ? 0 : settingKey.hashCode());

        return hashCode;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }
}