package com.communote.server.web.fe.portal.user.controller;

import java.util.Map;

import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.util.InitalFiltersVO;
import com.communote.server.web.fe.portal.blog.controller.InitialFiltersViewController;


/**
 * Controller for rendering the page after selecting a user.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class UserViewController extends InitialFiltersViewController {

    private String currentUserFoundSelectedMenu;

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSelectedMenu(Map<String, Object> model) {
        if (this.currentUserFoundSelectedMenu != null) {
            Object vo = model.get(InitialFiltersViewController.KEY_INITIAL_FILTERS_VO);
            if (vo != null) {
                InitalFiltersVO filtersVO = (InitalFiltersVO) vo;
                if (filtersVO.getUserId() != null
                        && filtersVO.getUserId().equals(SecurityHelper.getCurrentUserId())) {
                    return this.currentUserFoundSelectedMenu;
                }
            }
        }
        return super.getSelectedMenu(model);
    }

    /**
     * Sets the identifier of the menu that should be marked as selected when the initial filters
     * contain the user ID of the current user. If this identifier is unset or the user ID isn't the
     * ID of the current user the exported menu identifier will be determined as described in
     * {@link #setFiltersFoundSelectedMenu(String)}
     * 
     * @param menu
     *            the menu identifier to set
     */
    public void setCurrentUserFoundSelectedMenu(String menu) {
        this.currentUserFoundSelectedMenu = menu;
    }
}
