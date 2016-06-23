package com.communote.plugins.smileys;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.common.util.Pair;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.processor.NoteContentRenderingPreProcessor;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Provides
@Instantiate(name = "SmileysNoteRenderingPreProcessor")
public class SmileysNoteRenderingPreProcessor implements NoteContentRenderingPreProcessor {

    /** Mapping of String to CSS classes of the smileys. */
    private final static List<Pair<String, String>> SMILEYS = new ArrayList<>();

    /** An array of word, which should be "escaped" before the content is processed. */
    private final static String[] WORDS_TO_ESCAPE = new String[] { ">", "<", "&nbsp;", "&#160;" };

    static {
        // Sort order is important, as some Icons may include others.
        SMILEYS.add(new Pair<>(":)", "smile"));
        SMILEYS.add(new Pair<>(":-)", "smile"));
        SMILEYS.add(new Pair<>("=)", "smile"));
        SMILEYS.add(new Pair<>("&gt;:(", "angry"));
        SMILEYS.add(new Pair<>("&gt;:-(", "angry"));
        SMILEYS.add(new Pair<>(":(", "sad"));
        SMILEYS.add(new Pair<>(":-(", "sad"));
        SMILEYS.add(new Pair<>("=(", "sad"));
        SMILEYS.add(new Pair<>(";)", "wink"));
        SMILEYS.add(new Pair<>(";-)", "wink"));
        SMILEYS.add(new Pair<>(":P", "tongue"));
        SMILEYS.add(new Pair<>(":-P", "tongue"));
        SMILEYS.add(new Pair<>("=P", "tongue"));
        SMILEYS.add(new Pair<>(":'(", "cry"));
        SMILEYS.add(new Pair<>("='(", "cry"));
        SMILEYS.add(new Pair<>("XD", "laugh"));
        SMILEYS.add(new Pair<>(":D", "grin"));
        SMILEYS.add(new Pair<>(":-D", "grin"));
        SMILEYS.add(new Pair<>("=D", "grin"));
        SMILEYS.add(new Pair<>(":\\", "think"));
        SMILEYS.add(new Pair<>("(-_-)zzz", "sleep"));
        SMILEYS.add(new Pair<>("|-)", "sleep"));
        SMILEYS.add(new Pair<>("&gt;:O", "angry"));
        SMILEYS.add(new Pair<>(":O", "amazed"));
        SMILEYS.add(new Pair<>("=O", "amazed"));
        SMILEYS.add(new Pair<>("o.O", "confused"));
        SMILEYS.add(new Pair<>(":X", "voiceless"));
        SMILEYS.add(new Pair<>("8)", "nerd"));
        SMILEYS.add(new Pair<>("8-)", "nerd"));
        SMILEYS.add(new Pair<>(":|", "deadpan"));
        SMILEYS.add(new Pair<>("%)", "crazy"));
        SMILEYS.add(new Pair<>("-_-", "yawn"));
        SMILEYS.add(new Pair<>("(y)", "dig"));
        SMILEYS.add(new Pair<>("(cake)", "cake"));
        SMILEYS.add(new Pair<>("#:-S", "phew"));
        SMILEYS.add(new Pair<>("m(", "facepalm"));
        SMILEYS.add(new Pair<>(":webiconset:", "copyright"));
    }

    @Override
    public int getOrder() {
        // give processor a pretty high order so it is run before the other processors. This is
        // required to ensure that caching is not disabled by a processor with higher prio and no
        // caching enabled. We also rely on some internal knowledge here: the template preprocessor
        // has a smaller priority which will lead to skipping this preprocessor for templates
        // because they cannot be cached.
        return DEFAULT_ORDER + 100;
    }

    @Override
    public boolean isCachable() {
        return true;
    }

    /**
     * This methods searches for smileys and replaces them with the corresponding html entity.
     * 
     * @param content
     *            The content to search and replace for.
     * @return The altered content.
     */
    private String processContent(String content) {
        if (content == null) {
            return null;
        }
        content = " " + content + " ";
        for (String wordToEscape : WORDS_TO_ESCAPE) {
            content = content.replace(wordToEscape, " " + wordToEscape + " ");
        }
        for (Pair<String, String> smiley : SMILEYS) {
            content = content.replace(" " + smiley.getLeft() + " ", " <i class=\"smiley "
                    + smiley.getRight() + "\" title=\"" + smiley.getLeft() + "\"></i> ");
        }
        for (String wordToEscape : WORDS_TO_ESCAPE) {
            content = content.replace(" " + wordToEscape + " ", wordToEscape);
        }
        return content.substring(1, content.length() - 1);
    }

    /**
     * Processes a note for a specific render context. This method will only be called if the
     * processor supports the mode given by the render context.
     * 
     * @param context
     *            holds details about the render context to allow specific processing in different
     *            situations
     * @param item
     *            the item to be processed
     * @return true if the item was modified, false otherwise
     * @throws com.communote.server.api.core.note.processor.NoteRenderingPreProcessorException
     *             in case something unexpected lead to the failure of the processor
     */
    @Override
    public boolean processNoteContent(NoteRenderContext context, NoteData item)
            throws NoteRenderingPreProcessorException {
        item.setContent(processContent(item.getContent()));
        item.setShortContent(processContent(item.getShortContent()));
        return true;
    }

    @Override
    public boolean replacesContent() {
        return false;
    }

    /**
     * @return True, if Portal.
     */
    @Override
    public boolean supports(NoteRenderMode mode, NoteData item) {
        return NoteRenderMode.PORTAL.equals(mode);
    }
}
