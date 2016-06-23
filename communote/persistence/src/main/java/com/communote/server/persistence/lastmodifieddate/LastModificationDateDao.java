package com.communote.server.persistence.lastmodifieddate;

import java.util.List;

/**
 * DAO to get all last modification dates of certain entities
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface LastModificationDateDao {

    /**
     * @param <T>
     *            the type of object to be returned that holds the information
     * @param lastModificationDateFactory
     *            factory that creates objects that holds the last modification date information and
     *            will return it as list
     * @return a list of all attachment ids with their last modification date (which is the same of
     *         the note)
     */
    public <T> List<T> getAttachmentLastModificationDates(
            LastModificationDateFactory<T> lastModificationDateFactory);

    /**
     * 
     * @param <T>
     *            the type of object to be returned that holds the information
     * @param lastModificationDateFactory
     *            factory that creates objects that holds the last modification date information and
     *            will return it as list
     * @return a list of all note ids with their last modification date (which is the young of the
     *         note and topic)
     */
    public <T> List<T> getNoteLastModificationDates(
            LastModificationDateFactory<T> lastModificationDateFactory);

    /**
     * 
     * @param <T>
     *            the type of object to be returned that holds the information
     * @param lastModificationDateFactory
     *            factory that creates objects that holds the last modification date information and
     *            will return it as list
     * @return a list of all topic ids with their last modification date
     */
    public <T> List<T> getTopicLastModificationDates(
            LastModificationDateFactory<T> lastModificationDateFactory);

}