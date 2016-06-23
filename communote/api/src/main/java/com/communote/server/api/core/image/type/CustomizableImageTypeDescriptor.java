package com.communote.server.api.core.image.type;

import java.awt.Color;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import com.communote.common.util.ColorUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.util.JsonHelper;

/**
 * Image type descriptor that allows the customization of some of the scaling settings. These
 * customizations are stored in a client configuration property.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public abstract class CustomizableImageTypeDescriptor extends DefaultImageTypeDescriptor {

    private static final String SETTING_JSON_FIELD_VALIGN = "vAlign";
    private static final String SETTING_JSON_FIELD_HALIGN = "hAlign";
    private static final String SETTING_JSON_FIELD_COLOR = "color";
    private final ClientConfigurationPropertyConstant customizationHoldingProperty;
    private String customizationPropertyValue;
    private Color backgroundColor;
    private Integer verticalAlignment;
    private Integer horizontalAlignment;

    /**
     * Create a customizable descriptor.
     *
     * @param customizationHoldingProperty
     *            the property which should store the custom values
     */
    public CustomizableImageTypeDescriptor(
            ClientConfigurationPropertyConstant customizationHoldingProperty) {
        super();
        this.customizationHoldingProperty = customizationHoldingProperty;
    }

    /**
     * @return the custom value or the default inherited from the parent
     */
    @Override
    public Color getBackgroundColor() {
        processProperty();
        if (backgroundColor == null) {
            return super.getBackgroundColor();
        }
        return backgroundColor;
    }

    private ConfigurationManager getConfigurationManager() {
        return CommunoteRuntime.getInstance().getConfigurationManager();
    }

    /**
     * @return the custom value or the default inherited from the parent
     */
    @Override
    public int getHorizontalAlignment() {
        processProperty();
        if (horizontalAlignment == null) {
            return super.getHorizontalAlignment();
        }
        return horizontalAlignment.intValue();
    }

    @Override
    public String getVersionString() {
        if (this.customizationHoldingProperty != null) {
            Date lastModification = getConfigurationManager().getClientConfigurationProperties()
                    .getPropertyLastModification(customizationHoldingProperty);
            if (lastModification != null) {
                return String.valueOf(lastModification.getTime());
            }
        }
        return super.getVersionString();
    }

    /**
     * @return the custom value or the default inherited from the parent
     */
    @Override
    public int getVerticalAlignment() {
        processProperty();
        if (verticalAlignment == null) {
            return super.getVerticalAlignment();
        }
        return verticalAlignment.intValue();
    }

    private int normalizeAlignement(int alignment) {
        if (alignment < 0) {
            return -1;
        } else if (alignment == 0) {
            return 0;
        }
        return 1;
    }

    private void processProperty() {
        if (this.customizationHoldingProperty != null) {
            String newPropertyValue = getConfigurationManager().getClientConfigurationProperties()
                    .getProperty(customizationHoldingProperty);
            if (!StringUtils.equals(newPropertyValue, customizationPropertyValue)) {
                processPropertyValue(newPropertyValue);
            }
        }
    }

    private synchronized void processPropertyValue(String newPropertyValue) {
        if (!StringUtils.equals(newPropertyValue, customizationPropertyValue)) {
            // property changed -> parse it
            JsonNode parsedSettings = JsonHelper.readJsonTree(newPropertyValue);
            if (parsedSettings == null || !parsedSettings.isObject()) {
                // reset to defaults
                this.horizontalAlignment = null;
                this.verticalAlignment = null;
                this.backgroundColor = null;
            } else {
                JsonNode node = parsedSettings.get(SETTING_JSON_FIELD_VALIGN);
                if (node != null && node.isInt()) {
                    this.verticalAlignment = node.asInt();
                } else {
                    this.verticalAlignment = null;
                }
                node = parsedSettings.get(SETTING_JSON_FIELD_HALIGN);
                if (node != null && node.isInt()) {
                    this.horizontalAlignment = node.asInt();
                } else {
                    this.horizontalAlignment = null;
                }
                node = parsedSettings.get(SETTING_JSON_FIELD_COLOR);
                if (node != null) {
                    this.backgroundColor = ColorUtils.decodeRGBSilently(node.asText());
                } else {
                    this.backgroundColor = null;
                }
            }
            this.customizationPropertyValue = newPropertyValue;
        }
    }

    /**
     * Set the scale settings of this type descriptor.
     *
     * @param verticalAlignment
     *            a value which describes the vertical alignment of the image within the background
     *            when it is scaled. A value of less than 0 means to place the image at the top
     *            edge, a value of 0 leads to centering the image and a value greater than 0 means
     *            to place the image at the bottom edge. Null can be used to reset this setting the
     *            default value.
     * @param horizontalAlignment
     *            a value which describes the horizontal alignment of the image within the
     *            background when it is scaled. A value of less than 0 means to place the image at
     *            the top left, a value of 0 leads to centering the image and a value greater than 0
     *            means to place the image at the right edge. Null can be used to reset this setting
     *            the default value.
     * @param backgroundColor
     *            the background color to use to fill unoccupied pixels when scaling an image. Null
     *            can be used to reset this setting the default value.
     */
    public void setScaleSettings(Integer verticalAlignment, Integer horizontalAlignment,
            Color backgroundColor) {
        if (this.customizationHoldingProperty == null) {
            return;
        }
        String propertyValue;
        if (verticalAlignment == null && horizontalAlignment == null && backgroundColor == null) {
            propertyValue = null;
        } else {
            ObjectNode node = JsonHelper.getSharedObjectMapper().createObjectNode();
            if (verticalAlignment != null) {
                node.put(SETTING_JSON_FIELD_VALIGN,
                        normalizeAlignement(verticalAlignment.intValue()));
            }
            if (horizontalAlignment != null) {
                node.put(SETTING_JSON_FIELD_HALIGN,
                        normalizeAlignement(horizontalAlignment.intValue()));
            }
            if (backgroundColor != null) {
                node.put(SETTING_JSON_FIELD_COLOR,
                        ColorUtils.encodeRGBHexString(backgroundColor, true));
            }
            propertyValue = JsonHelper.writeJsonTreeAsString(node);
            if (propertyValue.isEmpty()) {
                propertyValue = null;
            }
        }
        if (!StringUtils.equals(customizationPropertyValue, propertyValue)) {
            getConfigurationManager().updateClientConfigurationProperty(
                    customizationHoldingProperty, propertyValue);
            ImageManager imageManager = ServiceLocator.findService(ImageManager.class);
            imageManager.imageChanged(getName(), null, null);
            imageManager.defaultImageChanged(getName(), null);
        }
    }

}
