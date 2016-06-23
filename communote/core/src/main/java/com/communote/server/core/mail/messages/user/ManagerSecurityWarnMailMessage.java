package com.communote.server.core.mail.messages.user;

import java.util.Map;

import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.client.ClientUrlHelper;


/**
 * general mail-message for security manager warnings
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ManagerSecurityWarnMailMessage extends MailMessage {

    /**
     * message key for low risk
     */
    public final static String RISK_LEVEL_LOW = "mail.manager.risk_level.low";

    /**
     * message key for medium risk
     */
    public final static String RISK_LEVEL_MEDIUM = "mail.manager.risk_level.medium";

    /**
     * message key for high risk
     */
    public final static String RISK_LEVEL_HIGH = "mail.manager.risk_level.high";

    /**
     * message key for warn reason: possible hack attempt
     */
    public final static String WARN_REASON_POSSIBLE_HACK_ATTEMPT =
            "mail.manager.warn_reason.user_account.possible_hack_attempt";

    /**
     * message key for warn reason: user locked
     */
    public final static String WARN_REASON_USER_ACCOUNT_PERM_LOCKED =
            "mail.manager.warn_reason.user_account.perm_locked";

    private final User manager;
    private final String riskLevel;
    private final String warnReason;
    private final Long userId;

    /**
     * constructor
     * 
     * @param manager
     *            receiver with manager role
     * @param riskLevel
     *            message key for risk level
     * @param warnReason
     *            text which contains the warning reason
     * @param userId
     *            The users id.
     */
    public ManagerSecurityWarnMailMessage(User manager, String riskLevel, String warnReason,
            Long userId) {
        super("mail.message.user.manager-security-warn-mail", manager.getLanguageLocale(), manager);
        this.manager = manager;
        this.riskLevel = riskLevel;
        this.warnReason = warnReason;
        this.userId = userId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareModel(Map<String, Object> model) {
        model.put(MailModelPlaceholderConstants.RECEIVER, manager);
        model.put(MailModelPlaceholderConstants.RISK_LEVEL, riskLevel);
        model.put(MailModelPlaceholderConstants.WARN_REASON, warnReason);
        model.put(MailModelPlaceholderConstants.Client.USER_PROFILE_LINK,
                ClientUrlHelper.renderConfiguredAbsoluteUrl(
                        MailMessage.ACTIVATION_LINK_PREFIX, true)
                        + "?userId=" + userId);
    }
}
