package com.communote.server.core.template.velocity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.common.util.LocaleHelper;
import com.communote.common.util.Pair;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.event.EventListener;
import com.communote.server.api.core.i18n.LocalizationChangedEvent;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.core.template.NoteTemplateDefinition;
import com.communote.server.core.template.NoteTemplateService;

/**
 * Resource loader for the Velocity engine that resolves the Velocity resource names into note
 * templates managed by the {@link NoteTemplateService}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class NoteTemplateVelocityResourceLoader extends ResourceLoader implements
EventListener<LocalizationChangedEvent> {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(NoteTemplateVelocityResourceLoader.class);
    /**
     * Prefix a resource name needs to have to be recognized as a resource that can be loaded by
     * this class.
     */
    public static final String RESOURCE_NAME_PREFIX = "note_template_";
    /**
     * Character that separates the parts templateId, locale and renderMode in the resource name.
     */
    public static final char RESOURCE_NAME_PARTS_SEPARATOR = ' ';

    private NoteTemplateService templateService;

    private long resourceTemplatesLastModified;
    private boolean eventListenerRegistered;

    @Override
    public long getLastModified(Resource resource) {
        return resourceTemplatesLastModified;
    }

    /**
     * @return the lazily initialized template service
     */
    private NoteTemplateService getNoteTemplateService() {
        if (templateService == null) {
            templateService = ServiceLocator.instance().getService(NoteTemplateService.class);
        }
        return templateService;
    }

    @Override
    public Class<LocalizationChangedEvent> getObservedEvent() {
        return LocalizationChangedEvent.class;
    }

    @Override
    public InputStream getResourceStream(String resourceName) throws ResourceNotFoundException {
        Pair<LocalizedMessage, Locale> template = resolveTemplate(resourceName);
        // lazily register for the localization changed event to avoid conflicts while starting
        if (!eventListenerRegistered) {
            ServiceLocator.findService(EventDispatcher.class).register(this);
            eventListenerRegistered = true;
        }
        try {
            return new ByteArrayInputStream(template.getLeft().toString(template.getRight())
                    .getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException("Resource " + resourceName + " cannot be read", e);
        }
    }

    @Override
    public void handle(LocalizationChangedEvent event) {
        // set a new time stamp after the localizations have changed to force reload of the cached
        // templates
        LOGGER.debug("Invalidating velocity template cache");
        this.resourceTemplatesLastModified = System.currentTimeMillis();
    }

    @Override
    public void init(ExtendedProperties configuration) {
        setCachingOn(configuration.getBoolean("cache", true));
        // check interval can be small since the isSourceModified check is fast
        setModificationCheckInterval(configuration.getLong("modificationCheckInterval", 10L));
        this.resourceTemplatesLastModified = System.currentTimeMillis();
    }

    @Override
    public boolean isSourceModified(Resource resource) {
        // resource is considered modified if the last modified timestamp of the (cached) resource
        // is after timestamp of last plugin add/remove event
        return resource.getLastModified() < resourceTemplatesLastModified;
    }

    /**
     * Process the resourceName with {@link #resolveTemplateIdentifiers(String)} and return the
     * localized message object of the referenced template definition and the locale object.
     *
     * @param resourceName
     *            the velocity resource name
     * @return the message and the locale extract from the resource name
     * @throws ResourceNotFoundException
     *             in case the resource name is not an identifier for a note template or the note
     *             template does not exist
     */
    private Pair<LocalizedMessage, Locale> resolveTemplate(String resourceName)
            throws ResourceNotFoundException {
        String[] templateParts = resolveTemplateIdentifiers(resourceName);
        NoteTemplateDefinition definition = getNoteTemplateService()
                .getDefinition(templateParts[0]);
        if (definition == null) {
            throw new ResourceNotFoundException("Note template " + templateParts[0]
                    + " does not exist");
        }
        NoteRenderMode mode = null;
        Locale locale = null;
        try {
            locale = LocaleHelper.toLocale(templateParts[1]);
            if (templateParts[2] != null) {
                mode = NoteRenderMode.valueOf(templateParts[2]);
            }
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Resource name " + resourceName
                    + " cannot be parsed", e);
        }
        return new Pair<LocalizedMessage, Locale>(definition.getTemplate(mode), locale);
    }

    /**
     * Extract templateId, locale string and optionally the render mode from the resourceName.
     *
     * @param resourceName
     *            the velocity resource name to process
     * @return an array with 3 elements where the first is the templateId, the 2nd the locale string
     *         and the 3rd the render mode or null if not contained
     * @throws ResourceNotFoundException
     *             in case the resource name is not an identifier for a note template
     */
    private String[] resolveTemplateIdentifiers(String resourceName)
            throws ResourceNotFoundException {
        if (!resourceName.startsWith(RESOURCE_NAME_PREFIX)) {
            throw new ResourceNotFoundException("Resource " + resourceName
                    + " not supported by this loader");
        }
        String[] result = new String[3];
        int startIdx = RESOURCE_NAME_PREFIX.length();
        try {
            int idx = resourceName.indexOf(RESOURCE_NAME_PARTS_SEPARATOR, startIdx);
            result[0] = resourceName.substring(startIdx, idx);
            startIdx = idx + 1;
            idx = resourceName.indexOf(RESOURCE_NAME_PARTS_SEPARATOR, startIdx);
            // third part (render mode) is optional
            if (idx < 0) {
                result[1] = resourceName.substring(startIdx);
            } else {
                result[1] = resourceName.substring(startIdx, idx);
                result[2] = resourceName.substring(idx + 1);
            }
            return result;
        } catch (IndexOutOfBoundsException e) {
            throw new ResourceNotFoundException("Resource " + resourceName + " not found");
        }
    }

}
