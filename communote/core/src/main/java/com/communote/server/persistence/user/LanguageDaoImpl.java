package com.communote.server.persistence.user;

import java.util.List;

import com.communote.server.core.common.exceptions.IllegalDatabaseState;
import com.communote.server.model.user.Language;
import com.communote.server.model.user.LanguageConstants;


/**
 * @see com.communote.server.model.user.Language
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LanguageDaoImpl extends com.communote.server.persistence.user.LanguageDaoBase {

    private final static String FIND_BY_LANGUAGE_CODE_QUERY = "select language from "
            + LanguageConstants.CLASS_NAME + " language where lower(language."
            + LanguageConstants.LANGUAGECODE + ")=lower(?)";

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Language handleFindByLanguageCode(String languageCode) {
        List<Language> results = getHibernateTemplate().find(
                FIND_BY_LANGUAGE_CODE_QUERY.toString(), languageCode);
        if (results.size() == 0) {
            return null;
        }
        if (results.size() > 1) {
            throw new IllegalDatabaseState(
                    "Cannot have more than one language with the same language code. LanguageCode: "
                            + languageCode);
        }
        return results.iterator().next();
    }
}
