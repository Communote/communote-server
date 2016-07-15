package com.communote.server.core.blog;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.communote.common.util.HTMLHelper;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;

/**
 * Allows shortening the HTML content of a note to a given number of lines. The processNoteContent
 * is not thread safe.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteShortener {

    private static final String READ_MORE_LINK_TAG = "rml";
    private static final Pattern READ_MORE_LINK_TAG_PATTERN = Pattern.compile("<"
            + READ_MORE_LINK_TAG + "/>");
    private static final int DEFAULT_MAX_SHOWN_LINES = 4;
    private static final int DEFAULT_MAX_CHARS_ON_LINE = 75;
    private static final int DEFAULT_MAX_CHARS_ON_LAST_LINE = 47;
    private static final int DEFAULT_INDENT_CHARS_WIDTH = 6;
    private static final String BR_TAG = "br";
    // tags that cause a line break but not when they are the first child of an element node
    private static final Set<String> TAGS_CREATING_LINE = new HashSet<String>(Arrays.asList("p",
            "ol", "ul", "li", "div"));
    private static final Set<String> TAGS_CREATING_INDENT = new HashSet<String>(Arrays.asList("ul",
            "ol", "blockquote"));
    // only add nodes which should be container of the RML element
    private static final Set<String> TAGS_CONTAINING_TEXT = new HashSet<String>(Arrays.asList("li",
            "p", "blockquote", "div", "span", "root"));

    private static final Logger LOGGER = LoggerFactory.getLogger(NoteShortener.class);

    /**
     * Replaces the read more placeholder with a replacement (e.g. some link).
     *
     * @param content
     *            the content where the placeholder should be replaced
     * @param replacement
     *            the replacement
     * @return the modified content
     */
    public static String replaceReadMorePlaceHolder(String content, String replacement) {
        Matcher m = READ_MORE_LINK_TAG_PATTERN.matcher(content);
        return m.replaceFirst(replacement);
    }

    // whitespaces at which browsers break (cf.
    // http://www.fileformat.info/info/unicode/category/Zs/list.htm)
    private final Pattern whiteSpacePattern = Pattern
            .compile("[\\s\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2008\u2009\u200A\u3000]");

    private int curLine;
    private int charsOnLine;
    private Node lastProcessedNode;

    private int indentLevel;
    private final int maxShownLines;
    private final int maxCharsOnLine;
    private final int maxCharsOnLastLine;

    private final int indentCharsWidth;

    private final Pattern rootPattern;

    /**
     * Creates a NoteShortener with default settings.
     */
    public NoteShortener() {
        this(DEFAULT_MAX_SHOWN_LINES, DEFAULT_MAX_CHARS_ON_LINE, DEFAULT_MAX_CHARS_ON_LAST_LINE,
                DEFAULT_INDENT_CHARS_WIDTH);
    }

    /**
     * Creates a NoteShortener with custom settings.
     *
     * @param maxShownLines
     *            the maximum number of lines to be shown. All lines beyond this line will be cut.
     * @param maxCharsOnLine
     *            the maximum number of characters that fit on a line before a line break is
     *            necessary.
     * @param maxCharsOnLastLine
     *            the maximum number of characters to be allowed on the last shown line if cutting
     *            is necessary. A line that is the maxShownLine can have maxCharsOnLine characters.
     * @param indentCharsWidth
     *            the number of characters that are consumed by an indent (e.g. caused by an ul tag)
     */
    public NoteShortener(int maxShownLines, int maxCharsOnLine, int maxCharsOnLastLine,
            int indentCharsWidth) {
        this.maxShownLines = maxShownLines;
        this.maxCharsOnLine = maxCharsOnLine;
        this.maxCharsOnLastLine = maxCharsOnLastLine;
        this.indentCharsWidth = indentCharsWidth;
        rootPattern = Pattern.compile("<root>(.*)</root>", Pattern.DOTALL);
    }

    /**
     * Returns the data surrounded by the root tag.
     *
     * @param content
     *            the content to process
     * @return the data found within root tag or the empty string in case there is no root tag
     */
    private String extraxtContentOfBodyTag(String content) {
        Matcher matcher = rootPattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Find the character position where the browser would break a line and create new line.
     *
     * @param content
     *            the content
     * @param endPos
     *            character position in content which the split position must not exceed
     * @return the
     */
    private int findSplitPos(String content, int endPos) {
        int splitPos = -1;
        char charAtLimit = content.charAt(endPos);
        // note: browsers handle wrapping differently: IE splits after '-' FF only
        // if there are five chars before '-' which are not space; FF also evaluates '\' and breaks
        // at at last '\' if there is no whitespace between last 2 '\' (same for '/')
        // browsers do not break following whitespace chars U+205F U+180E U+1680
        if ((Character.isWhitespace(charAtLimit) && charAtLimit != '\u1680'
                && charAtLimit != '\u180E' && charAtLimit != '\u205F')
                || charAtLimit == '-') {
            splitPos = endPos;
        } else {
            // find previous word boundary
            Matcher m = this.whiteSpacePattern.matcher(content);
            while (m.find()) {
                if (m.start() < endPos) {
                    splitPos = m.start();
                } else {
                    break;
                }
            }
            // prefer hyphen if closer than space
            int lastHyphenPos = content.lastIndexOf("-", endPos - 1);
            if (splitPos < 0 || splitPos < lastHyphenPos) {
                splitPos = lastHyphenPos;
            }
        }
        return splitPos;
    }

    /**
     * Get the element node that is one of the notes that can contain text as defined by
     * TAGS_CONTAINING_TEXT. This method starts with the provided node and continues with its
     * parents until a matching node is found.
     *
     * @param node
     *            the node to start from
     * @return the found element node or null
     */
    private Node getTextContainingNode(Node node) {
        while (node != null) {
            if (node instanceof Element) {
                Element e = (Element) node;
                String localName = e.getLocalName() == null ? e.getTagName() : e.getLocalName();
                if (TAGS_CONTAINING_TEXT.contains(localName)) {
                    return node;
                }
            }
            node = node.getParentNode();
        }
        return null;
    }

    /**
     * Processes all child nodes of a node.
     *
     * @param parent
     *            the node whose children will be processed
     * @param parentTagName
     *            the tag name of the node whose children will be processed
     * @return true if the limit was not reached, false otherwise
     * @throws DOMException
     *             if the node cannot be modified or found
     */
    private boolean processChildNodes(Node parent, String parentTagName) throws DOMException {
        // must process childs by calling nextSibling because processNode seems to change the
        // nodeList of getChildNodes
        Node child = parent.getFirstChild();
        boolean goOn = true;
        boolean firstChild = true;
        while (child != null) {
            Node nextChild = child.getNextSibling();
            if (goOn) {
                if (!processNode(child, parentTagName, firstChild)) {
                    goOn = false;
                }
            } else {
                parent.removeChild(child);
            }
            child = nextChild;
            firstChild = false;
        }
        return goOn;
    }

    /**
     * Processes a single node and all its children recursively.
     *
     * @param node
     *            the node to process
     * @param parentTagName
     *            the tagName of the parent of the node to process
     * @param firstChild
     *            whether the node is the first child node
     * @return true if the limit was not reached, false otherwise
     * @throws DOMException
     *             if the node cannot be modified or found
     */
    private boolean processNode(Node node, String parentTagName, boolean firstChild)
            throws DOMException {
        boolean goOn = true;
        if (node instanceof Text) {
            lastProcessedNode = node;
            goOn = processTextNode((Text) node);
        } else if (node instanceof Element) {
            Element e = (Element) node;
            boolean decrementIndent = false;
            String localName = e.getLocalName() == null ? e.getTagName() : e.getLocalName();
            boolean isBr = BR_TAG.equals(localName);
            if (isBr || TAGS_CREATING_LINE.contains(localName)) {
                if (isBr || !firstChild) {
                    // the first child does not create a new line (since we start with line 1)
                    curLine++;
                }
                if (curLine > maxShownLines) {
                    node.getParentNode().removeChild(node);
                    return false;
                }
                charsOnLine = 0;
            }
            lastProcessedNode = node;
            if (TAGS_CREATING_INDENT.contains(localName)) {
                indentLevel++;
                // TODO check for MAX_CHARS??
                decrementIndent = true;
            }
            goOn = processChildNodes(node, localName);
            if (decrementIndent) {
                indentLevel--;
            }
        } else {
            // handle entity nodes??? - guess no
        }
        return goOn;
    }

    /**
     * Returns the shortened content or null if there is no need to shorten it. If the content was
     * shortened a directly closed read-more-link placeholder (&lt;rml/&gt;) will be contained at
     * the cut position.
     *
     * @param content
     *            the content to shorten
     * @return the shortened content or null if there is no need to shorten it.
     * @throws NoteStoringPreProcessorException
     *             in case content processing fails
     */
    public String processNoteContent(String content) throws NoteStoringPreProcessorException {
        charsOnLine = 0;
        curLine = 1;
        indentLevel = 0;
        lastProcessedNode = null;
        try {
            content = "<root>" + content + "</root>";
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource src = new InputSource(new StringReader(content));
            Document document = builder.parse(src);
            Node rootNode = document.getFirstChild();
            if (!processChildNodes(rootNode, "root")) {
                // add the placeholder
                Element e = document.createElement(READ_MORE_LINK_TAG);
                Node textContainingNode = getTextContainingNode(lastProcessedNode);
                Node placeHolderContainer = textContainingNode != null ? textContainingNode
                        : rootNode;
                placeHolderContainer.appendChild(e);
                // serialize
                Transformer t = TransformerFactory.newInstance().newTransformer();
                t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                StringWriter sw = new StringWriter();
                t.transform(new DOMSource(rootNode), new StreamResult(sw));

                String shortenedContent = sw.toString();
                // shortenedContent = addPlaceholder(shortenedContent);
                // do some HTML cleanup
                shortenedContent = HTMLHelper.convertXmlSerializedHtmlToLegalHtml(shortenedContent);
                // strip root tag
                return extraxtContentOfBodyTag(shortenedContent);
            } else {
                // content is short enough so no need for shortening
                return null;
            }
        } catch (ParserConfigurationException e) {
            throw new NoteStoringPreProcessorException(e.getMessage());
        } catch (SAXException e) {
            throw new NoteStoringPreProcessorException(e.getMessage());
        } catch (TransformerConfigurationException e) {
            throw new NoteStoringPreProcessorException(e.getMessage());
        } catch (TransformerFactoryConfigurationError e) {
            throw new NoteStoringPreProcessorException(e.getMessage());
        } catch (TransformerException e) {
            throw new NoteStoringPreProcessorException(e.getMessage());
        } catch (DOMException e) {
            throw new NoteStoringPreProcessorException(e.getMessage());
        } catch (IOException e) {
            throw new NoteStoringPreProcessorException(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected error processing Content: " + content, e);
            throw e;
        }
    }

    /**
     * Processes a text node and shortens its conent if necessary
     *
     * @param node
     *            the node to process
     * @return true if the limit was not reached, false otherwise
     * @throws DOMException
     *             if the node cannot be modified or found
     */
    private boolean processTextNode(Text node) throws DOMException {
        String content = node.getWholeText();
        int splitPos = processTextNodeContent(content);
        if (splitPos >= 0) {
            String newContent = content.substring(0, splitPos);
            node.replaceWholeText(newContent);
            return false;
        }
        return true;
    }

    /**
     * Processes the content of a text node.
     *
     * @param content
     *            the content
     * @return the position where the content of the text should be split. If the return value is
     *         smaller than 0 no split was necessary.
     */
    private int processTextNodeContent(String content) {
        int alreadyOccupiedCharsOnLine = charsOnLine + indentLevel * indentCharsWidth;
        int availableCharsOnLine = maxCharsOnLine - alreadyOccupiedCharsOnLine;
        if (availableCharsOnLine < 0) {
            availableCharsOnLine = 0;
        }
        // browsers only render one space even if there is a sequence of them
        int viewLength = content.replaceAll(" +", " ").length();
        if (availableCharsOnLine >= viewLength) {
            charsOnLine += viewLength;
            return -1;
        } else {
            // find split position
            int splitLimit;
            if (curLine == maxShownLines) {
                splitLimit = maxCharsOnLastLine - alreadyOccupiedCharsOnLine;
                if (splitLimit < 0) {
                    splitLimit = 0;
                }
            } else {
                splitLimit = availableCharsOnLine;
            }
            int splitPos = findSplitPos(content, splitLimit);
            if (splitPos < 0) {
                // no split found
                if (curLine == maxShownLines) {
                    // last line will be empty
                    splitPos = 0;
                } else {
                    // split in word
                    splitPos = splitLimit;
                }
            }
            if (curLine != maxShownLines) {
                curLine++;
                charsOnLine = 0;
                // get remaining text without space
                String remainingContent = splitPos == content.length() - 1 ? "" : content
                        .substring(splitPos + 1);
                int offset = splitPos + 1;
                splitPos = processTextNodeContent(remainingContent);
                if (splitPos >= 0) {
                    splitPos += offset;
                }
            }
            return splitPos;
        }
    }

}
