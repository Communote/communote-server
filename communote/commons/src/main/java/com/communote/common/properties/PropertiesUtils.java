package com.communote.common.properties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import com.communote.common.io.IOHelper;

/**
 * Helper class for working with {@link Properties}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class PropertiesUtils {

    private static Properties load(InputStream in) throws IOException {
        return load(in, StandardCharsets.UTF_8);
    }

    private static Properties load(InputStream in, Charset charset) throws IOException {
        BufferedReader reader = null;
        try {
            Properties props = null;

            if (in != null) {
                props = new Properties();
                // pass a decoder instead of string or charset because this way decoding errors will
                // not silently be ignored (see Charset#decode docu for details)
                reader = new BufferedReader(new InputStreamReader(in, charset.newDecoder()));
                props.load(reader);
            }
            return props;
        } catch (IllegalArgumentException e) {
            throw new IOException(
                    "Properties resource contains a malformed Unicode escape sequence", e);
        } finally {
            IOHelper.close(reader);
            IOHelper.close(in);
        }
    }

    /**
     * Load properties from the given resources using the current thread context class loader. The
     * properties are expected to be encoded in UTF-8.
     *
     * @param resourceName
     *            The resource name
     * @return The loaded properties, or null if the resource could not be found
     * @throws IOException
     *             in case an error occurred while reading the properties
     */
    public static Properties load(String resourceName) throws IOException {
        return load(resourceName, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Load properties from the given resources using the given class loader. The properties are
     * expected to be encoded in UTF-8.
     *
     * @param resourceName
     *            The resource name
     * @param loader
     *            The class loader
     * @return The loaded properties, or null if the resource could not be found
     * @throws IOException
     *             in case an error occurred while reading the properties
     */
    public static Properties load(String resourceName, ClassLoader loader) throws IOException {
        return load(loader.getResourceAsStream(resourceName));
    }

    /**
     * Load properties from the given URL. The properties are expected to be UTF-8 encoded.
     *
     * <p>
     * Note: this method does not set a read timeout!
     * </p>
     *
     * @param url
     *            the URL to load from
     *
     * @return The loaded properties
     * @throws IOException
     *             in case an error occurred while reading the properties
     */
    public static Properties load(URL url) throws IOException {
        if (url == null) {
            throw new IOException("URL is null");
        }
        return load(url.openStream());
    }

    /**
     * Load the properties from a file. The properties are expected to be UTF-8 encoded.
     *
     * @param propertiesFile
     *            The property file
     * @return The loaded properties
     * @throws IOException
     *             if the file cannot be found or read
     */
    public static Properties loadPropertiesFromFile(File propertiesFile) throws IOException {
        if (propertiesFile == null) {
            throw new FileNotFoundException("Properties file is null");
        }
        return load(new FileInputStream(propertiesFile));
    }

    /**
     * Load the properties from a file. The properties are expected to be UTF-8 encoded.
     *
     * @param propertiesFileName
     *            The name of the property file
     * @return The loaded properties
     * @throws IOException
     *             if the file cannot be found or read
     */
    public static Properties loadPropertiesFromFile(String propertiesFileName) throws IOException {
        File file = new File(propertiesFileName);
        return loadPropertiesFromFile(file);
    }

    /**
     * Stores the properties in a file. The properties are written with UTF-8 encoding.
     *
     * @param properties
     *            the properties to store
     * @param propertiesFile
     *            The property file
     * @throws IOException
     *             if the file cannot be found or written
     */
    public static void storePropertiesToFile(Properties properties, File propertiesFile)
            throws IOException {
        storePropertiesToFile(properties, propertiesFile, StandardCharsets.UTF_8);
    }

    /**
     * Stores the properties in a file.
     *
     * @param properties
     *            the properties to store
     * @param propertiesFile
     *            The property file
     * @param charset
     *            charset to use for encoding the output
     * @throws IOException
     *             if the file cannot be found or written
     */
    public static void storePropertiesToFile(Properties properties, File propertiesFile,
            Charset charset) throws IOException {
        FileOutputStream fos = null;
        BufferedWriter writer = null;
        try {
            fos = new FileOutputStream(propertiesFile);
            // pass an encoder because this way encoding errors will not silently be ignored
            // (see Charset#encode docu for details)
            writer = new BufferedWriter(new OutputStreamWriter(fos, charset.newEncoder()));
            properties.store(writer, "do not edit this file");
        } finally {
            IOHelper.close(writer);
            IOHelper.close(fos);
        }
    }

    /**
     * Its a helper class
     */
    private PropertiesUtils() {
    }
}
