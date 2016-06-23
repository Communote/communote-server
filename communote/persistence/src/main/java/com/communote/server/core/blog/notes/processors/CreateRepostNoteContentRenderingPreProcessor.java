package com.communote.server.core.blog.notes.processors;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.processor.NoteContentRenderingPreProcessor;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorException;
import com.communote.server.model.user.UserRole;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.service.UserService;

/**
 * PreProcessor that prepares the note content for using it in an editor to create a repost to that
 * note. This preprocessor will add a short introductory text before the content of the note to
 * repost.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class CreateRepostNoteContentRenderingPreProcessor implements
NoteContentRenderingPreProcessor {

    /**
     * Option for the repost modes to disable prepending the author of the original note to the note
     * text. If set to "true", the content of the note list data item returned when one of repost
     * render modes is set will be prepended with a short introduction text which contains a mention
     * of the original author. If unset and the author of the note to repost is not a system user
     * the mention will also be included. Otherwise the introduction will not contain the mention.
     */
    public static final String MODE_OPTION_DISABLE_PREPEND_AUTHOR = "repostModeOptionDisablePrependAuthor";

    /**
     * {@inheritDoc}
     *
     * @return 10, so it runs at the end
     */
    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public boolean isCachable() {
        return false;
    }

    @Override
    public boolean processNoteContent(NoteRenderContext context, NoteData originalNoteItem)
            throws NoteRenderingPreProcessorException {
        String repostIntro;
        // prepend original author
        String disableAuthorOption = context.getModeOptions().get(
                MODE_OPTION_DISABLE_PREPEND_AUTHOR);
        boolean disableAuthor;
        if (disableAuthorOption == null) {
            disableAuthor = ServiceLocator.findService(UserService.class).hasRole(
                    originalNoteItem.getUser().getId(), UserRole.ROLE_SYSTEM_USER);
        } else {
            disableAuthor = Boolean.parseBoolean(disableAuthorOption);
        }
        if (disableAuthor) {
            repostIntro = ResourceBundleManager.instance().getText(
                    "createRepostNoteContentRenderingPreProcessor.intro", context.getLocale());
        } else {
            repostIntro = ResourceBundleManager.instance().getText(
                    "createRepostNoteContentRenderingPreProcessor.intro.author",
                    context.getLocale(), "@" + originalNoteItem.getUser().getAlias());
        }
        if (NoteRenderMode.REPOST_PLAIN.equals(context.getMode())) {
            originalNoteItem.setContent(repostIntro + "\n" + originalNoteItem.getContent());
        } else {
            originalNoteItem.setContent("<p>" + repostIntro + "</p>"
                    + originalNoteItem.getContent());
        }
        return true;
    }

    @Override
    public boolean replacesContent() {
        return false;
    }

    @Override
    public boolean supports(NoteRenderMode mode, NoteData note) {
        return NoteRenderMode.REPOST.equals(mode) || NoteRenderMode.REPOST_PLAIN.equals(mode);
    }

}
