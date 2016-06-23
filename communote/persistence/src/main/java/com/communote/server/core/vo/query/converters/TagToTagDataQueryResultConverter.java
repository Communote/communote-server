package com.communote.server.core.vo.query.converters;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.communote.common.util.LocaleHelper;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.model.i18n.Message;
import com.communote.server.model.tag.Tag;


/**
 * This converter adds localized information to the RankTagListItem.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagToTagDataQueryResultConverter extends
        DirectQueryResultConverter<Tag, TagData> {

    private final Locale locale;

    /**
     * @param locale
     *            The locale to use.
     */
    public TagToTagDataQueryResultConverter(Locale locale) {
        this.locale = locale;
    }

    /**
     * Check the message for the appropriated language. if country is set than check the country.
     * 
     * @param message
     *            of tag
     * @return true or false if locale of message is the current locale of
     *         {@link TagToTagDataQueryResultConverter}
     */
    private boolean checkLocale(Message message) {
        if (message.getLanguage().getLanguageCode() == null) {
            return false;
        }
        Locale messageLocale = LocaleHelper.toLocale(message.getLanguage().getLanguageCode());
        boolean result = true;
        // check language
        if (!messageLocale.getLanguage()
                .equals(locale.getLanguage())) {
            result = false;
        } else if (StringUtils.isNotBlank(messageLocale.getCountry()) // check if country was set
                && StringUtils.isNotBlank(locale.getCountry())) {
            if (!messageLocale.getCountry().equals(locale.getCountry())) {
                result = false;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean convert(Tag source, TagData target) {
        target.setDefaultName(source.getDefaultName());
        target.setId(source.getId());
        target.setTagStoreAlias(source.getTagStoreAlias());
        target.setTagStoreTagId(source.getTagStoreTagId());
        target.setName(source.getDefaultName());
        nameLoop: for (Message name : source.getNames()) {
            // locale string is de but language code is de_DE so
            // LocaleUtils convert language codes to locales
            if (checkLocale(name)) {
                target.setName(name.getMessage());
                target.setLocale(locale);
                break nameLoop;
            }
        }
        descriptionLoop: for (Message description : source.getDescriptions()) {
            if (checkLocale(description)) {
                target.setDescription(description.getMessage());
                target.setLocale(locale);
                break descriptionLoop;
            }
        }
        return true;
    }

    /**
     * @return new {@link TagData}
     */
    @Override
    public TagData create() {
        return new TagData();
    }
}