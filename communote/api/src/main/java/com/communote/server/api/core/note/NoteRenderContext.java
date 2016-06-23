package com.communote.server.api.core.note;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Describes the context in which a note is rendered.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class NoteRenderContext {

    private final NoteRenderMode mode;
    private Map<String, String> modeOptions;
    private HttpServletRequest request;

    private final Locale locale;

    /**
     * Creates a new render context.
     *
     * @param mode
     *            the mode for rendering a note
     * @param locale
     *            the locale of the user for whom the notes are rendered. Must not be null.
     */
    public NoteRenderContext(NoteRenderMode mode, Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException("The locale must not be null.");
        }
        this.locale = locale;
        this.mode = mode;
        modeOptions = new HashMap<String, String>();
    }

    /**
     * @return the locale of the user for whom the notes are rendered
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Return the mode for rendering a note.
     *
     * @return the mode
     */
    public NoteRenderMode getMode() {
        return mode;
    }

    /**
     * Return options which should apply for the render mode of this context. The options can
     * contain settings which are only relevant for a specific note rendering pre-processor, for
     * instance to activate some custom behavior.
     *
     * @return key value pairs of settings which should apply for the render mode of this context.
     *         Never null.
     */
    public Map<String, String> getModeOptions() {
        return this.modeOptions;
    }

    /**
     * @return the request that triggered the rendering. This can be null if no servlet request
     *         caused the rendering (e.g. when sending an e-mail).
     */
    public HttpServletRequest getRequest() {
        return this.request;
    }

    /**
     * Sets options which should apply for the render mode of this context. The options can contain
     * settings which are only relevant for a specific note rendering pre-processor, for instance to
     * activate some custom behavior.
     *
     * @param options
     *            the settings to set
     */
    public void setModeOptions(Map<String, String> options) {
        if (options != null) {
            this.modeOptions = options;
        } else {
            this.modeOptions = new HashMap<String, String>();
        }
    }

    /**
     * Set the request that triggered the render action.
     *
     * @param request
     *            the request
     */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

}
