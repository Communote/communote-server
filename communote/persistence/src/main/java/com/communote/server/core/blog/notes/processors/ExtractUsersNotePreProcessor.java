package com.communote.server.core.blog.notes.processors;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringImmutableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.core.blog.NoteManagement;
import com.communote.server.core.blog.notes.processors.exceptions.DirectMessageMissingRecipientException;
import com.communote.server.core.user.helper.ValidationPatterns;
import com.communote.server.model.note.Note;

/**
 * This processors extracts the users to be notified from the content. It also checks, if the note
 * is a direct message and takes this into account.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ExtractUsersNotePreProcessor implements NoteStoringImmutableContentPreProcessor {

    private static final String PATTERN_FOR_WHITESPACE_REPLACEMENTS = "</?(?:p|br)+\\s*/?>|&nbsp;|&#160;";

    /** Prefix for notifying users. */
    public static final String USER_PREFIX = "@";

    private static final Pattern DIRECT_MESSAGE_RECEIVER_PATTERN = Pattern
            .compile("(?:</?[\\w](?:\\s[^>]*)*>)*("
                    + NoteManagement.CONSTANT_MENTION_TOPIC_MANAGERS + "|"
                    + NoteManagement.CONSTANT_MENTION_TOPIC_READERS + "|"
                    + NoteManagement.CONSTANT_MENTION_TOPIC_AUTHORS + "|"
                    + NoteManagement.CONSTANT_MENTION_DISCUSSION_PARTICIPANTS + "|" + USER_PREFIX
                    + "(?:" + ValidationPatterns.PATTERN_ALIAS + "))(?:\\s|(</?[\\w]>))*\\s+");

    private static final Pattern IS_DIRECT_MESSAGE_PATTERN = Pattern
            .compile("((?:\\s|(</?[\\w]+(?:\\s[^>]*)*>))*[dD](?:\\s|(</?[\\w]+>))*)(?:"
                    + NoteManagement.CONSTANT_MENTION_TOPIC_MANAGERS + "|"
                    + NoteManagement.CONSTANT_MENTION_TOPIC_READERS + "|"
                    + NoteManagement.CONSTANT_MENTION_TOPIC_AUTHORS + "|"
                    + NoteManagement.CONSTANT_MENTION_DISCUSSION_PARTICIPANTS + "|" + USER_PREFIX
                    + "(?:" + ValidationPatterns.PATTERN_ALIAS + "))(?:\\s|(</?[\\w]+>))*.*");

    // matches user aliases with @ prefix
    private final static Pattern USER_PATTERN = Pattern.compile("(^|[\\s\\u00A0;,\\[(>])"
            + USER_PREFIX + "(" + ValidationPatterns.PATTERN_ALIAS + ")");

    /** Prefix for direct messages */
    public static final String DIRECT_MESSAGE_PREFIX = "d ";

    /**
     * Extracts the direct message receiver, if there is one.
     *
     * @param note
     *            The note.
     * @throws NoteStoringPreProcessorException
     *             Exception.
     */

    private void extractDirectMessageReceiver(NoteStoringTO note)
            throws NoteStoringPreProcessorException {
        String content = note.getContent();
        content = content.replaceAll(PATTERN_FOR_WHITESPACE_REPLACEMENTS, " ");
        Matcher isDirectMessageMatcher = IS_DIRECT_MESSAGE_PATTERN.matcher(content);
        if (isDirectMessageMatcher.matches()) {
            // appending a whitespace here otherwise the pattern won't match a user alias that
            // terminates the content string
            content = content.substring(isDirectMessageMatcher.end(1)) + " ";
            Matcher userMatcher = DIRECT_MESSAGE_RECEIVER_PATTERN.matcher(content);
            note.setIsDirectMessage(true);
            int lastEnd = 0;
            while (userMatcher.find()) {
                lastEnd = handleDirectMessageReceiver(note, userMatcher, lastEnd);
            }
        }
    }

    /**
     * Extracts users to be notify.
     *
     * @param note
     *            The note.
     */
    private void extractUsersToNotify(NoteStoringTO note) {
        Set<String> extractedUsers = new HashSet<String>();
        Matcher matcher = USER_PATTERN.matcher(note.getContent());
        while (matcher.find()) {
            extractedUsers.add(matcher.group(2));
        }
        if (note.getUsersToNotify() != null) {
            note.getUsersToNotify().addAll(extractedUsers);
        } else {
            note.setUsersToNotify(extractedUsers);
        }
    }

    /**
     * @return {@link NoteStoringImmutableContentPreProcessor#DEFAULT_ORDER}
     */
    @Override
    public int getOrder() {
        return NoteStoringImmutableContentPreProcessor.DEFAULT_ORDER;
    }

    /**
     * Handle as specific receiver found.
     *
     * @param note
     *            The note to handle
     * @param userMatcher
     *            The used matcher.
     * @param lastEnd
     *            Index of end of the previous found receiver.
     * @return The next index.
     * @throws DirectMessageMissingRecipientException
     *             Thrown, when the notification is invalid.
     */
    private int handleDirectMessageReceiver(NoteStoringTO note, Matcher userMatcher, int lastEnd)
            throws DirectMessageMissingRecipientException {
        String userAlias = userMatcher.group(1);
        theBigIf: {
            if (userAlias.equals(NoteManagement.CONSTANT_MENTION_TOPIC_MANAGERS)) {
                if (userMatcher.start() != lastEnd) {
                    note.getUsersNotToNotify().add(userAlias);
                    break theBigIf;
                }
                lastEnd = userMatcher.end();
                note.setMentionTopicManagers(true);
                break theBigIf;
            } else if (userAlias.equals(NoteManagement.CONSTANT_MENTION_TOPIC_READERS)
                    || userAlias.equals(NoteManagement.CONSTANT_MENTION_TOPIC_AUTHORS)
                    || userAlias.equals(NoteManagement.CONSTANT_MENTION_DISCUSSION_PARTICIPANTS)) {
                if (userMatcher.start() != lastEnd) {
                    note.getUsersNotToNotify().add(userAlias);
                    break theBigIf;
                }
                throw new DirectMessageMissingRecipientException(true);
            } else {
                userAlias = userAlias.replace(USER_PREFIX, "");
            }
            if (userMatcher.start() != lastEnd) {
                if (!note.getUsersToNotify().contains(userAlias)) {
                    note.getUsersNotToNotify().add(userAlias);
                }
            } else {
                note.getUsersToNotify().add(userAlias);
                lastEnd = userMatcher.end();
            }
        }
        return lastEnd;
    }

    @Override
    public boolean isProcessAutosave() {
        // notifications are only produced when publishing a note
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NoteStoringTO process(NoteStoringTO note) throws NoteStoringPreProcessorException {
        if (note.getUsersToNotify() == null) {
            note.setUsersToNotify(new HashSet<String>());
        }
        if (note.getUsersNotToNotify() == null) {
            note.setUsersNotToNotify(new HashSet<String>());
        }
        extractDirectMessageReceiver(note);
        if (!note.isIsDirectMessage()) {
            extractUsersToNotify(note);
        }
        setMentionsFlags(note);
        return note;
    }

    @Override
    public NoteStoringTO processEdit(Note noteToEdit, NoteStoringTO noteStoringTO)
            throws NoteStoringPreProcessorException, NoteManagementAuthorizationException {
        return process(noteStoringTO);
    }

    /**
     * Method to set the mentions flags.
     *
     * @param note
     *            The note to process.
     */
    private void setMentionsFlags(NoteStoringTO note) {
        String content = note.getContent();
        if (!note.isIsDirectMessage()) {
            note.setMentionTopicReaders(content
                    .contains(NoteManagement.CONSTANT_MENTION_TOPIC_READERS));
            note.setMentionTopicAuthors(content
                    .contains(NoteManagement.CONSTANT_MENTION_TOPIC_AUTHORS));
            note.setMentionTopicManagers(content
                    .contains(NoteManagement.CONSTANT_MENTION_TOPIC_MANAGERS));
            note.setMentionDiscussionAuthors(content
                    .contains(NoteManagement.CONSTANT_MENTION_DISCUSSION_PARTICIPANTS));
        }
    }
}
