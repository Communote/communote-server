package com.communote.server.api.core.note;

/**
 * Enumeration of possible modes to render a note.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public enum NoteRenderMode {
    /**
     * the note content should be rendered in a generic HTML format, i.e. the HTML should not
     * contain JavaScript or custom CSS classes. To provide additional meta data to clients, HTML
     * data attributes can be used.
     */
    HTML,
    /**
     * the note content should be rendered as plain text
     */
    PLAIN,
    /**
     * the notes are rendered by the web frontend and thus can contain custom JavaScript calls
     * available in the frontend
     */
    PORTAL,
    /**
     * The rendered note will be used to prepare an editor for creating a repost to this note. The
     * content is expected to be HTML but must not contain JavaScript. This mode should be used if
     * the editor is HTML capable.
     */
    REPOST,
    /**
     * The rendered note will be used to prepare an editor for creating a repost to this note. The
     * content is expected to be plain text.
     */
    REPOST_PLAIN;

    /**
     * Key for a mode option to inform the pre-processors that the plain text version of the content
     * should be beautified (e.g. indent LIs). This option should only be considered as set if the
     * value is "true".
     */
    public static final String PLAIN_MODE_OPTION_KEY_BEAUTIFY = "plainModeBeautify";
}
