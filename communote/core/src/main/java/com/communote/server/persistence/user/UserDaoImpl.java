package com.communote.server.persistence.user;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.common.exceptions.IllegalDatabaseState;
import com.communote.server.core.mail.MailMessageHelper;
import com.communote.server.core.messaging.NotificationManagementConstants;
import com.communote.server.core.user.UserAuthorityHelper;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.model.blog.BlogConstants;
import com.communote.server.model.messaging.MessagerConnectorConfig;
import com.communote.server.model.messaging.MessagerConnectorType;
import com.communote.server.model.note.NoteConstants;
import com.communote.server.model.tag.TagConstants;
import com.communote.server.model.user.CommunoteEntityConstants;
import com.communote.server.model.user.ExternalUserAuthenticationConstants;
import com.communote.server.model.user.NotificationConfig;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserAuthority;
import com.communote.server.model.user.UserAuthorityConstants;
import com.communote.server.model.user.UserConstants;
import com.communote.server.model.user.UserProfile;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;

/**
 * @see com.communote.server.model.user.User
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserDaoImpl extends UserDaoBase {

    private final static String FIND_BY_EMAIL_QUERY = "select u from " + UserConstants.CLASS_NAME
            + " u where lower(u." + UserConstants.EMAIL + ")=?";

    private final static String FIND_LATEST_BY_EXTERNAL_SYSTEM_ID_QUERY = "select u from "
            + UserConstants.CLASS_NAME + " u left join u." + UserConstants.EXTERNALAUTHENTICATIONS
            + " ua where ua." + ExternalUserAuthenticationConstants.SYSTEMID + " = ? and u."
            + CommunoteEntityConstants.ID + " > ? order by u." + CommunoteEntityConstants.ID
            + " asc";

    private final static String FIND_BY_SYSTEM_ID_QUERY = "select u from "
            + UserConstants.CLASS_NAME + " u left join u." + UserConstants.EXTERNALAUTHENTICATIONS
            + " ua where ua." + ExternalUserAuthenticationConstants.SYSTEMID + " = ?";

    private final static String IS_FOLLOWED_QUERY = "select u.id from " + UserConstants.CLASS_NAME
            + " u inner join u." + UserConstants.FOLLOWEDITEMS
            + " followed where u.id = ? and followed.id = ?";

    /** AND **/
    public static final String AND = " and ";

    private final static String FOLLOWABLE_QUERY_PREFIX = "from " + UserConstants.CLASS_NAME
            + " u inner join u." + UserConstants.FOLLOWEDITEMS
            + " followed where u.id = ? and followed.id = followable.";
    private final static String FOLLOWED_USERS_QUERY = "select followable.id from "
            + UserConstants.CLASS_NAME + " followable where ";
    private final static String FOLLOWED_BLOGS_QUERY = "select followable.id from "
            + BlogConstants.CLASS_NAME + " followable where ";
    private final static String FOLLOWED_DISCUSSIONS_QUERY = "select distinct "
            + NoteConstants.DISCUSSIONID + " from " + NoteConstants.CLASS_NAME
            + " followable where ";

    private String appendFollowableRange(String query, long idRangeStart, long idRangeEnd) {
        if (idRangeStart > -1) {
            query += AND + "followable.id>=" + idRangeStart;
        }
        if (idRangeEnd > -1) {
            query += AND + "followable.id<=" + idRangeEnd;
        }
        return query;
    }

    /**
     * Appends the range restrictions to the query for followed items
     *
     * @param query
     *            the query
     * @param idRangeStart
     *            the start of the range
     * @param idRangeEnd
     *            the end of the range
     */
    private void appendFollowableRange(StringBuilder query, long idRangeStart, long idRangeEnd) {
        if (idRangeStart > -1) {
            query.append(AND);
            query.append("followable.id>=");
            query.append(idRangeStart);
        }
        if (idRangeEnd > -1) {
            query.append(AND);
            query.append("followable.id<=");
            query.append(idRangeEnd);
        }
    }

    /**
     * @param source
     *            the source vo
     * @param target
     *            the entity
     */
    private void copyAuthorities(UserVO source, User target) {
        if (source.getRoles() != null) {
            if (target.getUserAuthorities() != null && target.getUserAuthorities().size() > 0) {
                getUserAuthorityDao().remove(target.getUserAuthorities());
            }
            Set<UserAuthority> authorities = UserAuthorityHelper
                    .getUserAuthorities(source.getRoles());
            getUserAuthorityDao().create(authorities);
            target.setUserAuthorities(authorities);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final int transform, final User kenmeiUser) {
        if (kenmeiUser == null) {
            throw new IllegalArgumentException("User.create - 'kenmeiUser' can not be null");
        }

        this.getHibernateTemplate().save(kenmeiUser);
        kenmeiUser.setGlobalId(getGlobalIdDao().createGlobalId(kenmeiUser));

        return this.transformEntity(transform, kenmeiUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getActiveUserCount() {
        // Count active users, but ignore system users.
        long result = DataAccessUtils.longResult(getHibernateTemplate().find(
                "select count(*) from " + UserConstants.CLASS_NAME + " u1 where u1."
                        + UserConstants.STATUS + " = ? AND u1.id not in (SELECT u2.id FROM "
                        + UserConstants.CLASS_NAME + " u2 JOIN u2." + UserConstants.USERAUTHORITIES
                        + " auth WHERE auth." + UserAuthorityConstants.ROLE + " = ?)",
                UserStatus.ACTIVE.getValue(), UserRole.ROLE_SYSTEM_USER));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getActiveUserCount(String systemId, UserRole role) {
        Criteria criteria = getSession().createCriteria(User.class);
        criteria.setProjection(Projections.rowCount());
        criteria.add(Restrictions.eq(UserConstants.STATUS, UserStatus.ACTIVE.getValue()));
        if (role != null) {
            criteria.createAlias(UserConstants.USERAUTHORITIES, "role");
            criteria.add(Restrictions.eq("role." + UserAuthorityConstants.ROLE, role.getValue()));
        }
        if (StringUtils.isNotBlank(systemId)) {
            criteria.createAlias(UserConstants.EXTERNALAUTHENTICATIONS, "externalAuth");
            criteria.add(Restrictions
                    .eq("externalAuth." + ExternalUserAuthenticationConstants.SYSTEMID, systemId));
        }
        Object result = criteria.list().get(0);
        return (Long) result;
    }

    /**
     * set the notification config where is none
     *
     * @return get a default notification config
     */
    private NotificationConfig getDefaultNotificationConfig() {
        NotificationConfig notificationConfig = NotificationConfig.Factory.newInstance();
        notificationConfig.setFallback("mail");
        getNotificationConfigDao().create(notificationConfig);
        createMailNotificationConfig(notificationConfig.getId());
        return notificationConfig;
    }

    /**
     * @return get a default profile
     */
    private UserProfile getDefaultProfile() {
        UserProfile profile = UserProfile.Factory.newInstance();
        profile.setLastModificationDate(new Timestamp(new Date().getTime()));
        if (profile.getNotificationConfig() == null) {
            profile.setNotificationConfig(getDefaultNotificationConfig());
        }
        return profile;
    }

    /**
     * @param notificationConfigId
     *            The notification to add mail notification to.
     */
    @Override
    protected void handleCreateMailNotificationConfig(Long notificationConfigId) {
        MessagerConnectorConfig mailConfig = MessagerConnectorConfig.Factory.newInstance();
        mailConfig.setType(MessagerConnectorType.MAIL);
        mailConfig.setPriority(NotificationManagementConstants.MAIL_PRIORITY);
        mailConfig.setProperties(null);
        ServiceLocator.findService(UserProfileManagement.class)
                .addMessagingConnectorConfig(notificationConfigId, mailConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected User handleFindByAlias(String alias) {

        List<?> results = getHibernateTemplate().find("from " + UserConstants.CLASS_NAME
                + " where lower(" + UserConstants.ALIAS + ") = ?",
                alias.toLowerCase().replace("\0", ""));
        if (results.size() > 1) {
            throw new IllegalDatabaseState("Cannot have more than one user with the same alias, "
                    + "Alias is: '" + alias + "'");
        }
        User user = null;
        if (results.size() > 0) {
            user = (User) results.iterator().next();
            // TODO this is bad. When always initializing the tags they should be fetched eagerly.
            // But tags are usually not needed!
            Hibernate.initialize(user.getTags());
        }
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected User handleFindByEmail(String email) {
        List<User> results = getHibernateTemplate().find(FIND_BY_EMAIL_QUERY,
                email.toLowerCase().replace("\0", ""));
        if (results.size() > 1) {
            throw new IllegalDatabaseState("Cannot have more than one user with the same email, "
                    + "Emails is: '" + email + "'");
        }
        User user = null;
        if (results.size() > 0) {
            user = results.iterator().next();
        }
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<User> handleFindByExternalSystemId(String systemId) {
        return getHibernateTemplate().find(FIND_BY_SYSTEM_ID_QUERY, systemId);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.persistence.user.UserDaoBase#handleFindByExternalUserId(String)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected User handleFindByExternalUserId(String userId, String systemId) {
        boolean compareIdLowerCase = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.COMPARE_EXTERNAL_USER_IDS_LOWERCASE, false);

        String query = "select u from " + UserConstants.CLASS_NAME + " u left join u."
                + UserConstants.EXTERNALAUTHENTICATIONS + " ua where ";
        if (compareIdLowerCase) {
            query += "lower(ua." + ExternalUserAuthenticationConstants.EXTERNALUSERID + ")";
            userId = userId.toLowerCase();
        } else {
            query += "ua." + ExternalUserAuthenticationConstants.EXTERNALUSERID;
        }
        query += " = ? and ua." + ExternalUserAuthenticationConstants.SYSTEMID + " = ?";

        List<User> results = getHibernateTemplate().find(query, userId, systemId);
        if (results.size() > 1) {
            throw new IllegalDatabaseState(
                    "Cannot have more than one user with the same external id, " + "dn is: '"
                            + userId + "'");
        }
        User user = null;
        if (results.size() > 0) {
            user = results.iterator().next();
        }

        return user;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<User> handleFindByRole(UserRole userRole, UserStatus status) {
        Object[] values = new Object[status == null ? 1 : 2];
        values[0] = userRole.toString();
        StringBuilder query = new StringBuilder("select user from " + UserConstants.CLASS_NAME
                + " user left join user." + UserConstants.USERAUTHORITIES + " roles  where roles."
                + UserAuthorityConstants.ROLE + " = ?");
        if (status != null) {
            query.append(" and " + UserConstants.STATUS + " = ?");
            values[1] = status.toString();
        }
        List<User> users = getHibernateTemplate().find(query.toString(), values);
        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<User> handleFindLatestBySystemId(String externalSystemId, Long userId,
            int maxCount) {
        Query query = getSession().createQuery(FIND_LATEST_BY_EXTERNAL_SYSTEM_ID_QUERY);
        query.setString(0, externalSystemId);
        query.setLong(1, userId);
        query.setMaxResults(maxCount);
        return query.list();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.persistence.user.UserDaoBase#handleFindNotConfirmedUser(java.util.Date,
     *      boolean)
     */
    @Override
    protected List<User> handleFindNotConfirmedUser(final Date before,
            final boolean reminderMailSent) {
        List<?> result = getHibernateTemplate().findByNamedParam(
                "select user" + " from " + UserConstants.CLASS_NAME + " user JOIN user."
                        + UserConstants.USERAUTHORITIES + " auth WHERE user." + UserConstants.STATUS
                        + " IN (:status) AND " + "user." + UserConstants.REMINDERMAILSENT
                        + " = :sent and " + "user." + UserConstants.STATUSCHANGED + " <= :date and "
                        + "auth." + UserAuthorityConstants.ROLE + " NOT IN (:authorities)",
                new String[] { "status", "sent", "date", "authorities" },
                new Object[] { new UserStatus[] { UserStatus.INVITED, UserStatus.REGISTERED },
                        reminderMailSent, before, new UserRole[] { UserRole.ROLE_SYSTEM_USER } });
        return (List<User>) result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<User> handleFindNotDeletedUser(boolean excludeSystemUsers) {
        String query = "from " + UserConstants.CLASS_NAME + " where " + UserConstants.STATUS
                + " not in (:statuses)";
        ArrayList<String> paramNames = new ArrayList<String>();
        paramNames.add("statuses");
        ArrayList<Object> params = new ArrayList<Object>();
        params.add(new String[] { UserStatus.DELETED.toString(),
                UserStatus.PERMANENTLY_DISABLED.toString() });
        if (excludeSystemUsers) {
            query += " AND " + CommunoteEntityConstants.ID + " NOT IN (SELECT user."
                    + CommunoteEntityConstants.ID + " FROM " + UserConstants.CLASS_NAME
                    + " user INNER JOIN user." + UserConstants.USERAUTHORITIES + " auth WHERE auth."
                    + UserAuthorityConstants.ROLE + " = :authorities) ";
            paramNames.add("authorities");
            params.add(UserRole.ROLE_SYSTEM_USER);
        }
        List<User> users = getHibernateTemplate().findByNamedParam(query,
                paramNames.toArray(new String[paramNames.size()]), params.toArray());
        return users;
    }

    @Override
    protected List<User> handleFindNotLoggedInActiveUser(Date before, boolean reminderMailSent,
            boolean includeTermsNotAccepted) {
        UserStatus[] statusFilter;
        if (includeTermsNotAccepted) {
            statusFilter = new UserStatus[] { UserStatus.ACTIVE, UserStatus.TERMS_NOT_ACCEPTED };
        } else {
            statusFilter = new UserStatus[] { UserStatus.ACTIVE };
        }
        // TODO this is strange: why excluding the anonymous suffix? It was added to avoid sending
        // emails to system users but to achieve this we should exclude the role instead. This
        // method should not know that the result is used for sending emails or assume a system user
        // has a specific email address.
        List<?> result = getHibernateTemplate().findByNamedParam(
                "FROM " + UserConstants.CLASS_NAME + " WHERE " + UserConstants.STATUS
                        + " IN (:status) AND " + UserConstants.REMINDERMAILSENT + " = :sent AND "
                        + UserConstants.STATUSCHANGED + " <= :date AND " + UserConstants.LASTLOGIN
                        + " IS Null " + " AND not(" + UserConstants.EMAIL + " like :anonymous)",
                new String[] { "status", "sent", "date", "anonymous" },
                new Object[] { statusFilter, reminderMailSent, before,
                        "%" + MailMessageHelper.ANONYMOUS_EMAIL_ADDRESS_SUFFIX });
        return (List<User>) result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Long> handleGetFollowedBlogs(Long userId, long blogIdRangeStart,
            long blogIdRangeEnd) {
        StringBuilder sb = new StringBuilder(FOLLOWED_BLOGS_QUERY);
        sb.append("exists (");
        sb.append(FOLLOWABLE_QUERY_PREFIX);
        sb.append(BlogConstants.GLOBALID + ")");
        appendFollowableRange(sb, blogIdRangeStart, blogIdRangeEnd);
        return getHibernateTemplate().find(sb.toString(), userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Long> handleGetFollowedDiscussions(Long userId, long discussionIdRangeStart,
            long discussionIdRangeEnd) {
        StringBuilder sb = new StringBuilder(FOLLOWED_DISCUSSIONS_QUERY);
        sb.append("exists (");
        sb.append(FOLLOWABLE_QUERY_PREFIX);
        sb.append(NoteConstants.GLOBALID + ")");
        appendFollowableRange(sb, discussionIdRangeStart, discussionIdRangeEnd);
        return getHibernateTemplate().find(sb.toString(), userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Long> handleGetFollowedTags(Long userId, Long idRangeStart, Long idRangeEnd) {
        String query = "select followable.id from " + TagConstants.CLASS_NAME
                + " followable where exists (" + FOLLOWABLE_QUERY_PREFIX
                + CommunoteEntityConstants.GLOBALID + ")";
        query = appendFollowableRange(query, idRangeStart, idRangeEnd);
        return getHibernateTemplate().find(query, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Long> handleGetFollowedUsers(Long userId, long userIdRangeStart,
            long userIdRangeEnd) {
        StringBuilder sb = new StringBuilder(FOLLOWED_USERS_QUERY);
        sb.append("exists (");
        sb.append(FOLLOWABLE_QUERY_PREFIX);
        sb.append(CommunoteEntityConstants.GLOBALID);
        sb.append(")");
        appendFollowableRange(sb, userIdRangeStart, userIdRangeEnd);
        return getHibernateTemplate().find(sb.toString(), userId);
    }

    @Override
    protected void handleResetTermsAccepted(final Long userIdToIgnore) {
        // Note: not using bulk updates as it will delete the entire UserCache region which is
        // especially bad for SaaS because all clients are in the same region
        getHibernateTemplate().executeWithNativeSession(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session
                        .createQuery("from " + UserConstants.CLASS_NAME + " where "
                                + UserConstants.TERMSACCEPTED + " = ? AND (" + UserConstants.STATUS
                                + "= ? OR " + UserConstants.STATUS + "= ?) AND "
                                + CommunoteEntityConstants.ID + " != ?")
                        .setParameter(0, Boolean.TRUE).setParameter(1, UserStatus.ACTIVE)
                        .setParameter(2, UserStatus.TEMPORARILY_DISABLED)
                        .setParameter(3, userIdToIgnore);
                query.setCacheMode(CacheMode.GET);
                Iterator iterator = query.iterate();
                int count = 0;
                while (iterator.hasNext()) {
                    User user = (User) iterator.next();
                    user.setTermsAccepted(false);
                    // avoid out of memory by flushing session cache
                    if (++count % 40 == 0) {
                        session.flush();
                        session.clear();
                    }
                }
                return null;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleUserFollowsItem(Long userId, Long globalId) {
        List<?> result = getHibernateTemplate().find(IS_FOLLOWED_QUERY, userId, globalId);
        return result.size() != 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.persistence.user.UserDao#userVOToEntity(com.communote.server.api.core.user.UserVO)
     */
    @Override
    public User userVOToEntity(UserVO kenmeiUserVO) {
        User user = User.Factory.newInstance();
        this.userVOToEntity(kenmeiUserVO, user);
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void userVOToEntity(UserVO source, User target) {
        if (source.getEmail() != null) {
            target.setEmail(source.getEmail());
        }
        if (source.getAlias() != null) {
            target.setAlias(source.getAlias());
        }
        if (source.getLanguage() != null) {
            target.setLanguageLocale(source.getLanguage());
        }
        copyAuthorities(source, target);

        if (target.getProfile() == null) {
            target.setProfile(getDefaultProfile());
        }
        UserProfile profile = target.getProfile();

        if (source.getFirstName() != null) {
            profile.setFirstName(source.getFirstName());
        }
        if (source.getLastName() != null) {
            profile.setLastName(source.getLastName());
        }
        // TimeZone cannot be empty
        if (StringUtils.isNotBlank(source.getTimeZoneId())) {
            profile.setTimeZoneId(source.getTimeZoneId());
        }
        profile.setLastModificationDate(new Timestamp(new Date().getTime()));
        if (profile.getId() == null) {
            getUserProfileDao().create(profile);
        } else {
            getUserProfileDao().update(profile);
        }
    }
}
