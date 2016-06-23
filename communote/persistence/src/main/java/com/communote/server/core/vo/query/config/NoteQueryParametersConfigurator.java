package com.communote.server.core.vo.query.config;

import java.util.Map;

import com.communote.common.util.ParameterHelper;
import com.communote.server.core.vo.query.TimelineFilterViewType;
import com.communote.server.core.vo.query.post.NoteQueryParameters;

/**
 * This {@link NoteQueryParametersConfigurator} can be used to configure a
 * {@link NoteQueryParameters}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteQueryParametersConfigurator
        extends TimelineQueryParametersConfigurator<NoteQueryParameters> {

    /**
     * Constructor for this {@link QueryParametersConfigurator}.
     * 
     * @param nameProvider
     *            To fetch parameters for the query instance.
     */
    public NoteQueryParametersConfigurator(QueryParametersParameterNameProvider nameProvider) {
        super(nameProvider);
        if (nameProvider == null) {
            throw new IllegalArgumentException("nameProvider cannot be null!");
        }
    }

    /**
     * Constructor for this {@link QueryParametersConfigurator}.
     * 
     * @param nameProvider
     *            To fetch parameters for the query instance.
     * @param defaultMaxCount
     *            the default value to use if maxCount parameter is not set
     */
    public NoteQueryParametersConfigurator(
            QueryParametersParameterNameProvider nameProvider, int defaultMaxCount) {
        super(nameProvider, defaultMaxCount);
        if (nameProvider == null) {
            throw new IllegalArgumentException("nameProvider cannot be null!");
        }
    }

    /**
     * Method to start the configuration process.
     * 
     * @param parameters
     *            Map which holds the parameters to be used to configure the query instance.
     * @param queryParameters
     *            Class of type QueryInstance.
     */
    @Override
    public void configure(Map<String, ? extends Object> parameters,
            NoteQueryParameters queryParameters) {
        super.configure(parameters, queryParameters);
        queryParameters.setRetrieveOnlyNotesAfterId(ParameterHelper.getParameterAsLong(parameters,
                getParameterNameProvider().getNameForRetrieveOnlyNotesAfterId()));
        queryParameters.setRetrieveOnlyNotesBeforeId(ParameterHelper.getParameterAsLong(parameters,
                getParameterNameProvider().getNameForRetrieveOnlyNotesBeforeId()));
        queryParameters.setRetrieveOnlyNotesAfterDate(ParameterHelper.getParameterAsDate(
                parameters, getParameterNameProvider().getNameForRetrieveOnlyNotesAfterDate()));
        queryParameters.setRetrieveOnlyNotesBeforeDate(ParameterHelper.getParameterAsDate(
                parameters, getParameterNameProvider().getNameForRetrieveOnlyNotesBeforeDate()));
        configureSelectedViewType(parameters, queryParameters);
    }

    /**
     * Configures the query for the selected view type.
     * 
     * @param parameters
     *            Map with parameters.
     * @param queryParameters
     *            Class of type QueryInstance.
     */
    private void configureSelectedViewType(
            Map<String, ? extends Object> parameters, NoteQueryParameters queryParameters) {
        TimelineFilterViewType selectViewType = determineViewType(parameters);
        queryParameters.setTimelineFilterViewType(selectViewType);
    }
}
