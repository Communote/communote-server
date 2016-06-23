package com.communote.plugins.api.rest.v22.converter;

import com.communote.plugins.api.rest.v22.resource.timelinetopic.TimelineTopicResource;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.core.vo.query.user.DataAccessUserConverter;

/**
 * BlogDataToTimelineTopicConverter to convert the temporary object into a TimelineTopicResource
 * 
 * @param <T>
 *            The {@link BlogData} which is the incoming list
 * @param <O>
 *            The {@link TimelineTopicResource} which is the final list
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogDataToTimelineTopicConverter<T extends BlogData, O extends TimelineTopicResource>
        extends DataAccessUserConverter<T, O> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean convert(BlogData source, TimelineTopicResource target) {
        target.setAlias(source.getAlias());
        target.setTopicId(source.getId());
        target.setTitle(source.getTitle());
        return true;
    }

    /**
     * @return new {@link TimelineTopicResource}
     */
    @SuppressWarnings("unchecked")
    @Override
    public O create() {
        return (O) new TimelineTopicResource();
    }
}
