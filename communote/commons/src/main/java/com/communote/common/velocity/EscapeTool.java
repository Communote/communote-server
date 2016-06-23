package com.communote.common.velocity;

import com.communote.common.string.StringEscapeHelper;
import com.communote.common.string.StringHelper;

/**
 * Extended version of the generic velocity escape tool that provides some additional escape
 * functions.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class EscapeTool extends org.apache.velocity.tools.generic.EscapeTool {

    /**
     * Escapes the double quotes in a string with a backslash.
     * 
     * @param string
     *            string to escape
     * @return the escaped string
     */
    public String escapeQuote(Object string) {
        if (string == null) {
            return null;
        }
        return StringHelper.replaceCharacter(string.toString(), '"', "\\\"");
    }

    /**
     * Escapes the single quotes in a string with a backslash.
     * 
     * @param string
     *            the string to escape
     * @return the escaped string
     */
    public String escapeSingleQuote(Object string) {
        if (string == null) {
            return null;
        }
        return StringHelper.replaceCharacter(string.toString(), '\'', "\\'");
    }

    /**
     * Exposes {@link StringEscapeHelper#escapeJavaScriptInlineHtml(String)} to the Velocity engine.
     * 
     * @param string
     *            the string to escape
     * @return the escaped string
     * @see StringEscapeHelper#escapeJavaScriptInlineHtml(String)
     */
    public String javascriptInline(Object string) {
        if (string == null) {
            return null;
        }
        return StringEscapeHelper.escapeJavaScriptInlineHtml(string.toString());
    }

    /**
     * Escapes only single and double quotes with a backslash.
     * 
     * @param string
     *            string to escape
     * @return the escaped string
     */
    public String javascriptSimple(Object string) {
        if (string == null) {
            return null;
        }
        return StringEscapeHelper.escapeJavaScript(string.toString());
    }

    /**
     * Exposes {@link StringEscapeHelper#escapeJsonInlineHtml(String)} to the Velocity engine.
     * 
     * @param string
     *            the serialized JSON string to escape
     * @return the escaped string
     * @see StringEscapeHelper#escapeJsonInlineHtml(String)
     */
    public String jsonInline(Object string) {
        if (string == null) {
            return null;
        }
        return StringEscapeHelper.escapeJsonInlineHtml(string.toString());
    }

    /**
     * Exposes {@link StringEscapeHelper#escapeNonWordCharacters(String)} to velocity templates.
     * 
     * @param string
     *            the string to escape
     * @return the escaped string
     * @see StringEscapeHelper#escapeNonWordCharacters(String)
     */
    public String nonWordChars(Object string) {
        if (string == null) {
            return null;
        }
        return StringEscapeHelper.escapeNonWordCharacters(string.toString());
    }

    /**
     * Simple version of XML escaping which delegates to
     * {@link StringEscapeHelper#escapeXml(String)}.
     * 
     * @param string
     *            string to escape
     * @return the escaped string
     */
    public String xmlSimple(Object string) {
        if (string == null) {
            return null;
        }
        return StringEscapeHelper.escapeXml(string.toString());
    }
}
