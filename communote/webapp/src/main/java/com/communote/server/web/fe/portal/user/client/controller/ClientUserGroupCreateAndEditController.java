package com.communote.server.web.fe.portal.user.client.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.user.AliasAlreadyExistsException;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.core.user.group.AliasValidationException;
import com.communote.server.core.user.group.GroupNotFoundException;
import com.communote.server.core.user.group.GroupOperationNotPermittedException;
import com.communote.server.core.vo.user.group.GroupVO;
import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.user.CommunoteEntityDao;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * Create User Group Controller
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientUserGroupCreateAndEditController extends SimpleFormController implements
        InitializingBean {
    /**
     * The action types this controller supports.
     *
     */
    public enum ActionType {
        /**
         * Action to create new groups.
         */
        CREATE,
        /**
         * Action to edit existing groups.
         */
        EDIT
    }

    /** Logger. */
    private final static org.apache.log4j.Logger LOG = org.apache.log4j.Logger
            .getLogger(ClientUserGroupCreateAndEditController.class);

    private static final String PARAMETER_ID = "groupId";

    private ActionType actionType;

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.actionType, "An ActionType is required");
    }

    /**
     * Handles creating a new group.
     *
     * @param request
     *            the request
     * @param form
     *            the form backing object
     * @param errors
     *            for binding errors
     * @return the model and view to show
     */
    private ModelAndView handleCreate(HttpServletRequest request, GroupVO form, BindException errors) {
        ModelAndView mav = null;
        if (validateForm(form, errors)) {
            try {
                ServiceLocator.findService(UserGroupManagement.class).createGroup(form);
                MessageHelper.saveMessage(request, MessageHelper.getText(request,
                        "client.user.group.create.success"));
                mav = new ModelAndView(getSuccessView(), getCommandName(), new GroupVO());
            } catch (AliasAlreadyExistsException e) {
                errors.rejectValue("alias", "client.user.group.create.alias.failed.exists",
                        "The alias already exists.");
            } catch (AliasValidationException e) {
                errors.rejectValue("alias", "common.alias.explanation", "The alias is not valid.");
            } catch (Exception e) {
                errors.reject("client.user.group.create.failed");
            }
        }

        return mav;
    }

    /**
     * Handles editing a group.
     *
     * @param request
     *            the request
     * @param form
     *            the form backing object
     * @param errors
     *            for binding errors
     * @return the model and view to show
     */
    private ModelAndView handleEdit(HttpServletRequest request, GroupVO form, BindException errors) {
        Long groupId = ParameterHelper.getParameterAsLong(request.getParameterMap(), PARAMETER_ID);
        ModelAndView mav = null;
        if (groupId != null && validateForm(form, errors)) {
            try {
                ServiceLocator.findService(UserGroupManagement.class).updateGroup(groupId, form);
                MessageHelper.saveMessageFromKey(request, "client.user.group.save.success");
                mav = new ModelAndView(getSuccessView(), getCommandName(), form);
            } catch (GroupNotFoundException e) {
                LOG.debug("Error updating a group.", e);
                errors.reject("client.user.group.create.failed");
            } catch (GroupOperationNotPermittedException e) {
                LOG.debug("Error updating a group.", e);
                errors.reject("client.user.group.create.failed");
            }
        }
        return mav;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
        GroupVO form = (GroupVO) command;
        ModelAndView mav = null;
        if (ActionType.CREATE.equals(actionType)) {
            mav = handleCreate(request, form, errors);
        } else {
            mav = handleEdit(request, form, errors);
        }
        if (errors.hasErrors()) {
            if (errors.hasGlobalErrors()) {
                // save the first global error in request
                MessageHelper.saveErrorMessage(request, MessageHelper.getText(request, errors
                        .getGlobalError().getCode()));
            }
            ControllerHelper.setApplicationFailure(response);
            return showForm(request, response, errors);
        }
        ControllerHelper.setApplicationSuccess(response);
        return mav;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors)
            throws Exception {
        if (!errors.hasErrors() && ActionType.EDIT.equals(actionType)) {
            // fill form
            GroupVO form = (GroupVO) command;
            Long groupId = ParameterHelper.getParameterAsLong(request.getParameterMap(),
                    PARAMETER_ID);
            if (groupId != null) {
                CommunoteEntity entity = ServiceLocator.findService(CommunoteEntityDao.class)
                        .loadWithImplementation(groupId);
                if (entity instanceof Group) {
                    Group group = (Group) entity;
                    form.setAlias(group.getAlias());
                    form.setDescription(group.getDescription());
                    form.setName(group.getName());
                    if (group instanceof ExternalUserGroup) {
                        request.setAttribute("externalSystemId", ((ExternalUserGroup) group)
                                .getExternalSystemId());
                        request.setAttribute("externalId", ((ExternalUserGroup) group)
                                .getExternalId());
                    }
                }
            }
        }
        return super.referenceData(request, command, errors);
    }

    /**
     * Sets the action type this controller will handle.
     *
     * @param action
     *            the action type to set
     */
    public void setActionType(ActionType action) {
        actionType = action;
    }

    /**
     * Validates the form and returns whether the validation succeeded, i.e. no errors where found.
     *
     * @param form
     *            the form to validate
     * @param errors
     *            for binding errors
     * @return true if no errors where found, false otherwise
     */
    private boolean validateForm(GroupVO form, BindException errors) {
        if (StringUtils.isBlank(form.getName())) {
            errors.rejectValue("name", "form.field.error.note.empty",
                    "The field must not be empty.");
        }
        if (ActionType.CREATE.equals(actionType) && StringUtils.isBlank(form.getAlias())) {
            errors.rejectValue("alias", "form.field.error.note.empty",
                    "The field must not be empty.");
        }
        return !errors.hasErrors();
    }
}