package com.communote.server.web.fe.portal.user.forms;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.model.user.User;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SearchUserForm {

    private List<User> searchResults;
    private String action;
    private String searchString;
    private ResultSpecification resultSpecification;

    /**
     * Constructor.
     */
    public SearchUserForm() {
        // TODO: ResultSpecification tempor&auml;r gesetzt
        resultSpecification = new ResultSpecification(0, 10);
    }

    /**
     * Returns the action of the form
     * 
     * @return Returns action
     */
    public String getAction() {
        return action;
    }

    /**
     * @return the result specification
     */
    public ResultSpecification getResultSpecification() {
        return resultSpecification;
    }

    /**
     * Returns the search results
     * 
     * @return search results
     */
    public List<User> getSearchResults() {
        return searchResults;
    }

    /**
     * Returns the search string
     * 
     * @return search result
     */
    public String getSearchString() {
        return searchString;
    }

    /**
     * @param action
     *            the action
     */
    public void setAction(String action) {
        this.action = StringUtils.trim(action);
    }

    /**
     * @param resultSpecification
     *            the result spec
     */
    public void setResultSpecification(ResultSpecification resultSpecification) {
        this.resultSpecification = resultSpecification;
    }

    /**
     * @param searchResults
     *            the search results
     */
    public void setSearchResults(List<User> searchResults) {
        this.searchResults = searchResults;
    }

    /**
     * @param searchString
     *            the search string
     */
    public void setSearchString(String searchString) {
        this.searchString = StringUtils.trim(searchString);

    }

}
