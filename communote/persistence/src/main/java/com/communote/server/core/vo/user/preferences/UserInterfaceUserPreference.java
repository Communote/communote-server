package com.communote.server.core.vo.user.preferences;

import java.util.Map;

import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.config.type.ClientProperty.PRESELECTED_TABS_VALUES;
import com.communote.server.core.vo.query.TimelineFilterViewType;

/**
 * UserPreferences for the user interface.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserInterfaceUserPreference extends UserPreference {

    private static final String KEY_PRESELECTED_TAB = "preselectedTab";
    private static final String KEY_VIEW_TYPE = "viewType";

    @Override
    protected Map<String, String> getDefaults() {
        Map<String, String> defaults = super.getDefaults();
        defaults.put(KEY_VIEW_TYPE,
                ClientProperty.PRESELECTED_VIEW.getValue(TimelineFilterViewType.COMMENT.name()));
        defaults.put(KEY_PRESELECTED_TAB,
                ClientProperty.PRESELECTED_TAB.getValue(ClientProperty.PRESELECTED_TABS_VALUES.ALL
                        .name()));
        return defaults;
    }

    /**
     * @return The value for the selected notes overview tab.
     */
    public PRESELECTED_TABS_VALUES getPreselectedTab() {
        return PRESELECTED_TABS_VALUES.valueOf(getValue(KEY_PRESELECTED_TAB,
                ClientProperty.PRESELECTED_TABS_VALUES.ALL.name()));
    }

    /**
     * @return The value for the selected view type.
     */
    public TimelineFilterViewType getViewType() {
        return TimelineFilterViewType.valueOf(getValue(KEY_VIEW_TYPE,
                TimelineFilterViewType.COMMENT.name()));
    }

    @Override
    public void setPreferences(Map<String, String> preferences) {
        if (preferences == null) {
            return;
        }
        if (preferences.get(KEY_PRESELECTED_TAB) != null) {
            getPreferences().put(KEY_PRESELECTED_TAB, preferences.get(KEY_PRESELECTED_TAB));
        }
        if (preferences.get(KEY_VIEW_TYPE) != null) {
            getPreferences().put(KEY_VIEW_TYPE, preferences.get(KEY_VIEW_TYPE));
        }
    }

    /**
     * @param tab
     *            Set the preselected notes overview tab.
     */
    public void setPreselectedTab(PRESELECTED_TABS_VALUES tab) {
        setValue(KEY_PRESELECTED_TAB, tab.name());
    }

    /**
     * 
     * @param type
     *            Sets the type.
     */
    public void setViewType(TimelineFilterViewType type) {
        setValue(KEY_VIEW_TYPE, type.name());
    }
}
