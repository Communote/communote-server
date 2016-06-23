package com.communote.plugins.mq.message.core.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.communote.plugins.mq.message.core.data.property.StringProperty;
import com.communote.plugins.mq.message.core.data.tag.Tag;
import com.communote.plugins.mq.message.core.data.topic.ExternalObject;
import com.communote.plugins.mq.message.core.handler.converter.MqToCoreExternalObjectConverter;
import com.communote.plugins.mq.message.core.util.ConverterUtils;
import com.communote.plugins.mq.message.core.util.StoringPolicy;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.core.external.ExternalObjectAlreadyAssignedException;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.core.external.ExternalSystemNotConfiguredException;
import com.communote.server.core.external.TooManyExternalObjectsPerTopicException;

/**
 * Topic message handlers utilities
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicMessageHandlerUtils {

    /**
     * @param newProperties
     *            new properties
     * @return property TOs
     */
    public static List<StringPropertyTO> extractPropertiesTO(
            StringProperty[] newProperties) {
        return extractPropertiesTO(newProperties, StoringPolicy.MERGE);
    }

    /**
     * extracts properties TOs, based on the specified extraction policy
     * 
     * @param newProperties
     *            new properties
     * @param propertiesExtractionPolicy
     *            extraction policy
     * @return list of properties TOs
     */
    public static List<StringPropertyTO> extractPropertiesTO(
            StringProperty[] newProperties,
            StoringPolicy propertiesExtractionPolicy) {
        List<StringPropertyTO> props = new ArrayList<StringPropertyTO>();
        switch (propertiesExtractionPolicy) {
        case MERGE:
            props.addAll(ConverterUtils.convertIterableToCollection(Arrays.asList(newProperties),
                    ConverterUtils.STRING_PROPERTYTO_CONVERTER));
            break;
        default:
            break;
        }
        return props;
    }

    /**
     * @param newTags
     *            new tags
     * @return Tag TOs
     */
    public static Set<TagTO> extractTagTOs(Tag[] newTags) {
        return extractTagTOs(newTags, null, StoringPolicy.SET_NEW);
    }

    /**
     * extracts tags according to the extraction policy
     * 
     * @param newTags
     *            new tags
     * @param existingTags
     *            existing tags
     * @param policy
     *            storing policy
     * @return extracted tags
     */
    public static Set<TagTO> extractTagTOs(Tag[] newTags,
            Set<com.communote.server.model.tag.Tag> existingTags,
            StoringPolicy policy) {
        Set<TagTO> tagTOs = new HashSet<TagTO>();
        switch (policy) {
        case SET_NEW:
            tagTOs.addAll(ConverterUtils.convertIterableToCollection(Arrays.asList(newTags),
                    ConverterUtils.MQ_TAG_TAGTO_CONVERTER));
            break;
        case PRESERVE_EXISTING:
            tagTOs.addAll(ConverterUtils.convertIterableToCollection(existingTags,
                    ConverterUtils.DB_TAG_TAGTO_CONVERTER));
            break;
        case MERGE:
            // assume that a tag, which should be updated (if any), has an id equal to the existing
            // tag id. This way new tags will replace old ones in the resulting set
            tagTOs.addAll(ConverterUtils.convertIterableToCollection(Arrays.asList(newTags),
                    ConverterUtils.MQ_TAG_TAGTO_CONVERTER));
            tagTOs.addAll(ConverterUtils.convertIterableToCollection(existingTags,
                    ConverterUtils.DB_TAG_TAGTO_CONVERTER));
            break;
        case DELETE:
            tagTOs = null;
            break;
        default:
            break;
        }

        return tagTOs;
    }

    /**
     * Update the external objects of a topic according to the policy.
     * 
     * @param topicId
     *            ID of the topic
     * @param externalSystemId
     *            identifier of the external system of the external objects
     * @param newExternalObjects
     *            new external objects to assign or update
     * @param policy
     *            the storing policy to use. Currently only SET_NEW and MERGE are supported.
     * @param externalObjectManagement
     *            the service for handling external objects
     * @throws BlogAccessException
     *             in case the current user is not manager of the topic
     * @throws BlogNotFoundException
     *             in case the topic does not exist
     * @throws ExternalObjectAlreadyAssignedException
     *             in case the external object is assigned to another blog than the provided one
     * @throws ExternalSystemNotConfiguredException
     * @throws TooManyExternalObjectsPerTopicException
     */
    public static void updateExternalObjects(Long topicId, String externalSystemId,
            ExternalObject[] newExternalObjects, StoringPolicy policy,
            ExternalObjectManagement externalObjectManagement)
            throws BlogAccessException, BlogNotFoundException,
            ExternalObjectAlreadyAssignedException, TooManyExternalObjectsPerTopicException,
            ExternalSystemNotConfiguredException {

        if (StoringPolicy.SET_NEW.equals(policy) || StoringPolicy.MERGE.equals(policy)) {
            ArrayList<com.communote.server.model.external.ExternalObject> externalObjectTOs;
            externalObjectTOs = new ArrayList<com.communote.server.model.external.ExternalObject>(
                    newExternalObjects != null ? newExternalObjects.length : 0);
            MqToCoreExternalObjectConverter converter = new MqToCoreExternalObjectConverter(
                    externalSystemId);
            ConverterUtils.convertArray(newExternalObjects, externalObjectTOs, converter);
            if (StoringPolicy.SET_NEW.equals(policy)) {
                externalObjectManagement.replaceExternalObjects(topicId, externalObjectTOs);
            } else if (externalObjectTOs.size() > 0) {
                externalObjectManagement.assignOrUpdateExternalObjects(topicId, externalObjectTOs);
            }
        } else if (StoringPolicy.DELETE.equals(policy)) {
            // pass an empty collection to remove all
            List<com.communote.server.model.external.ExternalObject> externalObjectTOs = Collections
                    .emptyList();
            externalObjectManagement.replaceExternalObjects(topicId, externalObjectTOs);
        }

    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private TopicMessageHandlerUtils() {
        // Do nothing
    }

}
