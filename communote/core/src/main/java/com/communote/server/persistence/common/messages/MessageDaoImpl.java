package com.communote.server.persistence.common.messages;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.communote.server.model.i18n.Message;
import com.communote.server.model.i18n.MessageConstants;
import com.communote.server.model.user.LanguageConstants;


/**
 * @see com.communote.server.model.i18n.Message
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MessageDaoImpl
        extends com.communote.server.persistence.common.messages.MessageDaoBase {
    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.common.messages.MessageDao#find(String)
     */
    @Override
    protected Collection<Message> handleFind(String key) {
        Criteria criteria = getSession().createCriteria(Message.class);
        criteria.add(Restrictions.eq(MessageConstants.MESSAGEKEY, key));
        return criteria.list();
    }

    /**
     * 
     * {@inheritDoc}
     * 
     * @see com.communote.server.persistence.common.messages.MessageDao#find(String,
     *      com.communote.server.persistence.user.Language)
     */
    @Override
    protected Message handleFind(String key, String languageCode) {
        Criteria criteria = getSession().createCriteria(Message.class);
        criteria.add(Restrictions.eq(MessageConstants.MESSAGEKEY, key));
        Criteria join = criteria.createCriteria(MessageConstants.LANGUAGE);
        join.add(Restrictions.eq(LanguageConstants.LANGUAGECODE, languageCode));
        return (Message) criteria.uniqueResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Message handleFind(String key, String languageCode, String fallbackLanguageCode) {
        Message message = find(key, languageCode);
        if (message != null) {
            return message;
        }
        return find(key, fallbackLanguageCode);
    }
}