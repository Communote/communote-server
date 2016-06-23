package com.communote.server.web.fe.portal.user.client.validator;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.communote.server.core.security.iprange.InvalidIpAddressException;
import com.communote.server.core.security.iprange.InvalidIpRangeException;
import com.communote.server.core.security.iprange.IpRangeHelper;
import com.communote.server.web.fe.portal.user.client.controller.AddUpdateIPRangeController;
import com.communote.server.web.fe.portal.user.client.forms.IpRangeFilterItem;

/**
 * This validator validates the formular input for an ip range filter
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AddUpdateIPRangeValidator implements Validator {

    /**
     * {@inheritDoc}
     */
    public boolean supports(Class<?> clazz) {
        return IpRangeFilterItem.class.equals(clazz);
    }

    /**
     * {@inheritDoc}
     */
    public void validate(Object commandObject, Errors errors) {
        IpRangeFilterItem item = (IpRangeFilterItem) commandObject;
        if (StringUtils.isNotEmpty(item.getAction())
                && item.getAction().equals(AddUpdateIPRangeController.ACTION_DELETE)) {
            return;
        }
        if (StringUtils.isEmpty(item.getName())) {
            errors.rejectValue("name", "client.iprange.update.error.name.empty",
                    "The filter name can not be empty");
        }
        if (StringUtils.isNotEmpty(item.getExcludes())) {
            try {
                IpRangeHelper.stringToRanges(item.getExcludes(), ",");
            } catch (InvalidIpRangeException e) {
                errors.rejectValue("excludes", "client.iprange.ip.validation.error",
                        "The inserted Ip address or range is not valid");
            } catch (InvalidIpAddressException e) {
                errors.rejectValue("excludes", "client.iprange.ip.validation.error",
                        "The inserted Ip address or range is not valid");
            }
        }
        if (StringUtils.isNotEmpty(item.getIncludes())) {
            try {
                IpRangeHelper.stringToRanges(item.getIncludes(), ",");
            } catch (InvalidIpRangeException e) {
                errors.rejectValue("includes", "client.iprange.ip.validation.error",
                        "The inserted Ip address or range is not valid");
            } catch (InvalidIpAddressException e) {
                errors.rejectValue("includes", "client.iprange.ip.validation.error",
                        "The inserted Ip address or range is not valid");
            }
        }
    }

}
