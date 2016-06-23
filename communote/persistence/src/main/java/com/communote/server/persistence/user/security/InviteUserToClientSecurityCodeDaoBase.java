package com.communote.server.persistence.user.security;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.communote.server.model.user.security.InviteUserToClientSecurityCode;
import com.communote.server.model.user.security.InviteUserToClientSecurityCodeImpl;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>InviteUserToClientSecurityCode</code>.
 * </p>
 *
 * @see InviteUserToClientSecurityCode
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class InviteUserToClientSecurityCodeDaoBase extends HibernateDaoSupport implements
        InviteUserToClientSecurityCodeDao {

    @Override
    public Collection<InviteUserToClientSecurityCode> create(
            final Collection<InviteUserToClientSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "InviteUserToClientSecurityCode.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new HibernateCallback<InviteUserToClientSecurityCode>() {
                    @Override
                    public InviteUserToClientSecurityCode doInHibernate(Session session)
                            throws HibernateException {
                        for (Iterator<InviteUserToClientSecurityCode> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            create(entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    @Override
    public InviteUserToClientSecurityCode create(
            InviteUserToClientSecurityCode inviteUserToClientSecurityCode) {
        if (inviteUserToClientSecurityCode == null) {
            throw new IllegalArgumentException(
                    "InviteUserToClientSecurityCode.create - 'inviteUserToClientSecurityCode' can not be null");
        }
        this.getHibernateTemplate().save(inviteUserToClientSecurityCode);
        return inviteUserToClientSecurityCode;
    }

    @Override
    public void evict(InviteUserToClientSecurityCode entity) {
        this.getHibernateTemplate().evict(entity);
    }

    @Override
    public InviteUserToClientSecurityCode findByUser(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "InviteUserToClientSecurityCodeDao.findByUser(Long userId) - 'userId' can not be null");
        }

        return this.handleFindByUser(userId);
    }

    /**
     * Performs the core logic for {@link #findByUser(Long)}
     */
    protected abstract InviteUserToClientSecurityCode handleFindByUser(Long userId);

    @Override
    public InviteUserToClientSecurityCode load(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "InviteUserToClientSecurityCode.load - 'id' can not be null");
        }
        return this.getHibernateTemplate().get(InviteUserToClientSecurityCodeImpl.class, id);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public List<InviteUserToClientSecurityCode> loadAll() {
        List<?> results = this.getHibernateTemplate().loadAll(
                InviteUserToClientSecurityCodeImpl.class);
        return (List<InviteUserToClientSecurityCode>) results;
    }

    @Override
    public void remove(Collection<InviteUserToClientSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "InviteUserToClientSecurityCode.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    @Override
    public void remove(InviteUserToClientSecurityCode inviteUserToClientSecurityCode) {
        if (inviteUserToClientSecurityCode == null) {
            throw new IllegalArgumentException(
                    "InviteUserToClientSecurityCode.remove - 'inviteUserToClientSecurityCode' can not be null");
        }
        this.getHibernateTemplate().delete(inviteUserToClientSecurityCode);
    }

    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "InviteUserToClientSecurityCode.remove - 'id' can not be null");
        }
        InviteUserToClientSecurityCode entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    @Override
    public void update(final Collection<InviteUserToClientSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "InviteUserToClientSecurityCode.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new HibernateCallback<InviteUserToClientSecurityCode>() {
                    @Override
                    public InviteUserToClientSecurityCode doInHibernate(Session session)
                            throws HibernateException {
                        for (Iterator<InviteUserToClientSecurityCode> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

    @Override
    public void update(InviteUserToClientSecurityCode inviteUserToClientSecurityCode) {
        if (inviteUserToClientSecurityCode == null) {
            throw new IllegalArgumentException(
                    "InviteUserToClientSecurityCode.update - 'inviteUserToClientSecurityCode' can not be null");
        }
        this.getHibernateTemplate().update(inviteUserToClientSecurityCode);
    }
}