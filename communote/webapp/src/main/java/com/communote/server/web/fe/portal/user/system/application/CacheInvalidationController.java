package com.communote.server.web.fe.portal.user.system.application;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.caching.CacheException;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.controller.BaseFormController;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class CacheInvalidationController extends BaseFormController {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        List<String> invalidateCachesList = ServiceLocator.findService(CacheManager.class)
                .getAdditionalCaches();

        Map<String, String> invalidateCachesMap = new TreeMap<String, String>();
        for (String invalidateCache : invalidateCachesList) {
            String cacheName = MessageHelper.getText(request,
                    "client.system.application.cacheinvalidation.caches." + invalidateCache);
            if (StringUtils.isBlank(cacheName)) {
                cacheName = MessageHelper.getText(request,
                        "client.system.application.cacheinvalidation.caches.unspecified",
                        new String[] { invalidateCache });
            }
            invalidateCachesMap.put(invalidateCache, cacheName);
        }

        request.setAttribute("invalidateCachesMap", invalidateCachesMap);
        return new CacheInvalidationForm();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleOnSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors)
                    throws Exception {
        CacheInvalidationForm form = (CacheInvalidationForm) command;

        try {
            if (form.getMainCacheEnabled()) {
                ServiceLocator.findService(CacheManager.class).invalidateMainCache();
            }
            for (String cache : form.getInvalidateCaches()) {
                ServiceLocator.findService(CacheManager.class).invalidateAdditionalCache(cache);
            }
            MessageHelper.saveMessageFromKey(request,
                    "client.system.application.cacheinvalidation.success");
        } catch (CacheException e) {
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.application.cacheinvalidation.error");
        }
        return new ModelAndView(getSuccessView(), "command", form);
    }
}
