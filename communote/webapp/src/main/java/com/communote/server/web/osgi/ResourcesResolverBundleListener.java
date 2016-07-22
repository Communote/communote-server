package com.communote.server.web.osgi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.codehaus.jackson.JsonProcessingException;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.common.io.IOHelper;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.osgi.CommunoteBundleListener;
import com.communote.server.core.osgi.OSGiHelper;
import com.communote.server.web.commons.resource.ConcatenatedResourceStoreException;
import com.communote.server.web.commons.resource.FaviconProviderManager;
import com.communote.server.web.commons.resource.ResourceCategoryRegistry;
import com.communote.server.web.commons.resource.ResourceExtension;

/**
 * Bundle listener which processes the resources contained in the bundle.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Service
public class ResourcesResolverBundleListener extends CommunoteBundleListener {

    private static final String RESOURCE_ROOT_PATH = "META-INF/resources";
    private static final String JS_CATEGORIES_FILE = "javascript-categories.json";
    private static final String CSS_CATEGORIES_FILE = "css-categories.json";

    /** Logger. */
    private final static Logger LOG = LoggerFactory
            .getLogger(ResourcesResolverBundleListener.class);

    @Autowired
    private ResourceCategoryRegistry resourceCategoryRegistry;
    @Autowired
    private FaviconProviderManager faviconProviderManager;
    private final String faviconFilepath;

    public ResourcesResolverBundleListener() {
        this.faviconFilepath = "static" + File.separator
                + BundleFaviconProvider.FAVICON_FILE_PATH.replace('/', File.separatorChar);
    }

    /**
     * Copies all additional resources out of the given bundle.
     *
     * @param bundle
     *            The bundle.
     * @return True, if any resources where copied.
     */
    private boolean copyResources(Bundle bundle) {
        Enumeration<URL> resources = bundle.findEntries(RESOURCE_ROOT_PATH, "*.*", true);
        if (resources == null || !resources.hasMoreElements()) {
            return false;
        }
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            String bundleStorage = OSGiHelper.getBundleStorage(bundle.getSymbolicName())
                    + File.separator;
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String filePath = resource.getPath();
                if (filePath.startsWith("/")) {
                    filePath = filePath.substring(1);
                }
                inputStream = resource.openStream();
                File file = new File(bundleStorage + filePath.replace(RESOURCE_ROOT_PATH, ""));
                file.getParentFile().mkdirs();
                file.createNewFile();
                fileOutputStream = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    fileOutputStream.write(buf, 0, len);
                }
                IOHelper.close(fileOutputStream);
                IOHelper.close(inputStream);
            }
        } catch (IOException e) {
            LOG.error("Error on copying a template from bundle:" + bundle.getSymbolicName(), e);
        } finally {
            IOHelper.close(fileOutputStream);
            IOHelper.close(inputStream);
        }
        return true;
    }

    @Override
    protected void fireBundleStarted(Bundle bundle) {
        LOG.debug("Bundle started: [" + bundle.getBundleId() + "]" + bundle.getSymbolicName());
        if (copyResources(bundle)) {
            registerFaviconProvider(bundle);
            registerCategories(bundle, JS_CATEGORIES_FILE);
            registerCategories(bundle, CSS_CATEGORIES_FILE);
        }
    }

    /**
     * Removes the copied velocity templates and clears all new mappings.
     *
     * @param bundle
     *            The bundle.
     */
    @Override
    protected void fireBundleStopped(Bundle bundle) {
        String bundleName = bundle.getSymbolicName();
        unregisterFaviconProvider(bundle);
        OSGiHelper.clearBundleStorage(bundleName);
        resourceCategoryRegistry.unregisterJsCategories(bundleName);
        resourceCategoryRegistry.unregisterCssCategories(bundleName);
    }

    /**
     * Extend or add resource categories with those provided by the bundle, if any.
     *
     * @param bundle
     *            the bundle
     * @param fileName
     *            the name of the file containing a JSON based definition of the categories to
     *            register at the provided store
     */
    private synchronized void registerCategories(Bundle bundle, String fileName) {
        Enumeration<URL> categoryDefs = bundle.findEntries("META-INF", fileName, false);
        if (categoryDefs != null && categoryDefs.hasMoreElements()) {
            URL categoryDefinition = categoryDefs.nextElement();
            try {
                ResourceExtension extension = JsonHelper.getSharedObjectMapper().readValue(
                        categoryDefinition, ResourceExtension.class);
                String bundleName = bundle.getSymbolicName();
                if (fileName.equals(JS_CATEGORIES_FILE)) {
                    resourceCategoryRegistry.registerJsResourceExtension(bundleName, extension);
                } else {
                    resourceCategoryRegistry.registerCssResourceExtension(bundleName, extension);
                }
            } catch (JsonProcessingException e) {
                LOG.error(
                        "Parsing categories definition " + fileName + " of bundle "
                                + bundle.getSymbolicName() + " failed", e);
            } catch (IOException e) {
                LOG.error(
                        "Reading categories definition " + fileName + " of bundle "
                                + bundle.getSymbolicName() + " failed", e);
            } catch (ConcatenatedResourceStoreException e) {
                LOG.error("Registering categories for bundle " + bundle.getSymbolicName()
                        + " defined in " + fileName + " failed", e);
            }
        }
    }

    private void registerFaviconProvider(Bundle bundle) {
        File faviconFile = new File(OSGiHelper.getBundleStorage(bundle.getSymbolicName()),
                faviconFilepath);
        if (faviconFile.exists()) {
            faviconProviderManager.addFaviconProvider(new BundleFaviconProvider(bundle
                    .getSymbolicName(), String.valueOf(bundle.getVersion().hashCode())));
        }

    }

    private void unregisterFaviconProvider(Bundle bundle) {
        File faviconFile = new File(OSGiHelper.getBundleStorage(bundle.getSymbolicName()),
                faviconFilepath);
        if (faviconFile.exists()) {
            faviconProviderManager.removeFaviconProvider(new BundleFaviconProvider(bundle
                    .getSymbolicName(), String.valueOf(bundle.getVersion().hashCode())));
        }

    }
}
