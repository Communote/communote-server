package com.communote.server.core.template;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.tools.ToolManager;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.template.velocity.NoteTemplateVelocityResourceLoader;

/**
 * This class provides services to support notes which have a predefined content that is based on a
 * template.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class NoteTemplateService {

    private final static Logger LOGGER = LoggerFactory.getLogger(NoteTemplateService.class);

    /**
     * Key of the NoteProperty that holds the template ID.
     */
    public static final String NOTE_PROPERTY_KEY_TEMPLATE_ID = "note.template.id";
    /**
     * Key of the NoteProperty that holds the template properties.
     */
    public static final String NOTE_PROPERTY_KEY_TEMPLATE_PROPERTIES = "note.template.properties";

    private final ConcurrentHashMap<String, NoteTemplateDefinition> templateDefinitions = new ConcurrentHashMap<String, NoteTemplateDefinition>();
    // hold tools to be added to the velocity context. We do this manually because it's too
    // complicated to add tools to the velocity tool manager programmatically.
    private final ConcurrentHashMap<String, Object> tools = new ConcurrentHashMap<String, Object>();
    private ToolManager toolManager;
    @Autowired
    private PropertyManagement propertyManagement;

    @Autowired
    private VelocityEngine velocityEngine;

    /**
     * Add a template definition. A definition is identified by its ID. If there is already a
     * definition with that ID the new one will be ignored.
     *
     * @param definition
     *            the template definition to add
     * @return whether the definition was added
     */
    public boolean addDefinition(NoteTemplateDefinition definition) {
        synchronized (templateDefinitions) {
            if (!templateDefinitions.contains(definition.getTemplateId())) {
                templateDefinitions.put(definition.getTemplateId(), definition);
                return true;
            }
            LOGGER.debug("Ignored template definition with ID {} because it already exists",
                    definition.getTemplateId());
            return false;
        }
    }

    /**
     * Add a tool which should be accessible from within the template. The tool can be any arbitrary
     * object that provides some utility functions. It will be added to the velocity context under
     * the provided key so that its methods are accessible like those of any other object in the
     * context. Because the tool is shared between all invocations, it must be thread safe.
     *
     * @param toolKey
     *            the key under which the tool should be accessible. If there is already a tool for
     *            the key the new one will be ignored.
     * @param tool
     *            the tool to add
     * @return true if the tool was added, false otherwise
     */
    public boolean addTool(String toolKey, Object tool) {
        synchronized (tools) {
            if (toolKey != null && tool != null && !tools.containsKey(toolKey)) {
                tools.put(toolKey, tool);
                return true;
            }
            LOGGER.debug("Ignoring tool with key {} because it already exists or is invalid",
                    toolKey);
            return false;
        }
    }

    /**
     * Add the tools to the context.
     *
     * @param context
     *            the context to add the tools to
     */
    private void addTools(Context context) {
        for (Map.Entry<String, Object> toolDef : tools.entrySet()) {
            context.put(toolDef.getKey(), toolDef.getValue());
        }
    }

    /**
     * Build a resource name that can be processed by the {@link NoteTemplateVelocityResourceLoader}
     *
     * @param definition
     *            the template definition that should be identified by the resource name
     * @param renderMode
     *            the current render mode
     * @param locale
     *            the locale to use for rendering the template
     * @return the resource name
     */
    private String buildVelocityResourceName(NoteTemplateDefinition definition,
            NoteRenderMode renderMode, Locale locale) {
        StringBuilder builder = new StringBuilder(
                NoteTemplateVelocityResourceLoader.RESOURCE_NAME_PREFIX);
        builder.append(definition.getTemplateId());
        builder.append(NoteTemplateVelocityResourceLoader.RESOURCE_NAME_PARTS_SEPARATOR);
        builder.append(locale.toString());
        // only append render mode if the definition actually has a separate template for the mode
        // to improve caching
        if (definition.hasTemplate(renderMode)) {
            builder.append(NoteTemplateVelocityResourceLoader.RESOURCE_NAME_PARTS_SEPARATOR);
            builder.append(renderMode.name());
        }
        return builder.toString();
    }

    /**
     * Return the template definition for the given ID or null if there is none.
     *
     * @param templateId
     *            the ID for which a definition should be returned
     * @return the definition or null
     */
    public NoteTemplateDefinition getDefinition(String templateId) {
        return templateDefinitions.get(templateId);
    }

    /**
     * initialize required components
     */
    @PostConstruct
    public void init() {
        toolManager = new ToolManager(false, false);
        toolManager.setVelocityEngine(velocityEngine);
        toolManager.configure("com/communote/server/core/note/template/velocity-toolbox.xml");
        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty,
                PropertyManagement.KEY_GROUP, NOTE_PROPERTY_KEY_TEMPLATE_ID);
        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty,
                PropertyManagement.KEY_GROUP, NOTE_PROPERTY_KEY_TEMPLATE_PROPERTIES);
    }

    /**
     * Convert the template properties JSON string into a map structure and calls the validator if
     * required and defined.
     *
     * @param definition
     *            the template definition
     * @param templatePropertiesJSON
     *            the JSON string to process, can be null
     * @param validate
     *            whether to call the validator of the template definition
     * @return the JSON object as nested map or null
     * @throws NoteTemplateRenderException
     *             in case the properties string isn't valid JSON or the validation failed
     */
    private HashMap<String, Object> prepareTemplateProperties(NoteTemplateDefinition definition,
            String templatePropertiesJSON, boolean validate) throws NoteTemplateRenderException {
        HashMap<String, Object> jsonObject = null;
        if (templatePropertiesJSON != null) {
            // convert JSON string to nested map
            try {
                // TODO should we cache the converted JSON? If so we should use StringProperty
                // class because it provides an ID and a timestamp for identification and
                // invalidation
                jsonObject = JsonHelper.getSharedObjectMapper().readValue(templatePropertiesJSON,
                        new TypeReference<HashMap<String, Object>>() {
                });
            } catch (JsonProcessingException e) {
                LOGGER.error("templatePropertiesJSON string is not valid JSON", e);
                throw new NoteTemplateRenderException(definition.getTemplateId(),
                        "templatePropertiesJSON string is not valid JSON", e);
            } catch (IOException e) {
                LOGGER.error("Processing templateProperties failed", e);
                throw new NoteTemplateRenderException(definition.getTemplateId(), e);
            }
        }
        if (validate) {
            definition.validateProperties(jsonObject);
        }
        return jsonObject;
    }

    /**
     * Remove a template definition.
     *
     * @param templateId
     *            the ID of the template definition to remove. In case there is no definition for
     *            the ID nothing will happen.
     */
    public void removeDefinition(String templateId) {
        synchronized (templateDefinitions) {
            templateDefinitions.remove(templateId);
        }
    }

    /**
     * Remove a tool that was previously added via {@link #addTool(String, Object)}
     *
     * @param toolKey
     *            the key of the tool to remove. In case there is no tool for the key nothing will
     *            happen.
     */
    public void removeTool(String toolKey) {
        synchronized (tools) {
            tools.remove(toolKey);
        }
    }

    /**
     * Render a template and returns the resulting string which can than be used as note content.
     *
     * @param templateId
     *            the ID of the template definition whose template should be rendered
     * @param templatePropertiesJSON
     *            optional string in JSON which should be passed to the template engine and can
     *            contain any additional data the template might need. The string is converted into
     *            a nested map for easier processing within the template. The variable is called
     *            'props'.
     * @param renderContext
     *            the render context to use when getting the actual template from the template
     *            definition. The rendering context will also be put into the velocity context so it
     *            can be used in the template. The variable is called 'context'
     * @param validateTemplateProperties
     *            whether to call the validator of the template definition to validate the template
     *            properties. In case the definition has no validator this argument is ignored.
     * @return the rendered template
     * @throws NoteTemplateNotFoundException
     *             in case there is no template definition for the given ID
     * @throws NoteTemplateRenderException
     *             in case of an error while rendering the template
     */
    public String renderTemplate(String templateId, String templatePropertiesJSON,
            NoteRenderContext renderContext, boolean validateTemplateProperties)
                    throws NoteTemplateNotFoundException, NoteTemplateRenderException {
        NoteTemplateDefinition definition = getDefinition(templateId);
        if (definition == null) {
            throw new NoteTemplateNotFoundException(templateId);
        }
        Context velocityContext = toolManager.createContext();
        addTools(velocityContext);
        velocityContext.put("context", renderContext);
        HashMap<String, Object> jsonObject = prepareTemplateProperties(definition,
                templatePropertiesJSON, validateTemplateProperties);
        if (jsonObject != null) {
            velocityContext.put("props", jsonObject);
        }
        String velocityResourceName = buildVelocityResourceName(definition,
                renderContext.getMode(), renderContext.getLocale());
        StringWriter result = new StringWriter();
        try {
            velocityEngine.mergeTemplate(velocityResourceName, "UTF-8", velocityContext, result);
        } catch (VelocityException e) {
            LOGGER.error("VelocityEngine couldn't render the template", e);
            throw new NoteTemplateRenderException(templateId,
                    "VelocityEngine couldn't render the template", e);
        }
        return result.toString();
    }
}
