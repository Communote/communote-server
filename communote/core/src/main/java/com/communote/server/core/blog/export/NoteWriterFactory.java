package com.communote.server.core.blog.export;

import java.util.HashMap;
import java.util.Map;

import com.communote.server.core.blog.export.NoteWriter;
import com.communote.server.core.blog.export.impl.RssNoteWriter;
import com.communote.server.core.blog.export.impl.RtfNoteWriter;
import com.communote.server.persistence.blog.ExportFormat;


/**
 * Factory for getting exporters dependent on the format.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class NoteWriterFactory {

    private static Map<String, NoteWriter> WRITERS = new HashMap<String, NoteWriter>();

    /** Maximum of posts to export */
    public static final int EXPORT_DEFAULT_MAX_POSTS = 100;

    static {
        WRITERS.put(ExportFormat.RTF.getValue().toLowerCase(), new RtfNoteWriter());
        RssNoteWriter rssNoteWriter = new RssNoteWriter();
        WRITERS.put(ExportFormat.XML.getValue().toLowerCase(), rssNoteWriter);
        WRITERS.put("rss", new RssNoteWriter());
    }

    /**
     * Adds a new writer or replaces an existing writer for the given export format.
     * 
     * @param exportFormat
     *            The export format.
     * @param writer
     *            The writer.
     */
    public static void addWriter(String exportFormat, NoteWriter writer) {
        WRITERS.put(exportFormat, writer);
    }

    /**
     * 
     * @param exportFormat
     *            The export format.
     * @return The writer for the given format or null.
     */
    public static NoteWriter getExporter(ExportFormat exportFormat) {
        return getExporter(exportFormat.getValue().toLowerCase());
    }

    /**
     * 
     * @param exportFormat
     *            The export format.
     * @return The writer for the given format or null.
     */
    public static NoteWriter getExporter(String exportFormat) {
        return WRITERS.get(exportFormat);
    }

    /**
     * Removes the writer for the given export format.
     * 
     * @param exportFormat
     *            The export format.
     */
    public static void removeWriter(String exportFormat) {
        WRITERS.remove(exportFormat);
    }

    /**
     * Private constructor to avoid instances of a utility class.
     */
    private NoteWriterFactory() {
        // Do nothing
    }
}
