package com.communote.server.web.fe.widgets.user.profile;

import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.filter.listitems.UserProfileDetailListItem;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.model.user.PhoneNumber;
import com.communote.server.widgets.AbstractWidget;

/**
 * Widget for displaying the manage user profile form
 *
 * @author
 */
public class AboutUserProfileWidget extends AbstractWidget {

    private static final String PARAM_USER_ID = "userId";

    private Long userId;
    private UserProfileDetailListItem user = null;
    private String phone = null;
    private String fax = null;

    /** */
    private boolean currentUserFollowsUser = false;

    /**
     * @return the locale of the current user
     */
    public Locale getCurrentUserLocale() {
        return SessionHandler.instance().getCurrentLocale(getRequest());
    }

    /**
     * @return the fax
     */
    public String getFax() {
        if (fax == null) {
            getFaxNumberAsString();
        }

        return fax;
    }

    /**
     * Creates a readable fax number.
     */
    private void getFaxNumberAsString() {
        String number = StringUtils.EMPTY;

        if (user != null && user.getFax() != null) {
            number = renderNumber(user.getFax());
        }

        this.fax = number;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        if (phone == null) {
            getPhoneNumberAsString();
        }

        return phone;
    }

    /**
     * Creates a readable phone number.
     */
    private void getPhoneNumberAsString() {
        String number = StringUtils.EMPTY;

        if (user != null && user.getPhone() != null) {
            number = renderNumber(user.getPhone());
        }

        this.phone = number;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "core.widget.user.profile.about";
    }

    /**
     *
     * @return the time zone of the current user
     */
    public TimeZone getTimeZone() {
        userId = getLongParameter(PARAM_USER_ID, 0);
        return UserManagementHelper.getEffectiveUserTimeZone(userId);
    }

    /**
     *
     * @return the time zone ID
     */
    public String getTimeZoneId() {
        userId = getLongParameter(PARAM_USER_ID, 0);
        TimeZone timeZone = UserManagementHelper.getEffectiveUserTimeZone(userId);
        return timeZone.getID();
    }

    /**
     *
     * @return the user id to filter
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handleRequest() {
        userId = getLongParameter(PARAM_USER_ID, 0);

        if (userId != 0) {
            user = ServiceLocator.findService(UserProfileManagement.class)
                    .findUserProfileDetailListItemByUserId(userId);
            if (user != null) {
                currentUserFollowsUser = ServiceLocator.instance()
                        .getService(FollowManagement.class).followsUser(userId);
            }
        }
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initParameters() {
    }

    /**
     *
     * @return true if there are contact data
     */
    public boolean isContactData() {
        if (user != null) {
            if (StringUtils.isBlank(user.getCity()) && StringUtils.isBlank(user.getCompany())
                    && StringUtils.isBlank(user.getStreet()) && StringUtils.isBlank(user.getZip())
                    && isPhoneNumberEmpty(user.getPhone()) && isPhoneNumberEmpty(user.getFax())) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @return true if the filtered user is the current user
     */
    public boolean isCurrentUser() {
        return SecurityHelper.getCurrentUserId().equals(getUserId());
    }

    /**
     *
     * @return {@code true} if the current user follows this user
     */
    public boolean isCurrentUserFollowsUser() {
        return currentUserFollowsUser;
    }

    /**
     *
     * @return true if the phone number is not blank
     * @param number
     *            : the number object which have to check
     */
    private boolean isPhoneNumberEmpty(PhoneNumber number) {
        return number == null || StringUtils.isBlank(number.getPhoneNumber());
    }

    /**
     * Creates a readable phone number.
     *
     * @param number
     *            The phone number
     * @return the fax number as string or an empty string
     */
    private String renderNumber(PhoneNumber number) {
        StringBuilder renderedNumber = new StringBuilder();

        if (number != null) {
            StringBuilder countryCode = new StringBuilder();
            StringBuilder areaCode = new StringBuilder();

            if (StringUtils.isNotEmpty(number.getCountryCode())) {
                countryCode.append("+");
                countryCode.append(number.getCountryCode());
                countryCode.append(" ");
            }
            if (StringUtils.isNotEmpty(number.getAreaCode())) {
                areaCode.append(countryCode);
                areaCode.append("(");
                areaCode.append(number.getAreaCode());
                areaCode.append(") ");
            }
            if (StringUtils.isNotEmpty(number.getPhoneNumber())) {
                renderedNumber.append(areaCode);
                renderedNumber.append(number.getPhoneNumber());
            }
        }

        return renderedNumber.toString();
    }

}
