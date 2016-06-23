package com.communote.server.persistence.user.security;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.communote.server.model.user.security.InviteUserToBlogSecurityCode;
import com.communote.server.model.user.security.InviteUserToBlogSecurityCodeImpl;

/**
 * <p>
 * Base Spring DAO Class: is able to create, update, remove, load, and find objects of type
 * <code>InviteUserToBlogSecurityCode</code>.
 * </p>
 *
 * @see InviteUserToBlogSecurityCode
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class InviteUserToBlogSecurityCodeDaoBase extends HibernateDaoSupport implements
        InviteUserToBlogSecurityCodeDao {

    @Override
    public Collection<InviteUserToBlogSecurityCode> create(
            final Collection<InviteUserToBlogSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "InviteUserToBlogSecurityCode.create - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new HibernateCallback<InviteUserToBlogSecurityCode>() {
                    @Override
                    public InviteUserToBlogSecurityCode doInHibernate(Session session)
                            throws HibernateException {
                        for (Iterator<InviteUserToBlogSecurityCode> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            create(entityIterator.next());
                        }
                        return null;
                    }
                });
        return entities;
    }

    @Override
    public InviteUserToBlogSecurityCode create(
            InviteUserToBlogSecurityCode inviteUserToBlogSecurityCode) {
        if (inviteUserToBlogSecurityCode == null) {
            throw new IllegalArgumentException(
                    "InviteUserToBlogSecurityCode.create - 'inviteUserToBlogSecurityCode' can not be null");
        }
        this.getHibernateTemplate().save(inviteUserToBlogSecurityCode);
        return inviteUserToBlogSecurityCode;
    }

    @Override
    public void evict(InviteUserToBlogSecurityCode entity) {
        this.getHibernateTemplate().evict(entity);
    }

    @Override
    public InviteUserToBlogSecurityCode findByUser(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "InviteUserToBlogSecurityCodeDao.findByUser(Long userId) - 'userId' can not be null");
        }
        return this.handleFindByUser(userId);
    }

    protected abstract InviteUserToBlogSecurityCode handleFindByUser(Long userId);

    @Override
    public InviteUserToBlogSecurityCode load(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "InviteUserToBlogSecurityCode.load - 'id' can not be null");
        }

        return this.getHibernateTemplate().get(InviteUserToBlogSecurityCodeImpl.class, id);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public List<InviteUserToBlogSecurityCode> loadAll() {
        List<?> results = this.getHibernateTemplate().loadAll(
                InviteUserToBlogSecurityCodeImpl.class);
        return (List<InviteUserToBlogSecurityCode>) results;
    }

    @Override
    public void remove(Collection<InviteUserToBlogSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "InviteUserToBlogSecurityCode.remove - 'entities' can not be null");
        }
        this.getHibernateTemplate().deleteAll(entities);
    }

    @Override
    public void remove(InviteUserToBlogSecurityCode inviteUserToBlogSecurityCode) {
        if (inviteUserToBlogSecurityCode == null) {
            throw new IllegalArgumentException(
                    "InviteUserToBlogSecurityCode.remove - 'inviteUserToBlogSecurityCode' can not be null");
        }
        this.getHibernateTemplate().delete(inviteUserToBlogSecurityCode);
    }

    @Override
    public void remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "InviteUserToBlogSecurityCode.remove - 'id' can not be null");
        }
        InviteUserToBlogSecurityCode entity = this.load(id);
        if (entity != null) {
            this.remove(entity);
        }
    }

    /**
     * @see InviteUserToBlogSecurityCodeDao#update(java .util.Collection<
     *      InviteUserToBlogSecurityCode>)
     */
    @Override
    public void update(final Collection<InviteUserToBlogSecurityCode> entities) {
        if (entities == null) {
            throw new IllegalArgumentException(
                    "InviteUserToBlogSecurityCode.update - 'entities' can not be null");
        }
        this.getHibernateTemplate().executeWithNativeSession(
                new HibernateCallback<InviteUserToBlogSecurityCode>() {
                    @Override
                    public InviteUserToBlogSecurityCode doInHibernate(Session session)
                            throws HibernateException {
                        for (Iterator<InviteUserToBlogSecurityCode> entityIterator = entities
                                .iterator(); entityIterator.hasNext();) {
                            update(entityIterator.next());
                        }
                        return null;
                    }
                });
    }

    /**
     * @see InviteUserToBlogSecurityCodeDao#update(InviteUserToBlogSecurityCode)
     */
    @Override
    public void update(InviteUserToBlogSecurityCode inviteUserToBlogSecurityCode) {
        if (inviteUserToBlogSecurityCode == null) {
            throw new IllegalArgumentException(
                    "InviteUserToBlogSecurityCode.update - 'inviteUserToBlogSecurityCode' can not be null");
        }
        this.getHibernateTemplate().update(inviteUserToBlogSecurityCode);
    }

}