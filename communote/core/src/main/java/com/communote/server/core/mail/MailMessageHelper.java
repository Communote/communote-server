package com.communote.server.core.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import com.communote.common.string.StringEscapeHelper;
import com.communote.common.util.HTMLHelper;
import com.communote.common.util.MsoHTMLHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.crc.vo.ContentMetadata;
import com.communote.server.core.mail.messages.GenericMailMessage;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.core.vo.content.AttachmentStreamTO;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.model.attachment.AttachmentStatus;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;

/**
 * Helper class for mail messages
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class MailMessageHelper {

    private static final String JAVA_MAIL_MESSAGE_ID_MARKER = ".JavaMail.";

    private static final String VALID_EMAIL_ADDRESS_CHARS = "[^\\s\\(\\)<>@\\\\,;:.\\[\\]\"]";
    private static final String EMAIL_ADDRESS_ATOM = VALID_EMAIL_ADDRESS_CHARS + "+(?:\\."
            + VALID_EMAIL_ADDRESS_CHARS + "+)*";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_ADDRESS_ATOM + "@"
            + EMAIL_ADDRESS_ATOM);

    /**
     * Suffix of an email address (starting with @) of an anonymized user.
     */
    public static final String ANONYMOUS_EMAIL_ADDRESS_SUFFIX = "@anonymous.host";

    private static final String X_ORIGINAL_TO = "X-Original-To";
    private static final String DELIVERED_TO = "Delivered-To";
    private static final String MIME_TYPE_MULTIPART_TYPES = "multipart/*";
    private static final String MIME_TYPE_PLAIN_TEXT = "text/plain";
    private static final String MIME_TYPE_HTML = "text/html";

    private static final Pattern FILENAME_PATTERN = Pattern.compile(".*(?:filename|name)=(.+)");

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MailMessageHelper.class);

    /**
     * Verifies that no email address in TO, BCC and CC headers ends with
     * {@link MailMessageHelper#ANONYMOUS_EMAIL_ADDRESS_SUFFIX}. In case such an email address is
     * encountered an {@link InvalidRecipientMailAddressException} will be thrown.
     *
     * @param message
     *            the message to check
     * @throws MessagingException
     *             in case the message has no recipients or getting the recipients failed
     * @throws InvalidRecipientMailAddressException
     *             in case an anonymous recipient is encountered
     */
    public static void assertNonAnonymousRecipients(Message message)
            throws MessagingException, InvalidRecipientMailAddressException {
        Address[] addresses = message.getAllRecipients();
        if (addresses == null || addresses.length == 0) {
            throw new MessagingException("Message has no recipients");
        }
        for (Address a : addresses) {
            if (a instanceof InternetAddress) {
                String email = ((InternetAddress) a).getAddress();
                if (isAnonymousEmailAddress(email)) {
                    throw new InvalidRecipientMailAddressException(
                            "Recipient address ends with the anonymous email address suffix.",
                            email);
                }
            }
        }
    }

    /**
     * Creates a value for the Message-ID header.
     *
     * @param identifier
     *            the identifier to convert to the header value. The identifier must be in
     *            dot-atom-text format as defined in RFC 2822
     * @return the header value
     */
    public static String createMessageIdHeaderValue(String identifier) {
        StringBuilder sb = new StringBuilder();
        InternetAddress localAddress = InternetAddress.getLocalAddress(null);
        String address = (localAddress != null) ? localAddress.getAddress()
                : "javamailuser@localhost";
        sb.append("<");
        sb.append(identifier);
        sb.append(JAVA_MAIL_MESSAGE_ID_MARKER);
        sb.append(address);
        sb.append(">");
        return sb.toString();
    }

    /**
     * Extracts the message identifier from the reply headers. The returned value is the identifier
     * passed to {@link #createMessageIdHeaderValue(String)}.
     *
     * @param message
     *            the message to evaluate
     * @return the identifier or null if none of the reply headers is set or the found value does
     *         not adhere to the msg-id format specification of RFC 822
     * @throws MessagingException
     *             in case of an error while reading the email headers
     */
    public static String extractMessageIdentifierFromReplyHeaders(Message message)
            throws MessagingException {
        String identifier = null;
        // first check In-Reply-To, if not set check References
        String orgMessageId;
        Pattern messageIdPattern = Pattern.compile("<[^>]+>");
        orgMessageId = extractMessageIdFromInReplyToHeader(message, messageIdPattern);
        if (orgMessageId == null) {
            // check references, which are in reverse order i.e. parent is last entry
            // this header also does not have a strict syntax so we go backward and take the first
            // looking like a Message-ID
            String[] referencesHeaders = message.getHeader("References");
            if (referencesHeaders != null) {
                for (int i = referencesHeaders.length - 1; i >= 0; i--) {
                    Matcher matcher = messageIdPattern.matcher(referencesHeaders[i]);
                    if (matcher.find()) {
                        orgMessageId = matcher.group();
                    }
                }
            }
        }
        // ignore the ID if it does not contain the '.JavaMail.'
        if (orgMessageId != null) {
            int idx = orgMessageId.indexOf(JAVA_MAIL_MESSAGE_ID_MARKER);
            // larger than 1 because of '>'
            if (idx > 1) {
                identifier = orgMessageId.substring(1, idx);
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Message-ID " + orgMessageId
                        + " does not seem to have been generated by Communote.");
            }
        }
        return identifier;
    }

    /**
     * Checks the In-Reply-To email header for something that looks like a Message-ID.
     *
     * @param message
     *            the email message to analyze
     * @param messageIdPattern
     *            the pattern to discover a Message-ID
     * @return the message ID or null
     * @throws MessagingException
     *             in case of an error while reading the header data
     */
    private static String extractMessageIdFromInReplyToHeader(Message message,
            Pattern messageIdPattern) throws MessagingException {
        String orgMessageId = null;
        String[] inReplyToHeaders = message.getHeader("In-Reply-To");
        // In-Reply-To does not have a strict syntax so we take first occurrence that looks like a
        // Message-ID
        if (inReplyToHeaders != null) {
            for (String inReplyToHeader : inReplyToHeaders) {
                Matcher matcher = messageIdPattern.matcher(inReplyToHeader);
                if (matcher.find()) {
                    orgMessageId = matcher.group();
                    break;
                }
            }
        }
        return orgMessageId;
    }

    /**
     * Extracts all parts (especially in case of a multipart message) of a message that have one of
     * the supplied MIME types.
     *
     * @param message
     *            the message to process
     * @param mimeTypes
     *            the MIME types to search for
     * @return a possibly empty mapping from MIME type to message part
     * @throws MessagingException
     *             in case of an error while reading the email data
     * @throws IOException
     *             if accessing the content of the part failed
     */
    public static Map<String, Part> extractMessagePartsMatchingMimeTypes(Message message,
            String... mimeTypes) throws MessagingException, IOException {
        Map<String, Part> matchingParts = new HashMap<String, Part>();
        extractMessagePartsMatchingMimeTypes(message, matchingParts, mimeTypes);
        return matchingParts;
    }

    /**
     * Extracts parts having a given MIME type recursively.
     *
     * @param part
     *            the part to process
     * @param matchingParts
     *            mapping from MIME type to message part with all previous found parts
     * @param mimeTypes
     *            the MIME types to search for
     * @throws MessagingException
     *             in case of an error while reading the email data
     * @throws IOException
     *             if accessing the content of the part failed
     */
    private static void extractMessagePartsMatchingMimeTypes(Part part,
            Map<String, Part> matchingParts, String... mimeTypes) throws MessagingException,
            IOException {
        if (part.isMimeType(MIME_TYPE_MULTIPART_TYPES)) {
            Multipart multipartContent = (Multipart) part.getContent();
            for (int i = 0; i < multipartContent.getCount(); i++) {
                extractMessagePartsMatchingMimeTypes(multipartContent.getBodyPart(i),
                        matchingParts, mimeTypes);
            }
        } else {
            for (String mimeType : mimeTypes) {
                // add a mimeType only once
                if (part.isMimeType(mimeType) && !matchingParts.containsKey(mimeType)) {
                    // some mail clients seem to add a content part which does not contain any
                    // content, such parts must be ignored
                    try {
                        // part without content will throw an IOException with message 'No content';
                        // a part.getSize() might do the same job but the documentation is unclear
                        part.getContent();
                    } catch (IOException e) {
                        if (e.getMessage().equals("No content")) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Found body part with MIME type " + mimeType
                                        + " that has no content");
                            }
                            // skip
                            return;
                        }
                        // another exception so better re-throw it
                        throw e;
                    }
                    matchingParts.put(mimeType, part);
                }
            }
        }
    }

    /**
     * Extract recipients from the message by evaluating the provided recipient type.
     *
     * @param message
     *            the message to check
     * @param recipientType
     *            the recipient header to evaluate
     * @param recipients
     *            storage for the found recipients
     * @param suffixFilter
     *            optional filter to restrict results to email addresses that end with the suffix.
     *            If null all values will be accepted.
     * @throws MessagingException
     *             in case of an error while reading the recipients
     */
    private static void extractRecipients(Message message, Message.RecipientType recipientType,
            Set<String> recipients, String suffixFilter) throws MessagingException {
        Address[] addresses = message.getRecipients(recipientType);
        if (addresses != null) {
            String lowerSuffix = suffixFilter == null ? null : suffixFilter
                    .toLowerCase(Locale.ENGLISH);
            for (Address a : addresses) {
                if (a instanceof InternetAddress) {
                    String addressString = ((InternetAddress) a).getAddress();
                    if (lowerSuffix != null) {
                        if (addressString.toLowerCase(Locale.ENGLISH).endsWith(lowerSuffix)) {
                            recipients.add(addressString);
                        }
                    } else {
                        recipients.add(addressString);
                    }
                }
            }
        }
    }

    /**
     * Extract recipients from the message by evaluating the provided header.
     *
     * @param message
     *            the message to check
     * @param header
     *            the header to evaluate
     * @param recipients
     *            storage for the found recipients
     * @param suffixFilter
     *            optional filter to restrict results to email addresses that end with the suffix.
     *            If null all values will be accepted.
     * @throws MessagingException
     *             in case of an error while reading the header values
     */
    private static void extractRecipients(Message message, String header, Set<String> recipients,
            String suffixFilter) throws MessagingException {
        String[] headerValues = message.getHeader(header);
        if (headerValues == null) {
            return;
        }
        String lowerSuffix = suffixFilter == null ? null : suffixFilter.toLowerCase(Locale.ENGLISH);
        for (String value : headerValues) {
            // get substring from header that is the email address
            Matcher emailMatcher = EMAIL_PATTERN.matcher(value);
            if (emailMatcher.find()) {
                String address = emailMatcher.group();
                if (lowerSuffix != null) {
                    if (address.toLowerCase(Locale.ENGLISH).endsWith(lowerSuffix)) {
                        recipients.add(address);
                    } else {
                        LOGGER.info("The prefix was not correct for a message send from '"
                                + StringUtils.join(message.getFrom()) + "' to '"
                                + StringUtils.join(message.getAllRecipients(), ",") + "'.");
                    }
                } else {
                    recipients.add(address);
                }
            }
        }
    }

    /**
     *
     * @param receivers
     * @return all users which do not have an anonymous email
     */
    public static Collection<User> filterAnonymousUsers(Collection<User> receivers) {
        Collection<User> realReceivers = new HashSet<>();
        for (User user : receivers) {
            if (!MailMessageHelper.isAnonymousEmailAddress(user.getEmail())) {
                realReceivers.add(user);
            }
        }
        return realReceivers;
    }

    /**
     * @param part
     *            The root part.
     * @return List of all children parts of the root parts or the root part itself, if it is not a
     *         multipart part.
     * @throws IOException
     *             Exception.
     * @throws MessagingException
     *             Exception.
     */
    private static Collection<Part> getAllParts(Part part) throws MessagingException, IOException {
        Collection<Part> parts = new ArrayList<Part>();
        if (part.isMimeType(MIME_TYPE_MULTIPART_TYPES)) {
            Multipart multipartContent = (Multipart) part.getContent();
            for (int i = 0; i < multipartContent.getCount(); i++) {
                parts.addAll(getAllParts(multipartContent.getBodyPart(i)));
            }
        } else {
            parts.add(part);
        }
        return parts;
    }

    /**
     * Shorthand for calling {@link #getAllRecipients(Message, boolean, boolean, String)} with
     * arguments message, true, true, null
     *
     * @param message
     *            the message to check
     * @return the addresses found or null if there were no
     * @throws MessagingException
     *             in case of an error while reading the recipients
     */
    public static String[] getAllRecipients(Message message) throws MessagingException {
        return getAllRecipients(message, true, true, null);
    }

    /**
     * Gets all recipients found in X-Original-To header field and/or the standard recipient headers
     * (TO,CC,Delivered-To - where the latter is useful for emails received because of a Bcc
     * header).
     *
     * @param message
     *            the message to check
     * @param evaluateBoth
     *            whether to check both header types. If false the other header type will only be
     *            evaluated if the first header type was empty.
     * @param startWithStandardHeaders
     *            when true the TO/CC/BCC headers will be evaluated first, otherwise the
     *            X-Original-To header
     * @param suffixFilter
     *            optional filter to restrict results to email addresses that end with the suffix.
     *            If null all values will be accepted.
     * @return the addresses found or null if there were no
     * @throws MessagingException
     *             in case of an error while reading the recipients
     */
    public static String[] getAllRecipients(Message message, boolean evaluateBoth,
            boolean startWithStandardHeaders, String suffixFilter) throws MessagingException {
        Set<String> recipients = new HashSet<String>();
        if (message != null) {
            if (startWithStandardHeaders) {
                extractRecipients(message, Message.RecipientType.TO, recipients, suffixFilter);
                extractRecipients(message, Message.RecipientType.CC, recipients, suffixFilter);
                extractRecipients(message, DELIVERED_TO, recipients, suffixFilter);
                if (recipients.size() == 0 || evaluateBoth) {
                    extractRecipients(message, X_ORIGINAL_TO, recipients, suffixFilter);
                }
            } else {
                extractRecipients(message, X_ORIGINAL_TO, recipients, suffixFilter);
                if (recipients.size() == 0 || evaluateBoth) {
                    // read standard recipient headers field
                    extractRecipients(message, Message.RecipientType.TO, recipients, suffixFilter);
                    extractRecipients(message, Message.RecipientType.CC, recipients, suffixFilter);
                    extractRecipients(message, DELIVERED_TO, recipients, suffixFilter);
                }
            }
        }
        return recipients.toArray(new String[recipients.size()]);
    }

    /**
     * Extracts the attachments from the mail.
     *
     * @param message
     *            The message.
     * @return Collection of attachments as {@link AttachmentTO}.
     * @throws IOException
     *             Exception.
     * @throws MessagingException
     *             Exception.
     */
    public static Collection<AttachmentTO> getAttachments(Message message)
            throws MessagingException, IOException {
        Collection<AttachmentTO> attachments = new ArrayList<AttachmentTO>();
        Collection<Part> parts = getAllParts(message);
        for (Part part : parts) {
            String disposition = part.getDisposition();
            String contentType = part.getContentType();
            if (StringUtils.containsIgnoreCase(disposition, "inline")
                    || StringUtils.containsIgnoreCase(disposition, "attachment")
                    || StringUtils.containsIgnoreCase(contentType, "name=")) {
                String fileName = part.getFileName();
                Matcher matcher = FILENAME_PATTERN.matcher(part.getContentType());
                if (matcher.matches()) {
                    fileName = matcher.group(1);
                    fileName = StringUtils.substringBeforeLast(fileName, ";");
                }
                if (StringUtils.isNotBlank(fileName)) {
                    fileName = fileName.replace("\"", "").replace("\\\"", "");
                    fileName = MimeUtility.decodeText(fileName);
                    if (fileName.endsWith("?=")) {
                        fileName = fileName.substring(0, fileName.length() - 2);
                    }
                    fileName = fileName.replace("?", "_");
                    AttachmentTO attachmentTO = new AttachmentStreamTO(part.getInputStream());
                    attachmentTO.setContentLength(part.getSize());
                    attachmentTO.setMetadata(new ContentMetadata());
                    attachmentTO.getMetadata().setFilename(fileName);
                    if (StringUtils.isNotBlank(contentType)) {
                        contentType = contentType.split(";")[0].toLowerCase();
                    }
                    attachmentTO.setStatus(AttachmentStatus.UPLOADED);
                    attachments.add(attachmentTO);
                }
            }
        }
        return attachments;
    }

    /**
     * Returns the text content of a email message.
     *
     * @param message
     *            the message to process
     * @param plainTextOnly
     *            when false this method will first look for an HTML message part and return it. If
     *            there is no such part a plain text part is searched and returned. In case this
     *            parameter is true this method will first search for a plain text message part and
     *            return it. If none exists a HTML message part is searched and cleaned from HTML
     *            markup before it is returned.
     * @param cleanup
     *            if true some cleanup operations will be done before returning. This only applies
     *            when returning HTML from an HTML part or plain text from a HTML part (here the
     *            cleanup will replace paragraphs with newlines).
     * @return the text part or null if there is no plain text or HTML part
     * @throws MessagingException
     *             in case of an error while reading the email data
     * @throws IOException
     *             if accessing the content of the part failed
     */
    public static String getMessageText(Message message, boolean plainTextOnly, boolean cleanup)
            throws MessagingException, IOException {
        String messageContent = null;
        if (message != null) {
            Map<String, Part> parts = extractMessagePartsMatchingMimeTypes(message,
                    MIME_TYPE_PLAIN_TEXT, MIME_TYPE_HTML);
            if (!plainTextOnly) {
                // check for HTML part, if not found return text
                if (parts.containsKey(MIME_TYPE_HTML)) {
                    messageContent = parts.get(MIME_TYPE_HTML).getContent().toString();
                    if (cleanup) {
                        if (isOutlookMessage(message)) {
                            // M$-Outlook mails require some cleanup for xml
                            // parsing
                            messageContent = HTMLHelper
                                    .encapsulateAttributesInQuotes(messageContent);
                            messageContent = HTMLHelper.removeNamespaces(messageContent);
                            messageContent = MsoHTMLHelper
                                    .convertListParagraphsToHTMLList(messageContent);
                            messageContent = HTMLHelper.removeUnclosedMetaElements(messageContent);
                        }
                        messageContent = HTMLHelper.stripLinebreaks(messageContent);
                        messageContent = HTMLHelper.removeComments(messageContent);
                    }

                } else if (parts.containsKey(MIME_TYPE_PLAIN_TEXT)) {
                    messageContent = parts.get(MIME_TYPE_PLAIN_TEXT).getContent().toString();
                    messageContent = HTMLHelper.plaintextToHTML(messageContent);
                }
            } else {
                // check for plain text part, if not found take html part and
                // convert to text
                if (parts.containsKey(MIME_TYPE_PLAIN_TEXT)) {
                    messageContent = parts.get(MIME_TYPE_PLAIN_TEXT).getContent().toString();
                } else if (parts.containsKey(MIME_TYPE_HTML)) {
                    messageContent = parts.get(MIME_TYPE_HTML).getContent().toString();
                    if (cleanup) {
                        messageContent = HTMLHelper.htmlToPlaintext(messageContent);
                    } else {
                        messageContent = StringEscapeHelper.removeHtmlMarkup(messageContent);
                    }
                }
            }
        }
        return messageContent;
    }

    /**
     * Returns the recipients found by evaluating the provided header.
     *
     * @param message
     *            the message to check
     * @param recipientType
     *            the recipient header to evaluate
     * @return the found recipients
     * @throws MessagingException
     *             in case of an error while reading the recipients
     */
    public static String[] getRecipients(Message message, Message.RecipientType recipientType)
            throws MessagingException {
        if (message == null) {
            return new String[0];
        }
        Set<String> recipients = new HashSet<String>();
        extractRecipients(message, recipientType, recipients, null);
        return recipients.toArray(new String[recipients.size()]);
    }

    /**
     *
     * @param email
     *            true if it is an anonymous email and hence no mail should be send here
     * @return
     */
    public static boolean isAnonymousEmailAddress(String email) {
        return email.endsWith(ANONYMOUS_EMAIL_ADDRESS_SUFFIX);
    }

    /**
     * Tries to determine whether a message was created using a MS Outlook Mail-Client. This is done
     * by inspecting the X-Mailer and the X-MimeOLE header.
     *
     * @param message
     *            the message to check
     * @return whether the message was created using MS Outlook
     * @throws MessagingException
     *             in case of an error while reading the email data
     */
    public static boolean isOutlookMessage(Message message) throws MessagingException {
        String[] headerData = message.getHeader("X-Mailer");
        boolean isOutlook = false;
        if (headerData != null) {
            for (String hd : headerData) {
                if (hd.toLowerCase().contains("outlook")) {
                    isOutlook = true;
                    break;
                }
            }
        } else {
            if (message.getHeader("X-MimeOLE") != null) {
                isOutlook = true;
            }
        }

        return isOutlook;
    }

    /**
     * Sends a mail message to the defined receivers with the correct locale.
     *
     * @param receivers
     *            List of receivers.
     * @param template
     *            The template.
     * @param model
     *            The model.
     */
    public static void sendMessage(Collection<User> receivers, String template,
            Map<String, Object> model) {
        sendMessage(receivers, template, model, null, null);
    }

    /**
     * Sends a mail message to the defined receivers with the correct locale.
     *
     * @param receivers
     *            List of receivers.
     * @param messageKey
     *            The message key of the template.
     * @param model
     *            The model.
     * @param replyToAddress
     *            Address for reply to (optional).
     * @param replyToName
     *            Name for reply to (optional).
     */
    public static void sendMessage(Collection<User> receivers, String messageKey,
            Map<String, Object> model, final String replyToAddress, final String replyToName) {

        final Collection<User> realReceivers = filterAnonymousUsers(receivers);

        if (realReceivers.size() > 0) {

            Map<Locale, Collection<User>> localizedUsers = UserManagementHelper
                    .getUserByLocale(realReceivers);
            for (Locale locale : localizedUsers.keySet()) {
                Collection<User> localizedReceivers = localizedUsers.get(locale);
                GenericMailMessage mailMessage = new GenericMailMessage(messageKey, locale, model,
                        localizedReceivers.toArray(new User[localizedReceivers.size()])) {
                    @Override
                    public String getReplyToAddress() {
                        return replyToAddress != null ? replyToAddress : super.getReplyToAddress();
                    }

                    @Override
                    public String getReplyToAddressName() {
                        return replyToName != null ? replyToName : super.getReplyToAddress();
                    }
                };

                ServiceLocator.findService(MailSender.class).send(mailMessage);
            }
        } else {
            LOGGER.info(
                    "Email not sent since no receivers or only anonymous receivers in list. receivers="
                            + receivers + " template=" + messageKey);
        }
    }

    /**
     * Sends a message to all Managers.
     *
     * @param template
     *            The template.
     * @param model
     *            The model.
     */
    public static void sendMessageToManagers(String template, Map<String, Object> model) {
        sendMessageToManagers(template, model, null, null);
    }

    /**
     * Sends a message to all Managers.
     *
     * @param template
     *            The template.
     * @param model
     *            The model.
     * @param replyToAddress
     *            Address for reply to (optional).
     * @param replyToName
     *            Name for reply to (optional).
     */
    public static void sendMessageToManagers(String template, Map<String, Object> model,
            String replyToAddress, String replyToName) {
        List<User> clientManagers = ServiceLocator
                .instance()
                .getService(UserManagement.class)
                .findUsersByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER,
                        UserStatus.ACTIVE);
        sendMessage(clientManagers, template, model, replyToAddress, replyToName);
    }

    /**
     * Stupid default constructor is private
     */
    private MailMessageHelper() {
        // Do nothing.
    }

}
