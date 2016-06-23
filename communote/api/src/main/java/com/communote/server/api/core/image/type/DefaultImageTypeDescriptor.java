package com.communote.server.api.core.image.type;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.communote.common.image.ImageFormatType;
import com.communote.common.util.ParameterHelper;
import com.communote.server.api.core.image.ImageTypeDescriptor;

/**
 * Image type descriptor which returns defaults for most attributes. The identifier is extracted by
 * looking for an "id" parameter.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public abstract class DefaultImageTypeDescriptor implements ImageTypeDescriptor {

    /** Default background color, which is white. */
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    /** Default supported mime types which are JPEG and PNG */
    public static final List<ImageFormatType> DEFAULT_MIMETYPES;
    static {
        ArrayList<ImageFormatType> mimeTypes = new ArrayList<>();
        mimeTypes.add(ImageFormatType.jpeg);
        mimeTypes.add(ImageFormatType.png);
        DEFAULT_MIMETYPES = Collections.unmodifiableList(mimeTypes);
    }

    /**
     * Return the value of a parameter that has the key "id" and a string value.
     *
     * @param parameters
     *            the parameters to parse
     * @return the value of the parameter or null if not found
     */
    @Override
    public String extractImageIdentifier(Map<String, ? extends Object> parameters) {
        return ParameterHelper.getParameterAsString(parameters, "id");
    }

    /**
     * @return white
     */
    @Override
    public Color getBackgroundColor() {
        return DEFAULT_BACKGROUND_COLOR;
    }

    /**
     * @return 0
     */
    @Override
    public int getHorizontalAlignment() {
        return 0;
    }

    /**
     * @return {@link #DEFAULT_MIMETYPES}
     */
    @Override
    public List<ImageFormatType> getValidMimeTypes() {
        return DEFAULT_MIMETYPES;
    }

    @Override
    public String getVersionString() {
        // could return the build timestamp but since the scaling values are not modified we can
        // just return a static string version
        return "2";
    }

    /**
     * @return 0
     */
    @Override
    public int getVerticalAlignment() {
        return 0;
    }

    /**
     * @return true
     */
    @Override
    public boolean isDrawBackground() {
        return true;
    }

    /**
     * @return true
     */
    @Override
    public boolean isPreserveAspectRation() {
        return true;
    }

}
