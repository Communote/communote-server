package com.communote.server.core.blog.export.impl;

import java.io.IOException;
import java.util.Collection;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.ListItem;
import com.lowagie.text.Phrase;

/**
 * This factory is used to create necessary elements for the {@link RtfNoteWriter}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RtfElementFactory {

    private final static Font DEFAULT_FONT;

    static {
        DEFAULT_FONT = new Font();
        DEFAULT_FONT.setSize(10);
    }

    /**
     * This creates a new chunk with the given text.
     * 
     * @param text
     *            The text.
     * @return an {@link Element}.
     */
    public static Element createChunk(String text) {
        return createChunk(text, DEFAULT_FONT);
    }

    /**
     * This creates a new chunk with the given text and font.
     * 
     * @param text
     *            The text.
     * @param font
     *            The font. Can be null.
     * @return an {@link Element}.
     */
    public static Element createChunk(String text, Font font) {
        Chunk chunk = new Chunk(text);
        if (font != null) {
            chunk.setFont(font);
        }
        return chunk;
    }

    /**
     * 
     * @param imageAsBytes
     *            The image as byte array.
     * @param width
     *            Width.
     * @param height
     *            Height,
     * @return The image.
     * @throws BadElementException
     *             exception.
     * @throws IOException
     *             exception.
     */
    public static Element createImage(byte[] imageAsBytes, float width, float height)
            throws BadElementException, IOException {
        Image image = Image.getInstance(imageAsBytes);
        image.scaleToFit(width, height);
        return image;
    }

    /**
     * This method creates an {@link Image} depending on the path. And scales is to fit into width
     * and height.
     * 
     * @param path
     *            the path.
     * @param width
     *            Width.
     * @param height
     *            Height,
     * @return the image.
     * @throws BadElementException
     *             exception
     * @throws IOException
     *             exception
     */
    public static Element createImage(String path, float width, float height)
            throws BadElementException, IOException {
        Image image = Image.getInstance(path);
        image.scaleToFit(width, height);
        return image;
    }

    /**
     * This method returns a {@link IdentifiableEntityData} with the given text as specific einput.
     * 
     * @param text
     *            The text.
     * @return a {@link IdentifiableEntityData}.
     */
    public static Element createListItem(String text) {
        return createListItem(text, null);
    }

    /**
     * This method returns a {@link IdentifiableEntityData} with the given text as specific einput.
     * 
     * @param text
     *            The text.
     * @param font
     *            {@link Font} to apply on the element.
     * @return a {@link IdentifiableEntityData}.
     */

    public static Element createListItem(String text, Font font) {
        ListItem item = new ListItem(text);
        if (font != null) {
            item.setFont(font);
        }
        return item;
    }

    /**
     * Returns as simple phrase with all given children added.
     * 
     * @param children
     *            List of children.
     * @return a {@link Phrase}.
     */
    public static Element createPhrase(Collection<Object> children) {
        Phrase phrase = new Phrase();
        for (Object child : children) {
            phrase.add(child);
        }
        return phrase;
    }

    /**
     * Private constructor to avoid instances of this utility class.
     */
    private RtfElementFactory() {
        // Do nothing.
    }
}
