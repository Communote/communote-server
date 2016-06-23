package com.communote.common.io;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.properties.PropertiesUtils;

import eu.medsea.mimeutil.MimeException;
import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.MimeUtil2;

/**
 * Mime type detector which works like eu.medsea.mimeutil.detector.ExtensionMimeDetector but
 * respects the order of the mime types. If an extension has a value which is a comma separated list
 * of mime types the first type will be considered as the most common for this extension and
 * therefore will be the first entry in the resulting collection.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExtensionMimeDetector extends eu.medsea.mimeutil.detector.ExtensionMimeDetector {
    private static final Logger LOGGER = LoggerFactory.getLogger(MimeUtil2.class);

    private Map<String, String[]> extensionMimeTypes;

    /**
     * No-arguments constructor which will be called by MimeDetectorRegistry
     */
    public ExtensionMimeDetector() {
        initExtensionMimeTypes();
    }

    /**
     * Search classpath for mime-types.properties resources (including JARs) and add them to the
     * given properties.
     *
     * @param extMimeTypes
     *            the properties to extend or override
     */
    private void addPropertiesFromClasspath(Properties extMimeTypes) {
        // Get an enumeration of all files on the classpath with this name. They could be in jar
        // files as well
        try {
            Enumeration<URL> e = MimeUtil.class.getClassLoader().getResources(
                    "mime-types.properties");
            while (e.hasMoreElements()) {
                URL url = e.nextElement();
                LOGGER.debug("Found custom mime-types.properties file on the classpath [{}].",
                        url);
                try {
                    Properties props = PropertiesUtils.load(url);
                    if (props.size() > 0) {
                        extMimeTypes.putAll(props);
                        LOGGER.debug(
                                "Successfully loaded custome mime-type.properties file [{}] from classpath.",
                                url);
                    }
                } catch (Exception ex) {
                    LOGGER.error("Failed while loading custom mime-type.properties file ["
                            + url + "] from classpath. File will be ignored.");
                }
            }
        } catch (Exception e) {
            LOGGER.error(
                    "Problem while processing mime-types.properties files(s) from classpath. Files will be ignored.",
                    e);
        }
    }

    /**
     * Load any mime extension mappings from a file defined with the JVM property
     * <code>mime-mappings</code> (e.g. -Dmime-mappings=../my/custom/mappings.properties) and add
     * them to the given properties
     *
     * @param extMimeTypes
     *            the properties to extend or override
     */
    private void addPropertiesFromSystemProperty(Properties extMimeTypes) {
        String fname = System.getProperty("mime-mappings");
        if (fname != null && fname.length() != 0) {
            LOGGER.debug(
                    "Found custom mime-mappings defined by the property -Dmime-mappings [{}].",
                    fname);
            try {
                Properties props = PropertiesUtils.loadPropertiesFromFile(fname);
                if (props.size() > 0) {
                    extMimeTypes.putAll(props);
                }

            } catch (IOException ex) {
                LOGGER.error(
                        "Failed to load the mime-mappings file defined by the property -Dmime-mappings [{}]",
                        fname);
            }
        }
    }

    @Override
    public Collection getMimeTypesFileName(String fileName) throws MimeException {
        LinkedHashSet<MimeType> mimeTypes = new LinkedHashSet<>();

        String fileExtension = MimeUtil.getExtension(fileName);
        while (fileExtension.length() != 0) {
            String[] types = null;
            // First try case sensitive
            types = extensionMimeTypes.get(fileExtension);
            if (types != null) {
                for (String type : types) {
                    mimeTypes.add(new MimeType(type));
                }
                return mimeTypes;
            }
            if (mimeTypes.isEmpty()) {
                // Failed to find case sensitive extension so lets try again with
                // lowercase
                types = extensionMimeTypes.get(fileExtension);
                if (types != null) {
                    for (String type : types) {
                        mimeTypes.add(new MimeType(type));
                    }
                    return mimeTypes;
                }
            }
            fileExtension = MimeUtil.getExtension(fileExtension);
        }
        return mimeTypes;
    }

    /**
     * Load all extensions like in the parent class, with the exception that we do not check the
     * home directory.
     */
    private void initExtensionMimeTypes() {
        Properties extMimeTypes = null;

        try {
            // Load the default supplied mime types of MimeUtil library
            extMimeTypes = PropertiesUtils.load("eu/medsea/mimeutil/mime-types.properties",
                    MimeUtil.class.getClassLoader());
        } catch (IOException e) {
            // just log and continue
            LOGGER.error("Error loading mime-types.properties from MimeUtil library", e);
        }

        if (extMimeTypes == null) {
            extMimeTypes = new Properties();
        }

        addPropertiesFromClasspath(extMimeTypes);
        addPropertiesFromSystemProperty(extMimeTypes);

        // Load the mime types into the known mime types map of MimeUtil and add to local mapping
        HashMap<String, String[]> extensionMimeTypes = new HashMap<>();
        Set<Entry<Object, Object>> entrySet = extMimeTypes.entrySet();
        for (Entry<Object, Object> entry : entrySet) {
            String[] types = entry.getValue().toString().split(",");
            if (types.length > 0) {
                extensionMimeTypes.put(entry.getKey().toString(), types);
                for (String type : types) {
                    MimeUtil.addKnownMimeType(type);
                }
            }
        }
        this.extensionMimeTypes = extensionMimeTypes;
    }

}
