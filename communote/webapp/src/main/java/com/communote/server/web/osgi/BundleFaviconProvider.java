package com.communote.server.web.osgi;

import javax.servlet.http.HttpServletRequest;

import com.communote.server.web.commons.helper.ControllerHelper;
import com.communote.server.web.commons.resource.FaviconProvider;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BundleFaviconProvider implements FaviconProvider {

    /**
     * relative location to the favicon icon file within the bundle storage
     */
    public static final String FAVICON_FILE_PATH = "/images/favicon.ico";

    private final String url;

    public BundleFaviconProvider(String bundleSymbolicName, String versionString) {
        this.url = "/plugins/" + bundleSymbolicName + FAVICON_FILE_PATH + "?t=" + versionString;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BundleFaviconProvider other = (BundleFaviconProvider) obj;
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }
        return true;
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public String getUrl(HttpServletRequest request) {
        return ControllerHelper.renderRelativeUrl(request, url, false, false);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

}
