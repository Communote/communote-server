package com.communote.server.web.osgi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.communote.common.io.IOHelper;
import com.communote.common.util.LocaleHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.ClientDelegateCallback;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.core.osgi.CommunoteBundleListener;
import com.communote.server.core.user.MasterDataManagement;
import com.communote.server.model.client.ClientStatus;
import com.communote.server.model.user.Language;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * This listener registers and unregisters messages from bundles.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Service
public class LocalizationResolverBundleListener extends CommunoteBundleListener {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(LocalizationResolverBundleListener.class);

    /**
     * Searches for files "error_LANGUAGECODE.html" and add's them as new error page.
     *
     * @param bundle
     *            The bundle to search in.
     * @param localizations
     *            The localizations the error page should be added to.
     * @param language
     *            The language to search for.
     */
    private void findErrorPages(Bundle bundle, Map<String, String> localizations, Language language) {
        Enumeration<URL> entries = bundle.findEntries("/", "error_" + language.getLanguageCode()
                + ".html", true);
        if (entries == null) {
            return;
        }
        while (entries.hasMoreElements()) {
            URL url = entries.nextElement();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuffer stringBuffer = new StringBuffer();
                String input = null;
                while ((input = reader.readLine()) != null) {
                    stringBuffer.append(input + "\n");
                }
                localizations.put("special.error.page", stringBuffer.toString());
            } catch (IOException e) {
                LOGGER.warn(e.getMessage());
            } finally {
                IOHelper.close(reader);
            }
        }
    }

    /**
     * This method searches and registers new languages within the given bundle.
     *
     * @param bundle
     *            The bundle to search in.
     *
     */
    private void findLanguages(Bundle bundle) {
        Enumeration<URL> entries = bundle.findEntries("/META-INF", "languages.properties", true);
        if (entries == null) {
            return;
        }
        final Map<String, String> languages = new HashMap<String, String>();
        while (entries.hasMoreElements()) {
            Properties config = new Properties();
            InputStreamReader reader = null;
            InputStream stream = null;
            try {
                stream = entries.nextElement().openStream();
                reader = new InputStreamReader(stream, "UTF-8");
                config.load(reader);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                continue;
            } finally {
                if (reader != null) {
                    IOHelper.close(reader);
                } else {
                    IOHelper.close(stream);
                }
            }
            for (Object keyObject : config.keySet()) {
                String key = keyObject.toString();
                if (!key.startsWith(MasterDataManagement.MASTERDATA_LANGUAGE_PREFIX)) {
                    continue;
                }
                String languageCode = key.replace(MasterDataManagement.MASTERDATA_LANGUAGE_PREFIX,
                        "").trim();
                String languageName = config.getProperty(key).trim();
                languages.put(languageCode, languageName);
            }
        }
        Collection<ClientTO> allClients = ServiceLocator.findService(ClientRetrievalService.class)
                .getAllActiveClients();
        for (ClientTO client : allClients) {
            if (!ClientStatus.ACTIVE.equals(client.getClientStatus())) {
                continue;
            }
            try {
                new ClientDelegate(client).execute(new ClientDelegateCallback<Object>() {
                    @Override
                    public Object doOnClient(ClientTO client) throws Exception {
                        MasterDataManagement masterDataManagement = ServiceLocator.instance()
                                .getService(MasterDataManagement.class);
                        for (Entry<String, String> language : languages.entrySet()) {
                            masterDataManagement.addLanguage(language.getKey(), language.getValue());
                        }
                        return null;
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Error adding language for client {}: {}", client.getClientId(),
                        e.getMessage());
            }
        }
    }

    /**
     * @param bundle
     *            The bundle to search in.
     */
    private void findLocalizations(Bundle bundle) {
        for (Language language : ServiceLocator.findService(MasterDataManagement.class)
                .getLanguages()) {
            Locale locale = LocaleHelper.toLocale(language.getLanguageCode());
            Map<String, String> localizations = new HashMap<String, String>();
            findNormalMessages(bundle, localizations, language);
            findErrorPages(bundle, localizations, language);
            if (!localizations.isEmpty()) {
                ResourceBundleManager.instance().addLocalizations(bundle.getSymbolicName(), locale,
                        localizations);
            }
            LOGGER.debug("Found {} localizations for {} for bundle {}", new Object[] {
                    localizations.size(), locale, bundle.getSymbolicName() });
        }
    }

    /**
     * This method searches for all files "messages_LANGUAGECODE.properties" within the given bundle
     * and adds them as messages.
     *
     * @param bundle
     *            The bundle to search in.
     * @param localizations
     *            The dictionary to add the localizations too.
     * @param language
     *            The current language.
     */
    private void findNormalMessages(Bundle bundle, Map<String, String> localizations,
            Language language) {
        Enumeration<URL> entries = bundle.findEntries("/", "messages_" + language.getLanguageCode()
                + ".properties", true);
        if (entries == null) {
            return;
        }
        while (entries.hasMoreElements()) {
            Properties config = new Properties();
            InputStreamReader reader = null;
            InputStream stream = null;
            try {
                stream = entries.nextElement().openStream();
                reader = new InputStreamReader(stream, "UTF-8");
                config.load(reader);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                continue;
            } finally {
                if (reader != null) {
                    IOHelper.close(reader);
                } else {
                    IOHelper.close(stream);
                }
            }
            for (Object keyObject : config.keySet()) {
                String key = keyObject.toString();
                localizations.put(key, config.getProperty(key));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fireBundleStarted(Bundle bundle) {
        findLanguages(bundle);
        findLocalizations(bundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fireBundleStopped(Bundle bundle) {
        ResourceBundleManager.instance().removeLocalizations(bundle.getSymbolicName());
        LOGGER.debug("Removed localization for bundle: {}", bundle.getSymbolicName());
    }
}
