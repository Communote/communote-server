package com.communote.server.core.vo.query.blog.external;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.core.filter.listitems.blog.ExternalObjectListItem;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.PropertyQuery;
import com.communote.server.model.blog.BlogConstants;
import com.communote.server.model.external.ExternalObjectConstants;

/**
 * Query to filter for external objects over all topics given the ids
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ExternalObjectQuery extends
        PropertyQuery<ExternalObjectListItem, ExternalObjectQueryParameters> {

    private List<String> constructorParameter;
    private final static String ALIAS_TOPIC = "topic";

    private final static String ALIAS_EXTERNAL_OBJECT = "external";

    @Override
    public String buildQuery(ExternalObjectQueryParameters queryInstance) {

        // for now the external id and system id must be set otherwise this query allows to read all
        // topic ids
        if (queryInstance.getExternalId() == null || queryInstance.getExternalSystemId() == null) {

            if (!SecurityHelper.isInternalSystem()) {
                throw new IllegalArgumentException(
                        "Both externalId and externalSystemId must be set.");
            }
        }

        StringBuilder mainQuery = new StringBuilder("SELECT ");
        StringBuilder whereQuery = new StringBuilder();

        this.renderSelect(mainQuery);

        mainQuery.append("from " + BlogConstants.CLASS_NAME + " " + ALIAS_TOPIC);
        mainQuery.append(" left join " + ALIAS_TOPIC + "." + BlogConstants.EXTERNALOBJECTS + " "
                + ALIAS_EXTERNAL_OBJECT + " ");

        String wherePrefix = "";
        if (queryInstance.getExternalId() != null) {
            whereQuery.append(wherePrefix + ALIAS_EXTERNAL_OBJECT + "."
                    + ExternalObjectConstants.EXTERNALID + " = :"
                    + ExternalObjectQueryParameters.PARAM_EXTERNAL_ID + " ");
            wherePrefix = AND;
        }
        if (queryInstance.getExternalSystemId() != null) {
            whereQuery.append(wherePrefix + ALIAS_EXTERNAL_OBJECT + "."
                    + ExternalObjectConstants.EXTERNALSYSTEMID + " = :"
                    + ExternalObjectQueryParameters.PARAM_EXTERNAL_SYSTEM_ID + " ");
            wherePrefix = AND;
        }

        if (whereQuery.length() > 0) {
            mainQuery.append(" WHERE ");
            mainQuery.append(whereQuery);
        }

        mainQuery.append(" ORDER BY " + ALIAS_TOPIC + ".id ");

        return mainQuery.toString();
    }

    @Override
    public ExternalObjectQueryParameters createInstance() {
        return new ExternalObjectQueryParameters();
    }

    /**
     * @return the list with constructor parameters
     */
    protected List<String> getConstructorParameter() {
        return this.constructorParameter;
    }

    @Override
    public PageableList postQueryExecution(ExternalObjectQueryParameters queryParameters,
            PageableList result) {
        BlogRightsManagement rightsManagement = ServiceLocator
                .findService(BlogRightsManagement.class);

        for (ExternalObjectListItem item : (List<ExternalObjectListItem>) result) {

            // if the user has no access the name and identifier field is set to null
            boolean hasAccess = rightsManagement
                    .currentUserHasReadAccess(item.getTopicId(), false);
            if (!hasAccess) {
                item.setExternalName(null);
                item.setTopicNameIdentifier(null);
            }
            item.setHasAccessToTopic(hasAccess);
        }

        return result;
    }

    /**
     * @param query
     *            The query.
     */
    protected void renderSelect(StringBuilder query) {
        query.append(" new ");
        query.append(ExternalObjectListItem.class.getName());
        query.append(" ( ");
        query.append(StringUtils.join(getConstructorParameter(), ","));
        query.append(" ) ");
    }

    @Override
    protected void setupQueries() {
        constructorParameter = new ArrayList<String>();
        constructorParameter.add(ALIAS_EXTERNAL_OBJECT + "." + ExternalObjectConstants.ID);
        constructorParameter.add(ALIAS_EXTERNAL_OBJECT + "." + ExternalObjectConstants.EXTERNALID);
        constructorParameter.add(ALIAS_EXTERNAL_OBJECT + "."
                + ExternalObjectConstants.EXTERNALSYSTEMID);
        constructorParameter
                .add(ALIAS_EXTERNAL_OBJECT + "." + ExternalObjectConstants.EXTERNALNAME);
        constructorParameter.add(ALIAS_TOPIC + "." + BlogConstants.ID);
        constructorParameter.add(ALIAS_TOPIC + "." + BlogConstants.NAMEIDENTIFIER);
    }

}
