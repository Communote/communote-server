package com.communote.server.web.commons.resource;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.communote.common.util.DescendingOrderComparator;
import com.communote.common.util.OrderableManager;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * Manages the available {@link FaviconProvider}s. If there are more than one provider the one with
 * the highest order value will be used.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @see com.communote.common.util.Orderable
 * @see FaviconProvider
 */
@Service
public class FaviconProviderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FaviconProviderManager.class);
    private static final String DEFAULT_FAVICON_URL = "/favicon.ico";
    private final OrderableManager<FaviconProvider> manager = new OrderableManager<FaviconProvider>(
            new DescendingOrderComparator(), false);

    /**
     * Add a new favicon provider if it does not yet exist
     *
     * @param provider
     *            the provider to add
     */
    public void addFaviconProvider(FaviconProvider provider) {
        if (manager.add(provider)) {
            LOGGER.debug("Added favicon provider {}", provider.getClass().getCanonicalName());
        } else {
            LOGGER.debug("Favicon provider {} already exists", provider.getClass()
                    .getCanonicalName());
        }
    }

    /**
     * Return the favicon icon URL. The URL will be taken from the added provider with highest order
     * value. If no provider exists the URL of the built-in default favicon will be returned.
     *
     * @param request
     *            the current request
     * @return the URL of the favicon icon
     */
    public String getFaviconUrl(HttpServletRequest request) {
        FaviconProvider provider = manager.getFirst();
        if (provider == null) {
            String url = ControllerHelper.renderRelativeUrl(request, DEFAULT_FAVICON_URL, true,
                    false);
            return ControllerHelper.appendTimestamp(url, DEFAULT_FAVICON_URL);
        }
        return provider.getUrl(request);
    }

    /**
     * Remove a previously added favicon provider
     *
     * @param provider
     *            the provider to remove
     */
    public void removeFaviconProvider(FaviconProvider provider) {
        if (manager.remove(provider)) {
            LOGGER.debug("Removed favicon provider {}", provider.getClass().getCanonicalName());
        }
    }
}
