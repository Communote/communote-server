package com.communote.server.web.fe.portal.user.client.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.iprange.IpRangeFilterManagement;
import com.communote.server.persistence.security.iprange.IpRangeFilterVO;

/**
 * List all defined Ip Range filter for the client
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class IPRangeManagementController extends SimpleFormController {

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest arg0, HttpServletResponse arg1)
            throws Exception {
        List<IpRangeFilterVO> filters = ServiceLocator.instance()
                .getService(IpRangeFilterManagement.class).listFilter();
        Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("filters", filters);
        return new ModelAndView(getFormView(), attr);
    }
}
