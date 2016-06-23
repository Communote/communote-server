package com.communote.server.core.vo.query;

/**
 * Enum of available view types.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum TimelineFilterViewType {

    /** The classical communote view, which shows notes ordered by descending creation date. */
    CLASSIC,
    /** View, which shows notes with the newest comments first. */
    COMMENT,
    /** Shows a discussion in the threaded view. */
    THREAD;

    /**
     * Method to convert ViewTypes concatenated with comma as String into an Array of ViewType.
     * 
     * @param viewTypesAsString
     *            Comma separated list of ViewTypes as String.
     * @return Array of ViewTypes defined through viewTypesAsString.
     */
    public static TimelineFilterViewType[] valuesOf(String viewTypesAsString) {
        if (viewTypesAsString == null || viewTypesAsString.isEmpty()) {
            return new TimelineFilterViewType[0];
        }
        String[] viewTypesArray = viewTypesAsString.split(",");
        TimelineFilterViewType[] viewTypes = new TimelineFilterViewType[viewTypesArray.length];
        for (int i = 0; i < viewTypesArray.length; i++) {
            viewTypes[i] = valueOf(viewTypesArray[i].toUpperCase());
        }
        return viewTypes;
    }
}