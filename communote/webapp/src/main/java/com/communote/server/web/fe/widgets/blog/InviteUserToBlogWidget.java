package com.communote.server.web.fe.widgets.blog;

import java.util.ArrayList;
import java.util.List;

import com.communote.server.core.blog.helper.BlogRoleHelper;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.persistence.user.InvitationField;
import com.communote.server.web.WebServiceLocator;
import com.communote.server.web.fe.portal.user.client.controller.AbstractUserInviteController;
import com.communote.server.widgets.EmptyWidget;

/**
 * Widget for inviting new users to a blog.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InviteUserToBlogWidget extends EmptyWidget {

    @Override
    public Object handleRequest() {
        getRequest().setAttribute("userGroupRoleLiterals",
                BlogRoleHelper.getBlogRolesSortedByAccess());
        getRequest().setAttribute(
                "blogRoles",
                BlogRoleHelper.getBlogRoles(SessionHandler.instance()
                        .getCurrentLocale(getRequest())));
        getRequest().setAttribute("BlogRole", BlogRole.class);
        setInvitationFields();
        return null;
    }

    /**
     * @param outputType
     *            Ignored.
     * 
     * @return "core.widget.blog.invite.user"
     */
    @Override
    public String getTile(String outputType) {
        return "core.widget.blog.invite.user";
    }

    /**
     * Set the invitation fields in the request
     */
    private void setInvitationFields() {
        List<InvitationField> fields = WebServiceLocator.instance().getInvitationProvider(null)
                .getInvitationFields();
        List<String> stringFields = new ArrayList<String>();
        for (InvitationField field : fields) {
            stringFields.add(field.getName());
        }
        getRequest()
                .setAttribute(AbstractUserInviteController.ATTR_INVITATION_FIELDS, stringFields);
    }
}
