package com.communote.server.persistence.user;

import com.communote.server.model.user.User;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>com.communote.server.persistence.user.KenmeiUser</code>.
 * </p>
 *
 * @see com.communote.server.model.user.User
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserDaoBase extends
        org.springframework.orm.hibernate3.support.HibernateDaoSupport implements
        com.communote.server.persistence.user.UserDao {

    private com.communote.server.persistence.global.GlobalIdDao globalIdDao;

    private com.communote.server.persistence.user.LanguageDao languageDao;

    private com.communote.server.persistence.user.UserAuthorityDao userAuthorityDao;

    private com.communote.server.persistence.user.NotificationConfigDao notificationConfigDao;

    private com.communote.server.persistence.user.UserProfileDao userProfileDao;

    /**
     * This anonymous transformer is designed to transform entities or report query results (which
     * result in an array of objects) to {@link com.communote.server.api.core.user.UserVO} using the
     * Jakarta Commons-Collections Transformation API.
     */
    private final org.apache.commons.collections.Transformer KENMEIUSERVO_TRANSFORMER = new org.apache.commons.collections.Transformer() {
        @Override
        public Object transform(Object input) {
            Object result = null;
            if (input instanceof com.communote.server.model.user.User) {
                result = toUserVO((com.communote.server.model.user.User) input);
            } else if (input instanceof Object[]) {
                result = toKenmeiUserVO((Object[]) input);
            }
            return result;
        }
    };

    private final org.apache.commons.collections.Transformer KenmeiUserVOToEntityTransformer = new org.apache.commons.collections.Transformer() {
        @Override
        public Object transform(Object input) {
            return userVOToEntity((com.communote.server.api.core.user.UserVO) input);
        }
    };

    /**
     * @see com.communote.server.persistence.user.UserDao#create(com.communote.server.model.user.User)
     */
    @Override
    public com.communote.server.model.user.User create(com.communote.server.model.user.User user) {
        return (com.communote.server.model.user.User) this.create(TRANSFORM_NONE, user);
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#create(int transform,
     *      com.communote.server.persistence.user.KenmeiUser)
     */
    @Override
    public Object create(final int transform, final com.communote.server.model.user.User user) {
        if (user == null) {
            throw new IllegalArgumentException("User.create - 'user' can not be null");
        }
        this.getHibernateTemplate().save(user);
        return this.transformEntity(transform, user);
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#create(int,
     *      java.util.Collection<com.communote.server.persistence.user.KenmeiUser>)
     */
    @Override
    public java.util.Collection<com.communote.server.model.user.User> create(final int transform,
            final java.util.Collection<com.communote.server.model.user.User> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("User.create - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.User>() {
                            @Override
                            public com.communote.server.model.user.User doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.User> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    create(transform, entityIterator.next());
                                }
                                return null;
                            }
                        });
        return entities;
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#create(java.util.Collection<de. User>)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.User> create(
            final java.util.Collection<com.communote.server.model.user.User> entities) {
        return create(TRANSFORM_NONE, entities);
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#createMailNotificationConfig(Long)
     */
    @Override
    public void createMailNotificationConfig(final Long notificationConfigId) {
        if (notificationConfigId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.createMailNotificationConfig(Long notificationConfigId) - 'notificationConfigId' can not be null");
        }
        try {
            this.handleCreateMailNotificationConfig(notificationConfigId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.createMailNotificationConfig(Long notificationConfigId)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evict(com.communote.server.model.user.User entity) {
        this.getHibernateTemplate().evict(entity);
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#findByAlias(String)
     */
    @Override
    public com.communote.server.model.user.User findByAlias(final String alias) {
        if (alias == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.findByAlias(String alias) - 'alias' can not be null");
        }
        try {
            return this.handleFindByAlias(alias);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.findByAlias(String alias)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#findByEmail(String)
     */
    @Override
    public com.communote.server.model.user.User findByEmail(final String email) {
        if (email == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.findByEmail(String email) - 'email' can not be null");
        }
        try {
            return this.handleFindByEmail(email);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.findByEmail(String email)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#findByExternalSystemId(String)
     */
    @Override
    public java.util.List<User> findByExternalSystemId(final String systemId) {
        if (systemId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.findByExternalSystemId(String systemId) - 'systemId' can not be null");
        }
        try {
            return this.handleFindByExternalSystemId(systemId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.findByExternalSystemId(String systemId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#findByExternalUserId(String, String)
     */
    @Override
    public com.communote.server.model.user.User findByExternalUserId(final String userId,
            final String systemId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.findByExternalUserId(String userId, String systemId) - 'userId' can not be null");
        }
        if (systemId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.findByExternalUserId(String userId, String systemId) - 'systemId' can not be null");
        }
        try {
            return this.handleFindByExternalUserId(userId, systemId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.findByExternalUserId(String userId, String systemId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#findByRole(com.communote.server.model.user.UserRole,
     *      com.communote.server.model.user.UserStatus)
     */
    @Override
    public java.util.List<com.communote.server.model.user.User> findByRole(
            final com.communote.server.model.user.UserRole userRole,
            final com.communote.server.model.user.UserStatus status) {
        if (userRole == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.findByRole(UserRole userRole, UserStatus status) - 'userRole' can not be null");
        }
        try {
            return this.handleFindByRole(userRole, status);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.findByRole(UserRole userRole, UserStatus status)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#findLatestBySystemId(String, Long, int)
     */
    @Override
    public java.util.List<User> findLatestBySystemId(final String externalSystemId,
            final Long userId, final int maxCount) {
        if (externalSystemId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.findLatestBySystemId(String externalSystemId, Long userId, int maxCount) - 'externalSystemId' can not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.findLatestBySystemId(String externalSystemId, Long userId, int maxCount) - 'userId' can not be null");
        }
        try {
            return this.handleFindLatestBySystemId(externalSystemId, userId, maxCount);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.findLatestBySystemId(String externalSystemId, Long userId, int maxCount)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#findNotConfirmedUser(java.util.Date,
     *      boolean)
     */
    @Override
    public java.util.List<com.communote.server.model.user.User> findNotConfirmedUser(
            final java.util.Date before, final boolean reminderMailSent) {
        if (before == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.findNotConfirmedUser(java.util.Date before, boolean reminderMailSent) - 'before' can not be null");
        }
        try {
            return this.handleFindNotConfirmedUser(before, reminderMailSent);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.findNotConfirmedUser(java.util.Date before, boolean reminderMailSent)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#findNotDeletedUser()
     */
    @Override
    public java.util.Collection<com.communote.server.model.user.User> findNotDeletedUser(
            boolean excludeSystemUsers) {
        try {
            return this.handleFindNotDeletedUser(excludeSystemUsers);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.findNotDeletedUser()' --> "
                            + rt, rt);
        }
    }

    @Override
    public java.util.List<com.communote.server.model.user.User> findNotLoggedInActiveUser(
            java.util.Date before, boolean reminderMailSent, boolean includeTermsNotAccepted) {
        if (before == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.findNotLoggedInActiveUser - 'before' cannot be null");
        }
        try {
            return this.handleFindNotLoggedInActiveUser(before, reminderMailSent,
                    includeTermsNotAccepted);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.findNotLoggedInActiveUser' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#getFollowedBlogs(Long, long, long)
     */
    @Override
    public java.util.List<Long> getFollowedBlogs(final Long userId, final long blogIdRangeStart,
            final long blogIdRangeEnd) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.getFollowedBlogs(Long userId, long blogIdRangeStart, long blogIdRangeEnd) - 'userId' can not be null");
        }
        try {
            return this.handleGetFollowedBlogs(userId, blogIdRangeStart, blogIdRangeEnd);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.getFollowedBlogs(Long userId, long blogIdRangeStart, long blogIdRangeEnd)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#getFollowedDiscussions(Long, long, long)
     */
    @Override
    public java.util.List<Long> getFollowedDiscussions(final Long userId,
            final long discussionIdRangeStart, final long discussionIdRangeEnd) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.getFollowedDiscussions(Long userId, long discussionIdRangeStart, long discussionIdRangeEnd) - 'userId' can not be null");
        }
        try {
            return this.handleGetFollowedDiscussions(userId, discussionIdRangeStart,
                    discussionIdRangeEnd);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.getFollowedDiscussions(Long userId, long discussionIdRangeStart, long discussionIdRangeEnd)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#getFollowedTags(Long, Long, Long)
     */
    @Override
    public java.util.List<Long> getFollowedTags(final Long userId, final Long rangeStart,
            final Long rangeEnd) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.getFollowedTags(Long userId, Long rangeStart, Long rangeEnd) - 'userId' can not be null");
        }
        if (rangeStart == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.getFollowedTags(Long userId, Long rangeStart, Long rangeEnd) - 'rangeStart' can not be null");
        }
        if (rangeEnd == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.getFollowedTags(Long userId, Long rangeStart, Long rangeEnd) - 'rangeEnd' can not be null");
        }
        try {
            return this.handleGetFollowedTags(userId, rangeStart, rangeEnd);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.getFollowedTags(Long userId, Long rangeStart, Long rangeEnd)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#getFollowedUsers(Long, long, long)
     */
    @Override
    public java.util.List<Long> getFollowedUsers(final Long userId, final long userIdRangeStart,
            final long userIdRangeEnd) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.getFollowedUsers(Long userId, long userIdRangeStart, long userIdRangeEnd) - 'userId' can not be null");
        }
        try {
            return this.handleGetFollowedUsers(userId, userIdRangeStart, userIdRangeEnd);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.getFollowedUsers(Long userId, long userIdRangeStart, long userIdRangeEnd)' --> "
                            + rt, rt);
        }
    }

    /**
     * Gets the reference to <code>globalIdDao</code>.
     */
    protected com.communote.server.persistence.global.GlobalIdDao getGlobalIdDao() {
        return this.globalIdDao;
    }

    /**
     * Gets the reference to <code>languageDao</code>.
     */
    protected com.communote.server.persistence.user.LanguageDao getLanguageDao() {
        return this.languageDao;
    }

    /**
     * Gets the reference to <code>notificationConfigDao</code>.
     */
    protected com.communote.server.persistence.user.NotificationConfigDao getNotificationConfigDao() {
        return this.notificationConfigDao;
    }

    /**
     * Gets the reference to <code>UserAuthorityDao</code>.
     */
    protected com.communote.server.persistence.user.UserAuthorityDao getUserAuthorityDao() {
        return this.userAuthorityDao;
    }

    /**
     * Gets the reference to <code>userProfileDao</code>.
     */
    protected com.communote.server.persistence.user.UserProfileDao getUserProfileDao() {
        return this.userProfileDao;
    }

    /**
     * Performs the core logic for {@link #createMailNotificationConfig(Long)}
     */
    protected abstract void handleCreateMailNotificationConfig(Long notificationConfigId);

    /**
     * Performs the core logic for {@link #findByAlias(String)}
     */
    protected abstract com.communote.server.model.user.User handleFindByAlias(String alias);

    /**
     * Performs the core logic for {@link #findByEmail(String)}
     */
    protected abstract com.communote.server.model.user.User handleFindByEmail(String email);

    /**
     * Performs the core logic for {@link #findByExternalSystemId(String)}
     */
    protected abstract java.util.List<User> handleFindByExternalSystemId(String systemId);

    /**
     * Performs the core logic for {@link #findByExternalUserId(String, String)}
     */
    protected abstract com.communote.server.model.user.User handleFindByExternalUserId(
            String userId, String systemId);

    /**
     * Performs the core logic for
     * {@link #findByRole(com.communote.server.model.user.UserRole, com.communote.server.model.user.UserStatus)}
     */
    protected abstract java.util.List<com.communote.server.model.user.User> handleFindByRole(
            com.communote.server.model.user.UserRole userRole,
            com.communote.server.model.user.UserStatus status);

    /**
     * Performs the core logic for {@link #findLatestBySystemId(String, Long, int)}
     */
    protected abstract java.util.List<User> handleFindLatestBySystemId(String externalSystemId,
            Long userId, int maxCount);

    /**
     * Performs the core logic for {@link #findNotConfirmedUser(java.util.Date, boolean)}
     */
    protected abstract java.util.List<com.communote.server.model.user.User> handleFindNotConfirmedUser(
            java.util.Date before, boolean reminderMailSent);

    /**
     * Performs the core logic for {@link #findNotDeletedUser(boolean)}
     */
    protected abstract java.util.Collection<com.communote.server.model.user.User> handleFindNotDeletedUser(
            boolean excludeSystemUsers);

    /**
     * Performs the core logic for {@link #findNotLoggedInActiveUser}
     */
    protected abstract java.util.List<com.communote.server.model.user.User> handleFindNotLoggedInActiveUser(
            java.util.Date before, boolean reminderMailSent, boolean includeTermsNotAccepted);

    /**
     * Performs the core logic for {@link #getFollowedBlogs(Long, long, long)}
     */
    protected abstract java.util.List<Long> handleGetFollowedBlogs(Long userId,
            long blogIdRangeStart, long blogIdRangeEnd);

    /**
     * Performs the core logic for {@link #getFollowedDiscussions(Long, long, long)}
     */
    protected abstract java.util.List<Long> handleGetFollowedDiscussions(Long userId,
            long discussionIdRangeStart, long discussionIdRangeEnd);

    /**
     * Performs the core logic for {@link #getFollowedTags(Long, Long, Long)}
     */
    protected abstract java.util.List<Long> handleGetFollowedTags(Long userId, Long rangeStart,
            Long rangeEnd);

    /**
     * Performs the core logic for {@link #getFollowedUsers(Long, long, long)}
     */
    protected abstract java.util.List<Long> handleGetFollowedUsers(Long userId,
            long userIdRangeStart, long userIdRangeEnd);

    /**
     * Performs the core logic for {@link #resetTermsAccepted()}
     */
    protected abstract void handleResetTermsAccepted(Long userIdToIgnore);

    /**
     * Performs the core logic for {@link #userFollowsItem(Long, Long)}
     */
    protected abstract boolean handleUserFollowsItem(Long userId, Long globalId);

    /**
     * @see com.communote.server.persistence.user.UserDao#load(int, Long)
     */
    @Override
    public Object load(final int transform, final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User.load - 'id' can not be null");
        }
        final Object entity = this.getHibernateTemplate().get(
                com.communote.server.model.user.User.class, id);
        return transformEntity(transform, (com.communote.server.model.user.User) entity);
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#load(Long)
     */
    @Override
    public com.communote.server.model.user.User load(Long id) {
        return (com.communote.server.model.user.User) this.load(TRANSFORM_NONE, id);
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#loadAll()
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public java.util.Collection<com.communote.server.model.user.User> loadAll() {
        return (java.util.Collection<com.communote.server.model.user.User>) this
                .loadAll(TRANSFORM_NONE);
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#loadAll(int)
     */
    @Override
    public java.util.Collection<?> loadAll(final int transform) {
        final java.util.Collection<?> results = this.getHibernateTemplate().loadAll(
                com.communote.server.model.user.User.class);
        this.transformEntities(transform, results);
        return results;
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#remove(com.communote.server.model.user.User)
     */
    @Override
    public void remove(com.communote.server.model.user.User user) {
        if (user == null) {
            throw new IllegalArgumentException("User.remove - 'user' can not be null");
        }
        this.getHibernateTemplate().delete(user);
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#remove(java.util.Collection<de. User>)
     */
    @Override
    public void remove(java.util.Collection<com.communote.server.model.user.User> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("User.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#remove(Long)
     */
    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User.remove - 'id' can not be null");
        }
        com.communote.server.model.user.User entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    @Override
    public void resetTermsAccepted(Long userIdToIgnore) {
        try {
            this.handleResetTermsAccepted(userIdToIgnore);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.resetTermsAccepted()' --> "
                            + rt, rt);
        }
    }

    /**
     * Sets the reference to <code>globalIdDao</code>.
     */
    public void setGlobalIdDao(com.communote.server.persistence.global.GlobalIdDao globalIdDao) {
        this.globalIdDao = globalIdDao;
    }

    /**
     * Sets the reference to <code>languageDao</code>.
     */
    public void setLanguageDao(com.communote.server.persistence.user.LanguageDao languageDao) {
        this.languageDao = languageDao;
    }

    /**
     * Sets the reference to <code>notificationConfigDao</code>.
     */
    public void setNotificationConfigDao(
            com.communote.server.persistence.user.NotificationConfigDao notificationConfigDao) {
        this.notificationConfigDao = notificationConfigDao;
    }

    /**
     * Sets the reference to <code>UserAuthorityDao</code>.
     */
    public void setUserAuthorityDao(
            com.communote.server.persistence.user.UserAuthorityDao userAuthorityDao) {
        this.userAuthorityDao = userAuthorityDao;
    }

    /**
     * Sets the reference to <code>userProfileDao</code>.
     */
    public void setUserProfileDao(
            com.communote.server.persistence.user.UserProfileDao userProfileDao) {
        this.userProfileDao = userProfileDao;
    }

    /**
     * Default implementation for transforming the results of a report query into a value object.
     * This implementation exists for convenience reasons only. It needs only be overridden in the
     * {@link UserDaoImpl} class if you intend to use reporting queries.
     *
     * @see com.communote.server.persistence.user.UserDao#toUserVO(com.communote.server.model.user.User)
     */
    protected com.communote.server.api.core.user.UserVO toKenmeiUserVO(Object[] row) {
        com.communote.server.api.core.user.UserVO target = null;
        if (row != null) {
            final int numberOfObjects = row.length;
            for (int ctr = 0; ctr < numberOfObjects; ctr++) {
                final Object object = row[ctr];
                if (object instanceof com.communote.server.model.user.User) {
                    target = this.toUserVO((com.communote.server.model.user.User) object);
                    break;
                }
            }
        }
        return target;
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#toUserVO(com.communote.server.model.user.User)
     */
    @Override
    public com.communote.server.api.core.user.UserVO toUserVO(
            final com.communote.server.model.user.User entity) {
        final com.communote.server.api.core.user.UserVO target = new com.communote.server.api.core.user.UserVO();
        this.toUserVO(entity, target);
        return target;
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#toUserVO(com.communote.server.model.user.User,
     *      com.communote.server.api.core.user.UserVO)
     */
    @Override
    public void toUserVO(com.communote.server.model.user.User source,
            com.communote.server.api.core.user.UserVO target) {
        target.setPassword(source.getPassword());
        target.setEmail(source.getEmail());
        target.setAlias(source.getAlias());
        // No conversion for target.tags (can't convert
        // source.getTags():com.communote.server.persistence.tag.Tag to
        // java.util.Set<com.communote.server.persistence.tag.TagTO>)
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#toUserVOCollection(java.util.Collection)
     */
    @Override
    public final void toUserVOCollection(java.util.Collection entities) {
        if (entities != null) {
            org.apache.commons.collections.CollectionUtils.transform(entities,
                    KENMEIUSERVO_TRANSFORMER);
        }
    }

    /**
     * Transforms a collection of entities using the
     * {@link #transformEntity(int,com.communote.server.model.user.User)} method. This method does
     * not instantiate a new collection.
     * <p/>
     * This method is to be used internally only.
     *
     * @param transform
     *            one of the constants declared in
     *            <code>com.communote.server.persistence.user.UserDao</code>
     * @param entities
     *            the collection of entities to transform
     * @see #transformEntity(int,com.communote.server.model.user.User)
     */
    protected void transformEntities(final int transform, final java.util.Collection<?> entities) {
        switch (transform) {
        case TRANSFORM_KENMEIUSERVO:
            toUserVOCollection(entities);
            break;
        case TRANSFORM_NONE: // fall-through
        default:
            // do nothing;
        }
    }

    /**
     * Allows transformation of entities into value objects (or something else for that matter),
     * when the <code>transform</code> flag is set to one of the constants defined in
     * <code>com.communote.server.persistence.user.UserDao</code>, please note that the
     * {@link #TRANSFORM_NONE} constant denotes no transformation, so the entity itself will be
     * returned.
     * <p/>
     * This method will return instances of these types:
     * <ul>
     * <li>{@link com.communote.server.model.user.User} - {@link #TRANSFORM_NONE}</li>
     * <li>{@link com.communote.server.api.core.user.UserVO} - {@link TRANSFORM_KENMEIUSERVO}</li>
     * </ul>
     *
     * If the integer argument value is unknown {@link #TRANSFORM_NONE} is assumed.
     *
     * @param transform
     *            one of the constants declared in
     *            {@link com.communote.server.persistence.user.UserDao}
     * @param entity
     *            an entity that was found
     * @return the transformed entity (i.e. new value object, etc)
     * @see #transformEntities(int,java.util.Collection)
     */
    protected Object transformEntity(final int transform,
            final com.communote.server.model.user.User entity) {
        Object target = null;
        if (entity != null) {
            switch (transform) {
            case TRANSFORM_KENMEIUSERVO:
                target = toUserVO(entity);
                break;
            case TRANSFORM_NONE: // fall-through
            default:
                target = entity;
            }
        }
        return target;
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#update(com.communote.server.model.user.User)
     */
    @Override
    public void update(com.communote.server.model.user.User user) {
        if (user == null) {
            throw new IllegalArgumentException("User.update - 'user' can not be null");
        }
        this.getHibernateTemplate().update(user);
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#update(java.util.Collection<de. User>)
     */
    @Override
    public void update(final java.util.Collection<com.communote.server.model.user.User> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("User.update - 'entities' can not be null");
        }
        this.getHibernateTemplate()
                .executeWithNativeSession(
                        new org.springframework.orm.hibernate3.HibernateCallback<com.communote.server.model.user.User>() {
                            @Override
                            public com.communote.server.model.user.User doInHibernate(
                                    org.hibernate.Session session)
                                    throws org.hibernate.HibernateException {
                                for (java.util.Iterator<com.communote.server.model.user.User> entityIterator = entities
                                        .iterator(); entityIterator.hasNext();) {
                                    update(entityIterator.next());
                                }
                                return null;
                            }
                        });
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#userFollowsItem(Long, Long)
     */
    @Override
    public boolean userFollowsItem(final Long userId, final Long globalId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.userFollowsItem(Long userId, Long globalId) - 'userId' can not be null");
        }
        if (globalId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.persistence.user.UserDao.userFollowsItem(Long userId, Long globalId) - 'globalId' can not be null");
        }
        try {
            return this.handleUserFollowsItem(userId, globalId);
        } catch (RuntimeException rt) {
            throw new RuntimeException(
                    "Error performing 'com.communote.server.persistence.user.UserDao.userFollowsItem(Long userId, Long globalId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see com.communote.server.persistence.user.UserDao#userVOToEntity(com.communote.server.api.core.user.UserVO,
     *      com.communote.server.model.user.User)
     */
    @Override
    public void userVOToEntity(com.communote.server.api.core.user.UserVO source,
            com.communote.server.model.user.User target, boolean copyIfNull) {
        if (copyIfNull || source.getPassword() != null) {
            target.setPassword(source.getPassword());
        }
        if (copyIfNull || source.getEmail() != null) {
            target.setEmail(source.getEmail());
        }
        if (copyIfNull || source.getAlias() != null) {
            target.setAlias(source.getAlias());
        }
    }

}