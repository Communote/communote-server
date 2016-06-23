package com.communote.server.web.fe.portal.blog.helper;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import com.communote.common.util.PageableList;
import com.communote.server.api.core.user.UserData;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.filter.listitems.blog.member.EntityGroupListItem;
import com.communote.server.core.filter.listitems.blog.member.CommunoteEntityData;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.web.commons.helper.ImageUrlHelper;

/**
 * Helper class for user search.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserSearchHelper {
    private static final String JSON_KEY_RESULTS_RETURNED = "resultsReturned";
    private static final String JSON_KEY_MORE_RESULTS = "moreResults";
    private static final String JSON_KEY_USER_IMAGE_PATH = "imagePath";
    private static final String JSON_KEY_USER_LONG_NAME = "longName";
    private static final String JSON_KEY_USER_SHORT_NAME = "shortName";
    private static final String JSON_KEY_USER_ALIAS = "alias";
    private static final String JSON_KEY_ID = "id";

    /**
     * Creates an array of JSON objects which hold details about the found users or groups.
     * 
     * @param list
     *            the search result to process
     * @param addSummary
     *            if true a summary JSON object will be added to the top of the array. The JSON
     *            object will be created with {@link #createSearchSummaryStatement(PageableList)} .
     * @param imageSize
     *            the size of the user logo to include in the image path, can be null to not include
     *            the image path into the JSON object. For groups no image will be included.
     * @return the JSON array with the details about the users
     */
    public static ArrayNode createEntitySearchJSONResult(
            PageableList<CommunoteEntityData> list, boolean addSummary,
            ImageSizeType imageSize) {
        JsonNodeFactory nodeFactory = JsonHelper.getSharedObjectMapper()
                .getNodeFactory();
        ArrayNode result = nodeFactory.arrayNode();
        if (addSummary) {
            result.add(UserSearchHelper.createSearchSummaryStatement(list));
        }
        for (CommunoteEntityData item : list) {
            String imagePath = null;
            boolean isGroup = (item instanceof EntityGroupListItem);
            if (!isGroup && imageSize != null) {
                imagePath = ImageUrlHelper.buildUserImageUrl(item.getEntityId(), imageSize);
            }
            ObjectNode entry = createUserSearchJSONResult(item.getEntityId(),
                    item.getShortDisplayName(), item.getDisplayName(),
                    imagePath, item.getAlias());
            entry.put("isGroup", isGroup);
            result.add(entry);
        }
        return result;
    }

    /**
     * Creates a JSON Object that holds details about the number of items returned and whether there
     * are more results
     * 
     * @param results
     *            the result of a search
     * @return the JSON Object with keys resultsTotal and resultsReturned
     */
    public static ObjectNode createSearchSummaryStatement(
            PageableList<?> results) {
        ObjectNode summary = JsonHelper.getSharedObjectMapper()
                .getNodeFactory().objectNode();
        summary.put(JSON_KEY_MORE_RESULTS,
                results.getMinNumberOfAdditionalElements() > 0);
        summary.put(JSON_KEY_RESULTS_RETURNED, results.size());
        return summary;
    }

    /**
     * Creates a JSON Object with key value pairs from the parameters passed to this method. Only
     * the values (and keys) which are not null will be added.
     * 
     * @param userId
     *            the ID of the user, if null this information is not added to the JSON object
     * @param shortName
     *            a short version of the user name, if null this information is not added to the
     *            JSON object
     * @param longName
     *            a long version of the user name, if null this information is not added to the JSON
     *            object
     * @param imagePath
     *            the path to the user image, if null this information is not added to the JSON
     *            object
     * @param alias
     *            the alias of the user, if null this information is not added to the JSON object
     * @return the JSON object
     */
    public static ObjectNode createUserSearchJSONResult(Long userId,
            String shortName, String longName, String imagePath, String alias) {
        ObjectNode resultObj = JsonHelper.getSharedObjectMapper()
                .createObjectNode();
        if (imagePath != null) {
            resultObj.put(JSON_KEY_USER_IMAGE_PATH, imagePath);
        }
        if (longName != null) {
            resultObj.put(JSON_KEY_USER_LONG_NAME, longName);
        }
        if (shortName != null) {
            resultObj.put(JSON_KEY_USER_SHORT_NAME, shortName);
        }
        if (alias != null) {
            resultObj.put(JSON_KEY_USER_ALIAS, alias);
        }
        if (userId != null) {
            resultObj.put(JSON_KEY_ID, userId);
        }
        return resultObj;
    }

    /**
     * Creates an array of JSON objects which hold details about the found users.
     * 
     * @param <T>
     *            the type of the result items
     * @param results
     *            the search result to process
     * @param addSummary
     *            if true a summary JSONObject will be added to the top of the array. The JSONObject
     *            will be created with {@link #createSearchSummaryStatement(PageableList)} .
     * @param imageSize
     *            the size of the user logo to include in the image path, can be null to not include
     *            the image path into the JSON object
     * @return the JSON array with the details about the users
     */
    public static <T extends UserData> ArrayNode createUserSearchJSONResult(
            PageableList<T> results, boolean addSummary, ImageSizeType imageSize) {
        ArrayNode resultObj = JsonHelper.getSharedObjectMapper()
                .createArrayNode();
        if (addSummary) {
            resultObj.add(UserSearchHelper
                    .createSearchSummaryStatement(results));
        }
        for (UserData item : results) {
            resultObj.add(UserSearchHelper.createUserSearchJSONResult(item,
                    imageSize));
        }
        return resultObj;
    }

    /**
     * Creates a JSON object describing a user. The returned object will have the attributes listed
     * in {@link #createUserSearchJSONResult(Long, String, String, String, String)} . If imageSize
     * is null the imagePath will not be included.
     * 
     * @param item
     *            the item from which the user information will be extracted
     * @param imageSize
     *            the size of the user logo to include in the image path, can be null to not include
     *            the image path into the JSON object
     * @return the JSON object
     */
    public static ObjectNode createUserSearchJSONResult(UserData item,
            ImageSizeType imageSize) {
        String imagePath = null;
        if (imageSize != null) {
            imagePath = ImageUrlHelper.buildUserImageUrl(item.getId(), imageSize);
        }
        String longName = UserNameHelper.getDetailedUserSignature(item);
        String shortName = UserNameHelper.getSimpleDefaultUserSignature(item);
        return createUserSearchJSONResult(item.getId(), shortName, longName,
                imagePath, item.getAlias());
    }

    /**
     * private constructor for helper class
     */
    private UserSearchHelper() {
        // TODO Auto-generated constructor stub
    }
}
