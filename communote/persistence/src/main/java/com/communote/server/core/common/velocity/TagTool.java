package com.communote.server.core.common.velocity;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagSuggestionConfiguration;
import com.communote.server.core.tag.TagSuggestionManagement;
import com.communote.server.model.tag.Tag;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagTool {

    /**
     * Create a serialized JSON array of objects that describe the available tag suggestions for
     * blog tag stores.
     * 
     * @param assignedTagsOnly
     *            only retrieve suggestions of tags that are already assigned to a Communote entity
     * @param aliasAttribute
     *            name of the attribute in the JSON object that holds the tag suggestion alias
     * @param titleAttribute
     *            name of the attribute in the JSON object that holds the localized name of the
     *            suggestion
     * @param providerAttribute
     *            name of the attribute in the JSON object that holds the alias of the tag
     *            suggestion provider
     * @param request
     *            the current request
     * @return the serialized JSON array
     */
    public String getBlogTagSuggestions(boolean assignedTagsOnly, String aliasAttribute,
            String titleAttribute, String providerAttribute, HttpServletRequest request) {
        return this.getTagSuggestions(TagStoreType.Types.BLOG, assignedTagsOnly, aliasAttribute,
                titleAttribute, providerAttribute, request);
    }

    /**
     * Create a serialized JSON array of objects that describe the available tag suggestions for
     * entity tag stores.
     * 
     * @param assignedTagsOnly
     *            only retrieve suggestions of tags that are already assigned to a Communote entity
     * @param aliasAttribute
     *            name of the attribute in the JSON object that holds the tag suggestion alias
     * @param titleAttribute
     *            name of the attribute in the JSON object that holds the localized name of the
     *            suggestion
     * @param providerAttribute
     *            name of the attribute in the JSON object that holds the alias of the tag
     *            suggestion provider
     * @param request
     *            the current request
     * @return the serialized JSON array
     */
    public String getEntityTagSuggestions(boolean assignedTagsOnly, String aliasAttribute,
            String titleAttribute, String providerAttribute, HttpServletRequest request) {
        return this.getTagSuggestions(TagStoreType.Types.ENTITY, assignedTagsOnly, aliasAttribute,
                titleAttribute, providerAttribute, request);
    }

    /**
     * @param tagId
     *            Id of the tag.
     * @param request
     *            The request.
     * @return Name of the tag or null if it does not exist.
     */
    public String getName(long tagId, HttpServletRequest request) {
        Locale locale = SessionHandler.instance().getCurrentLocale(request);
        TagData tag = ServiceLocator.instance().getService(TagManagement.class)
                .findTag(tagId, locale);
        if (tag != null) {
            return tag.getName();
        }
        return null;
    }

    /**
     * @param tagName
     *            The tag.
     * @return The id for this tag for notes or 0 if the tag doesn't exists.
     */
    public long getNoteTagId(String tagName) {
        Tag tag = ServiceLocator.instance().getService(TagManagement.class)
                .findTag(tagName, TagStoreType.Types.NOTE);
        return tag == null ? 0 : tag.getId();
    }

    /**
     * Create a serialized JSON array of objects that describe the available tag suggestions for
     * note tag stores.
     * 
     * @param assignedTagsOnly
     *            only retrieve suggestions of tags that are already assigned to a Communote entity
     * @param aliasAttribute
     *            name of the attribute in the JSON object that holds the tag suggestion alias
     * @param titleAttribute
     *            name of the attribute in the JSON object that holds the localized name of the
     *            suggestion
     * @param providerAttribute
     *            name of the attribute in the JSON object that holds the alias of the tag
     *            suggestion provider
     * @param request
     *            the current request
     * @return the serialized JSON array
     */
    public String getNoteTagSuggestions(boolean assignedTagsOnly, String aliasAttribute,
            String titleAttribute, String providerAttribute, HttpServletRequest request) {
        return this.getTagSuggestions(TagStoreType.Types.NOTE, assignedTagsOnly, aliasAttribute,
                titleAttribute, providerAttribute, request);
    }

    /**
     * Create a serialized JSON array of objects that describe the available tag suggestions for a
     * given tag store type.
     * 
     * @param tagStoreType
     *            the tag store type for which the suggestions should be retrieved
     * @param assignedTagsOnly
     *            only retrieve suggestions of tags that are already assigned to a Communote entity
     * @param aliasAttribute
     *            name of the attribute in the JSON object that holds the tag suggestion alias
     * @param titleAttribute
     *            name of the attribute in the JSON object that holds the localized name of the
     *            suggestion
     * @param providerAttribute
     *            name of the attribute in the JSON object that holds the alias of the tag
     *            suggestion provider
     * @param request
     *            the current request
     * @return the serialized JSON array
     */
    public String getTagSuggestions(TagStoreType tagStoreType, boolean assignedTagsOnly,
            String aliasAttribute, String titleAttribute, String providerAttribute,
            HttpServletRequest request) {
        List<TagSuggestionConfiguration> configs = ServiceLocator.instance()
                .getService(TagSuggestionManagement.class)
                .getTagSuggestionConfigurations(tagStoreType, assignedTagsOnly);
        ArrayNode resultObj = JsonHelper.getSharedObjectMapper().createArrayNode();
        if (configs.size() > 0) {
            Locale locale = SessionHandler.instance().getCurrentLocale(request);
            String msgKey;
            for (TagSuggestionConfiguration config : configs) {
                ObjectNode suggestion = resultObj.addObject();
                suggestion.put(aliasAttribute, config.getTagSuggestionAlias());
                suggestion.put(providerAttribute, config.getTagSuggestionProviderAlias());
                // use simpler static category title if there is only one category which is the
                // built-in default
                if (configs.size() == 1 && config.getTagSuggestionAlias().startsWith("Default")) {
                    msgKey = "autosuggest.title.tags";
                } else {
                    msgKey = config.getLocalizedName();
                }
                suggestion.put(titleAttribute,
                        ResourceBundleManager.instance().getText(msgKey, locale));
            }
        }
        return JsonHelper.writeJsonTreeAsString(resultObj);
    }
}
