package com.communote.server.core.blog.notes.processors;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.communote.common.util.HTMLHelper;
import com.communote.common.util.UrlHelper;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringEditableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;

/**
 * Processor, which removes any markup from the note content that is not supported/allowed.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RemoveUnsupportedMarkupNotePreProcessor implements
        NoteStoringEditableContentPreProcessor {
    private final static Pattern STANDALONE_TAG = Pattern.compile("<[-\\w:]+\\s*/>");
    private final static Pattern OPENING_TAG = Pattern.compile("<([-\\w:]+)[\\s=\\w\"';]*>");
    private final static Pattern BODY_PATTERN = Pattern.compile("<body[^>]*>(.*)</body>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final String MINIMAL_ENTITY_STRING = "<!DOCTYPE html [<!ENTITY nbsp   \"&#160;\">]>";
    private static final String VERSION_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    private final static Pattern XML_VERSION_PATTERN = Pattern.compile(
            "<\\?xml version=[^>]*>[\\n\\r]*", Pattern.CASE_INSENSITIVE);
    private final static Pattern DOCTYPE_PATTERN = Pattern.compile("<!DOCTYPE [^>]*>[\\n\\r]*",
            Pattern.CASE_INSENSITIVE);

    private static final HashSet<String> TAGS_TO_REMOVE = new HashSet<String>(Arrays.asList(
            "script", "head", "style", "link", "meta", "title"));

    private static final HashSet<String> SUPPORTED_TAGS = new HashSet<String>(Arrays.asList("div",
            "strong", "em", "u", "b", "i", "ul", "ol", "li", "blockquote", "a", "p", "br"));

    private static final HashSet<String> GLOBAL_ALLOWED_ATTRIBUTES = new HashSet<String>(Arrays
            .asList("name", "title"));
    private final HashMap<String, HashMap<String, ArrayList<Pattern>>> allowedAttributesPerTag;

    /**
     * Constructor.
     */
    public RemoveUnsupportedMarkupNotePreProcessor() {
        allowedAttributesPerTag = new HashMap<String, HashMap<String, ArrayList<Pattern>>>();
        defineAllowedAttributesPerTag();
    }

    /**
     * Adds elements required for XHTML conformity to the content. This includes version
     * information, the doctype string, html and body tags.
     * 
     * @param content
     *            the content to process
     * @return the processed content
     */
    private String addRequiredElements(String content) {
        StringBuilder newContent = new StringBuilder();
        String[] parts = XML_VERSION_PATTERN.split(content);
        // only take last part
        content = parts[parts.length - 1];
        parts = DOCTYPE_PATTERN.split(content);
        content = parts[parts.length - 1];
        newContent.append(VERSION_STRING);
        // removing the doctype makes parsing and normalization a lot faster
        // newContent.append(DOCTYPE_STRING);
        // only use lat1 entities for parsing
        // newContent.append(ENTITY_STRING);
        newContent.append(MINIMAL_ENTITY_STRING);

        // in case of plain text add html tags
        if (!content.contains("<html") && !content.contains("<HTML")) {
            boolean closeBodyTag = false;
            newContent.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
            if (!content.contains("<body>") && !content.contains("<BODY>")) {
                int index = content.indexOf("</head");
                if (index < 0) {
                    index = content.indexOf("</HEAD");
                }
                if (index < 0) {
                    newContent.append("<body>");
                    closeBodyTag = true;
                } else {
                    newContent.append(content.substring(0, index + 6));
                    newContent.append("<body>");
                    closeBodyTag = true;
                    newContent.append(content.substring(index + 6, content.length()));
                }
            }
            newContent.append(content);
            if (closeBodyTag) {
                newContent.append("</body>");
            }
            newContent.append("</html>");
        } else {
            newContent.append(content);
        }
        String newContentString = newContent.toString();
        return HTMLHelper.closeBrTags(newContentString);
    }

    /**
     * Creates the path part containing the segment part if existing not representing a jsession ID
     * 
     * @param pathPart
     *            the path part of the URL
     * @param segment
     *            the segment part starting with a semicolon
     * @return the complete path part
     */
    private String buildPathPart(String pathPart, String segment) {
        if (pathPart != null) {
            // for security reasons ignore group 4 if it contains a session ID
            if (segment != null && !segment.startsWith(";jsessionid=")) {
                // encode the pathPart as the recognition supports non ASCII letters which does not
                // confirm to RFC
                pathPart = UrlHelper.urlEncodeUrlPath(pathPart) + segment;
            }
        }
        return pathPart;
    }

    /**
     * Defines for certain tags which attributes are allowed. This information is stored in the
     * allowedAttributesPerTag map.
     */
    private void defineAllowedAttributesPerTag() {
        // define allowed attributes for 'a'
        HashMap<String, ArrayList<Pattern>> attributeValueMappings = new HashMap<String, ArrayList<Pattern>>();
        // for href all values are possible
        attributeValueMappings.put("href", null);
        // TODO restrict to _blank ?
        attributeValueMappings.put("target", null);
        allowedAttributesPerTag.put("a", attributeValueMappings);
        // define allowed attributes for 'span'
        attributeValueMappings = new HashMap<String, ArrayList<Pattern>>();
        ArrayList<Pattern> valueDefs = new ArrayList<Pattern>(1);
        // don't allow numeric size definitions
        valueDefs.add(Pattern.compile("font-size:\\s?[a-z\\-]+\\s*;?"));
        attributeValueMappings.put("style", valueDefs);
        allowedAttributesPerTag.put("span", attributeValueMappings);
    }

    /**
     * Matches always group 0 and returns currentValue if allowedValues is null
     * 
     * @param currentValue
     *            the current value
     * @param allowedValues
     *            the allowed patterns
     * @return returns the allowed attribute values or the currentValue if the allowedValues is null
     */
    private String extractSupportedAttributeValues(String currentValue,
            ArrayList<Pattern> allowedValues) {
        if (allowedValues == null) {
            return currentValue;
        }
        StringBuilder newValue = new StringBuilder();
        for (Pattern p : allowedValues) {
            Matcher matcher = p.matcher(currentValue);
            while (matcher.find()) {
                newValue.append(matcher.group(0));
            }
        }
        return newValue.toString();
    }

    /**
     * Returns the data surrounded by the body tag.
     * 
     * @param content
     *            the content to process
     * @return the data found within body tag or the empty string in case there is no body tag
     */
    private String extraxtContentOfBodyTag(String content) {
        Matcher matcher = BODY_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Returns the body node of the XHTML document.
     * 
     * @param document
     *            the XHTML document
     * @return the body node or null if it was not found
     * @throws NoteStoringPreProcessorException
     *             in case the body tag cannot be found (either in upper or lower case)
     */
    private Node getBodyNode(Document document) throws NoteStoringPreProcessorException {
        NodeList nodes = document.getElementsByTagName("body");
        if (nodes.getLength() == 0) {
            nodes = document.getElementsByTagName("BODY");
            if (nodes.getLength() == 0) {
                throw new NoteStoringPreProcessorException("No body element found.");
            }
        }
        return nodes.item(0);
    }

    /**
     * Tries to guess whether content is HTML (for opening and matching end tag). If it is not HTML
     * all HTML special characters in content will be escaped.
     * 
     * @param content
     *            the content to evaluate
     * @param contentType
     *            the content type
     * @return the escaped content or the unchanged content if it was already HTML
     */
    private String getHTMLEscapedContent(String content, NoteContentType contentType) {
        boolean encode = true;

        if (contentType == null || contentType == NoteContentType.UNKNOWN) {
            // try to find out whether the text is HTML (and thus escaped) or
            // plain text
            Matcher matcher = STANDALONE_TAG.matcher(content);
            if (matcher.find()) {
                // is HTML
                encode = false;
            } else {
                matcher = OPENING_TAG.matcher(content);
                while (matcher.find()) {
                    String ot = matcher.group(1);
                    int endOfMatch = matcher.end(0);
                    // search for end tag
                    if (content.indexOf("/" + ot + ">", endOfMatch) > 0) {
                        encode = false;
                        break;
                    }
                }
            }
        } else if (contentType == NoteContentType.HTML) {
            encode = false;
        }
        if (encode) {
            // escape and preserve linebreaks
            return HTMLHelper.plaintextToHTML(content);
        }
        return content;
    }

    @Override
    public int getOrder() {
        // note: not that important what we return here because this pre-processor will be added to
        // a list that is not modifiable
        return 0;
    }

    /**
     * Tests whether a node or one of its parent element nodes has the given tag name.
     * 
     * @param startNode
     *            the node to start with
     * @param tagName
     *            the tag name to match
     * @return true if the tag name of the startNode or one of the startNode's parents is tagName
     */
    private boolean hasMatchingParentElement(Node startNode, String tagName) {
        Node node = startNode;
        while (node != null && node instanceof Element) {
            Element e = (Element) node;
            String localName = e.getLocalName() == null ? e.getTagName() : e.getLocalName();
            if (StringUtils.equalsIgnoreCase(localName, tagName)) {
                return true;
            }
            node = e.getParentNode();
        }
        return false;
    }

    /**
     * Inserts an anchor into the DOM as left sibling of a reference node.
     * 
     * @param protocolHostPart
     *            the protocol and host part (including port) of the URL the anchor should link to
     * @param pathPart
     *            the path part of the URL; can be null
     * @param queryPart
     *            the query part of the URL; can be null
     * @param fragmentIdentifier
     *            the fragment identifier of the URL; can be null
     * @param refNode
     *            the reference node
     * @param leftText
     *            the text to be placed left of anchor; can be an empty string
     */
    private void insertAnchor(String protocolHostPart, String pathPart, String queryPart,
            String fragmentIdentifier, Node refNode, String leftText) {
        Document document = refNode.getOwnerDocument();
        Node parent = refNode.getParentNode();
        if (leftText.length() > 0) {
            Text textNode = document.createTextNode(leftText);
            parent.insertBefore(textNode, refNode);
        }
        Element anchor = document.createElement("a");
        StringBuilder urlFull = new StringBuilder();
        if (protocolHostPart.charAt(0) == 'w' || protocolHostPart.charAt(0) == 'W') {
            urlFull.append("http://");
        }
        urlFull.append(protocolHostPart);
        if (pathPart != null) {
            urlFull.append(pathPart);
        }
        if (queryPart != null) {
            urlFull.append(queryPart);
        }
        if (fragmentIdentifier != null) {
            urlFull.append(fragmentIdentifier);
        }
        anchor.setAttribute("href", urlFull.toString());
        anchor.setAttribute("title", urlFull.toString());
        anchor.setAttribute("target", "_blank");
        // shorten
        anchor.setTextContent(UrlHelper.shortenUrl(protocolHostPart, pathPart, queryPart,
                fragmentIdentifier));
        parent.insertBefore(anchor, refNode);
    }

    @Override
    public boolean isProcessAutosave() {
        // no need to cleanup when just doing an autosave
        return false;
    }

    /**
     * Tests whether a tag is supported.
     * 
     * @param node
     *            the node to test
     * @param localName
     *            the local name of the tag/node
     * @return true if the tag is supported, false otherwise
     */
    private boolean isTagSupported(Element node, String localName) {
        if (!SUPPORTED_TAGS.contains(localName)) {
            return false;
        }
        boolean supported = true;

        // special treatment of anchor to block javascript and data in href
        if (localName.equals("a")) {
            String hrefValue = node.getAttribute("href").toLowerCase();
            if (StringUtils.isBlank(hrefValue) || hrefValue.startsWith("javascript")
                    || hrefValue.startsWith("data")) {
                supported = false;
            }
        }
        return supported;
    }

    /**
     * Removes unsupported markup from the post content found in the storing transfer object. All
     * text nodes are preserved.
     * 
     * @param note
     *            the transfer object to process
     * @return The altered note.
     * @throws NoteStoringPreProcessorException
     *             Exception.
     */
    @Override
    public NoteStoringTO process(NoteStoringTO note) throws NoteStoringPreProcessorException {
        // escape html special characters before extracting
        String escapedContent = getHTMLEscapedContent(note.getContent(), note.getContentType());
        note.setContentType(NoteContentType.HTML);
        note.setContent(escapedContent);

        escapedContent = addRequiredElements(escapedContent);
        // remove the head because it's not of interest
        escapedContent = HTMLHelper.removeHeadElement(escapedContent);
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // remove superfluous linebreak from string end to avoid premature EOF
            // exceptions
            // content = removeTrailingLinebreak(content);
            // add entity resolver which checks class path for file with entity
            // definitions
            // builder.setEntityResolver(new KenmeiClasspathEntityResolver());
            InputSource src = new InputSource(new StringReader(escapedContent));
            Document document = builder.parse(src);
            DOMConfiguration domConfig = document.getDomConfig();
            // transform cdata to text
            domConfig.setParameter("cdata-sections", false);
            // remove comments
            domConfig.setParameter("comments", false);
            document.normalizeDocument();

            // start processing with body node
            Node bodyNode = getBodyNode(document);
            Node child = bodyNode.getFirstChild();
            while (child != null) {
                child = processDomNode(child);
            }
            // write content of body element to postContent
            // Node bodyNode = document.getElementsByTagName("body").item(0);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(bodyNode), new StreamResult(stringWriter));
            String finalContent = stringWriter.toString();
            // some cleanup
            finalContent = HTMLHelper.convertXmlSerializedHtmlToLegalHtml(finalContent);
            note.setContent(extraxtContentOfBodyTag(finalContent));
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
        } catch (IOException e) {
            throw new NoteStoringPreProcessorException(e.getMessage());
        }
        return note;
    }

    /**
     * Processes a DOM node and all it's children recursively. The processing covers removing the
     * node if it's tag is in the TAGS_TO_REMOVE set, removing unsupported attributes for nodes
     * whose tag is in the SUPPORTED_TAGS set and dropping all nodes whose tag is not in the
     * SUPPORTED_TAGS set but keeping the children of these nodes by moving them to the right
     * position in their parent layer.
     * 
     * @param node
     *            the DOM element node to process
     * @return the next sibling of the supplied node or null if there is none
     */
    private Node processDomNode(Node node) {
        if (node == null) {
            return null;
        }
        Node nextNode = node.getNextSibling();
        if (node instanceof Text) {
            transformAndShortenUrl((Text) node);
            return nextNode;
        }
        if (node instanceof Element) {
            Node newNextNode = processElementNode((Element) node);
            // if previous call returned a node the children of the passed node were moved one layer
            // up and the passed node was removed. The returned node was the first child of the
            // deleted node and became the next sibling we must process next.
            if (newNextNode != null) {
                nextNode = newNextNode;
            }
        } else {
            // TODO correct to remove all other nodes?
            node.getParentNode().removeChild(node);
        }
        return nextNode;
    }

    /**
     * Processes an element node. This covers removing the node if it's tag is in the TAGS_TO_REMOVE
     * set, removing unsupported attributes for nodes whose tag is in the SUPPORTED_TAGS set and
     * dropping all nodes whose tag is not in the SUPPORTED_TAGS set but keeping the children of
     * these nodes by moving them to the right position in their parent layer.
     * 
     * @param element
     *            the element node to process
     * @return the first child of this node if this node has children and has an unsupported tag,
     *         otherwise null
     */
    private Node processElementNode(Element element) {
        Node nextNode = null;
        String localName = element.getLocalName() == null ? element.getTagName() : element
                .getLocalName();
        // since it is HTML case is not important
        localName = localName.toLowerCase();
        if (TAGS_TO_REMOVE.contains(localName)) {
            element.getParentNode().removeChild(element);
            return null;
        }
        if (isTagSupported(element, localName)) {
            Node child = element.getFirstChild();
            if (child == null) {
                if (HTMLHelper.NODES_TO_REMOVE_WHEN_EMPTY.contains(localName)) {
                    element.getParentNode().removeChild(element);
                }
            } else {
                removeUnsupportedAttributes(element, localName);
                // process all direct children
                while (child != null) {
                    child = processDomNode(child);
                }
            }
        } else {
            // move all children to the layer of this node and remove this
            // node
            if (element.hasChildNodes()) {
                nextNode = element.getFirstChild();
            }
            processUnsupportedDomNode(element);
        }
        return nextNode;
    }

    /**
     * Moves all children of the unsupported node one layer upwards in DOM hierarchy and removes the
     * unsupported node afterwards. The children will be inserted before the unsupported node in
     * correct order.
     * 
     * @param unsupportedNode
     *            the unsupported node
     */
    private void processUnsupportedDomNode(Node unsupportedNode) {
        Node parent = unsupportedNode.getParentNode();
        Node refNode = unsupportedNode;
        // start with last node because there is only an insertBefore method
        Node child = unsupportedNode.getLastChild();
        while (child != null) {
            Node nextChild = child.getPreviousSibling();
            // insert before reference node (removes child automatically before
            // insert)
            parent.insertBefore(child, refNode);
            refNode = child;
            child = nextChild;
        }
        // remove the unsupported node
        parent.removeChild(unsupportedNode);
    }

    /**
     * Tests the element node for unsupported attributes and attribute values and removes those from
     * the node.
     * 
     * @param e
     *            the element node to check
     * @param localName
     *            the local name of the element node for convenience
     */
    private void removeUnsupportedAttributes(Element e, String localName) {
        NamedNodeMap m = e.getAttributes();
        for (int i = 0; i < m.getLength(); i++) {
            Attr attribute = (Attr) m.item(i);
            boolean removeAttribute = false;
            if (!GLOBAL_ALLOWED_ATTRIBUTES.contains(attribute.getName())) {
                removeAttribute = true;
                HashMap<String, ArrayList<Pattern>> allowedAttributes = allowedAttributesPerTag
                        .get(localName);
                // check for tag specific attribute allowance
                if (allowedAttributes != null) {
                    if (allowedAttributes.containsKey(attribute.getName())) {
                        ArrayList<Pattern> allowedValues = allowedAttributes.get(attribute
                                .getName());
                        String newValue = extractSupportedAttributeValues(attribute.getValue(),
                                allowedValues);
                        if (newValue.length() != 0) {
                            attribute.setValue(newValue);
                            removeAttribute = false;
                        }
                    }
                }
            }
            if (removeAttribute) {
                e.removeAttributeNode(attribute);
            }
        }
    }

    /**
     * Tests whether the text node contains a URL. If the parent is an anchor the link text is
     * shortened if necessary otherwise a new anchor element is created and inserted into the dom.
     * 
     * @param node
     *            the text node to analyze
     */
    private void transformAndShortenUrl(Text node) {
        Node parent = node.getParentNode();
        if (parent == null || !(parent instanceof Element)) {
            return;
        }
        if (hasMatchingParentElement(parent, "a")) {
            // shorten (only url part)
            String content = node.getTextContent();
            Matcher matcher = UrlHelper.URL_PATTERN.matcher(content);
            if (matcher.find()) {
                StringBuilder newContent = new StringBuilder();
                // save left text
                newContent.append(content.substring(0, matcher.start()));
                // get url
                StringBuilder protocolHostPart = new StringBuilder();
                protocolHostPart.append(matcher.group(1));
                protocolHostPart.append(matcher.group(2));
                String pathPart = buildPathPart(matcher.group(3), matcher.group(4));
                newContent.append(UrlHelper.shortenUrl(protocolHostPart.toString(), pathPart,
                        matcher.group(5), matcher.group(6)));
                // save right text
                newContent.append(content.substring(matcher.end()));
                node.setTextContent(newContent.toString());
            }
        } else {
            String content = node.getData();
            Matcher matcher = UrlHelper.URL_PATTERN.matcher(content);
            int offset = 0;
            while (matcher.find()) {
                // create text node for content left of url
                String leftText = content.substring(offset, matcher.start());
                // build url string
                StringBuilder protocolHostPart = new StringBuilder();
                protocolHostPart.append(matcher.group(1));
                protocolHostPart.append(matcher.group(2));

                String pathPart = buildPathPart(matcher.group(3), matcher.group(4));
                insertAnchor(protocolHostPart.toString(), pathPart, matcher.group(5), matcher
                        .group(6), node, leftText);
                offset = matcher.end();
            }
            // replace/remove the old text node
            if (offset > 0) {
                if (offset == content.length()) {
                    parent.removeChild(node);
                } else {
                    node.setTextContent(content.substring(offset));
                }
            }
        }
    }
}
