package com.communote.server.core.note.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.common.util.DescendingOrderComparator;
import com.communote.common.util.HTMLHelper;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.processor.NoteContentRenderingPreProcessor;
import com.communote.server.api.core.note.processor.NoteMetadataRenderingPreProcessor;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorException;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorManager;
import com.communote.server.core.blog.notes.processors.CreateRepostNoteContentRenderingPreProcessor;
import com.communote.server.core.blog.notes.processors.CreateRepostNoteMetadataRenderingPreProcessor;
import com.communote.server.core.blog.notes.processors.RepostNoteRenderingPreProcessor;
import com.communote.server.core.common.caching.Cache;
import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.core.common.caching.CacheKey;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.template.TemplateNoteRenderingPreProcessor;

/**
 * Implementation of the note rendering preprocessor extension point.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
// TODO cache invalidation when notes changed indirectly, e.g. due to TagMangement or user deletion
// But how can we do this generically? Should the NoteContentRenderingPreProcessors provide event
// classes that indicate content changes and this instance registers itself as listener for the
// events?
@Service("noteRenderingPreProcessorManager")
public class NoteRenderingExtensionPoint implements NoteRenderingPreProcessorManager {

    /**
     * Cache key for caching the preprocessed content of a note.
     */
    private class NoteContentCacheKey implements CacheKey {

        private final String cacheKeyString;

        /**
         * Create a new key.
         *
         * @param mode
         *            the mode for rendering the note
         * @param noteId
         *            the ID of the note
         */
        public NoteContentCacheKey(NoteRenderMode mode, Long noteId) {
            cacheKeyString = mode.name() + "." + noteId;
        }

        @Override
        public String getCacheKeyString() {
            return cacheKeyString;
        }

        @Override
        public boolean isUniquePerClient() {
            return true;
        }

    }

    /**
     * Provider which calls the computation-time expensive, but cachable preprocessors. When passed
     * to the get method of the cache a new provider instance with the current, supported
     * preprocessors must be created.
     */
    private class NoteContentElementProvider implements
            CacheElementProvider<NoteContentCacheKey, NoteContentHolder> {
        private final NoteData note;
        private final List<NoteContentRenderingPreProcessor> cachableProcessors;
        private final NoteRenderContext context;

        /**
         * create a new provider
         *
         * @param context
         *            the render context of the note
         * @param note
         *            the list item holding the details of the note to render
         * @param cachableProcessors
         *            cachable preprocessors that support the render mode and the note
         */
        public NoteContentElementProvider(NoteRenderContext context, NoteData note,
                List<NoteContentRenderingPreProcessor> cachableProcessors) {
            this.cachableProcessors = cachableProcessors;
            this.note = note;
            this.context = context;
        }

        @Override
        public String getContentType() {
            return "NoteContent";
        }

        @Override
        public int getTimeToLive() {
            return 7200;
        }

        @Override
        public NoteContentHolder load(NoteContentCacheKey key) {
            for (NoteContentRenderingPreProcessor processor : this.cachableProcessors) {
                try {
                    processor.processNoteContent(this.context, this.note);
                } catch (NoteRenderingPreProcessorException e) {
                    // wrap into a RTE to avoid caching
                    throw new RuntimeException(e);
                }
            }
            return new NoteContentHolder(this.note.getShortContent(), this.note.getContent(),
                    this.note.getLastModificationDate().getTime());
        }

    }

    @Autowired
    private CacheManager cacheManager;

    private List<NoteMetadataRenderingPreProcessor> metadataPreProcessors = new ArrayList<>();
    private List<NoteMetadataRenderingPreProcessor> metadataAfterContentPreProcessors = new ArrayList<>();
    private List<NoteContentRenderingPreProcessor> contentPreProcessors = new ArrayList<>();
    private final DescendingOrderComparator priorityComparator = new DescendingOrderComparator();

    /**
     * initialization stuff
     */
    @PostConstruct
    private void init() {
        // register core extensions
        addProcessor(new CreateRepostNoteContentRenderingPreProcessor());
        addProcessor(new CreateRepostNoteMetadataRenderingPreProcessor(), true);
        addProcessor(new TemplateNoteRenderingPreProcessor());
        addProcessor(new RepostNoteRenderingPreProcessor());
        addProcessor(new LikeNoteRenderingPreProcessor());
    }

    /**
     * Takes the short and full content of the note from the cache and sets the values in the note
     * list data object. If the content is not cached the cachablePreprocessors are applied to the
     * full and short content of the note and the result is cached.
     *
     * @param context
     *            the render context of the note
     * @param noteItem
     *            the note to render
     * @param cachableProcessors
     *            the cachable preprocessors
     * @throws NoteRenderingPreProcessorException
     *             in case content processing of one of the preprocessors failed
     */
    private void insertCachedContent(NoteRenderContext context, NoteData noteItem,
            List<NoteContentRenderingPreProcessor> cachableProcessors)
            throws NoteRenderingPreProcessorException {
        if (cachableProcessors.size() > 0) {
            NoteContentElementProvider cacheElementProvider = new NoteContentElementProvider(
                    context, noteItem, cachableProcessors);
            NoteContentCacheKey cacheKey = new NoteContentCacheKey(context.getMode(),
                    noteItem.getId());
            Cache cache = cacheManager.getCache();
            try {
                NoteContentHolder content = cache.get(cacheKey, cacheElementProvider);
                // invalidate and reload if changed
                if (content.getLastModificationTimestamp() < noteItem.getLastModificationDate()
                        .getTime()) {
                    cache.invalidate(cacheKey, cacheElementProvider);
                    content = cache.get(cacheKey, cacheElementProvider);
                }
                noteItem.setContent(content.getFullContent());
                noteItem.setShortContent(content.getShortContent());
            } catch (RuntimeException e) {
                Throwable cause = e.getCause();
                if (cause != null && cause instanceof NoteRenderingPreProcessorException) {
                    throw (NoteRenderingPreProcessorException) cause;
                }
            }
        }
    }

    @Override
    public NoteData process(NoteRenderContext context, NoteData item)
            throws NoteRenderingPreProcessorException {
        if (context.getMode() != null) {
            String content = item.getContent();
            String shortContent = item.getShortContent();
            for (NoteMetadataRenderingPreProcessor preProcessor : metadataPreProcessors) {
                if (preProcessor.supports(context.getMode())) {
                    preProcessor.process(context, item);
                }
            }
            // restore content in case some processor modified it
            item.setContent(content);
            item.setShortContent(shortContent);
            processNoteContent(context, item);
            // run metadata preprocessors that should run after the content preprocessors
            content = item.getContent();
            shortContent = item.getShortContent();
            for (NoteMetadataRenderingPreProcessor preProcessor : metadataAfterContentPreProcessors) {
                if (preProcessor.supports(context.getMode())) {
                    preProcessor.process(context, item);
                }
            }
            item.setContent(content);
            item.setShortContent(shortContent);
        }
        return item;
    }

    /**
     * Calls all registered content preprocessors and lets them modify the full and shortend content
     * of the note list data object.
     *
     * @param context
     *            the note render context
     * @param noteItem
     *            the note to render
     * @throws NoteRenderingPreProcessorException
     *             in case content processing of one of the preprocessors failed
     */
    private void processNoteContent(NoteRenderContext context, NoteData noteItem)
            throws NoteRenderingPreProcessorException {
        ArrayList<NoteContentRenderingPreProcessor> cachableProcessors = new ArrayList<>();
        ArrayList<NoteContentRenderingPreProcessor> remainingProcessors = new ArrayList<>();
        boolean replacingContentProcessorFound = separateContentRenderingPreProcessors(
                context.getMode(), noteItem, cachableProcessors, remainingProcessors);
        // convert to plain text if necessary, which isn't if a preprocessor is going to replace
        // the whole content
        if ((context.getMode().equals(NoteRenderMode.PLAIN) || context.getMode().equals(
                NoteRenderMode.REPOST_PLAIN))
                && !replacingContentProcessorFound) {
            boolean beautify = Boolean.TRUE.toString().equals(
                    context.getModeOptions().get(NoteRenderMode.PLAIN_MODE_OPTION_KEY_BEAUTIFY));
            noteItem.setContent(HTMLHelper.htmlToPlaintextExt(noteItem.getContent(), beautify));
            if (noteItem.getShortContent() != null) {
                noteItem.setShortContent(HTMLHelper.htmlToPlaintextExt(noteItem.getShortContent(),
                        beautify));
            }
        }
        insertCachedContent(context, noteItem, cachableProcessors);
        // run remaining pre-processors
        for (NoteContentRenderingPreProcessor preProcessor : remainingProcessors) {
            if (preProcessor.supports(context.getMode(), noteItem)) {
                preProcessor.processNoteContent(context, noteItem);
            }
        }

    }

    @Override
    public synchronized void addProcessor(NoteContentRenderingPreProcessor processor) {
        List<NoteContentRenderingPreProcessor> newPreProcessors = new ArrayList<>(
                this.contentPreProcessors);
        newPreProcessors.add(processor);
        Collections.sort(newPreProcessors, priorityComparator);
        this.contentPreProcessors = newPreProcessors;
        // invalidate all cached note contents if the processor supports caching
        if (processor.isCachable()) {
            // no need to pass parameters since provider is only used for invalidation
            cacheManager.getCache().invalidateAll(new NoteContentElementProvider(null, null, null));
        }
    }

    @Override
    public synchronized void addProcessor(NoteMetadataRenderingPreProcessor processor) {
        addProcessor(processor, false);
    }

    @Override
    public synchronized void addProcessor(NoteMetadataRenderingPreProcessor processor,
            boolean runAfterContentProcessors) {
        if (runAfterContentProcessors) {
            List<NoteMetadataRenderingPreProcessor> newPreProcessors = new ArrayList<>(
                    metadataAfterContentPreProcessors);
            newPreProcessors.add(processor);
            Collections.sort(newPreProcessors, priorityComparator);
            metadataAfterContentPreProcessors = newPreProcessors;
        } else {
            List<NoteMetadataRenderingPreProcessor> newPreProcessors = new ArrayList<>(
                    metadataPreProcessors);
            newPreProcessors.add(processor);
            Collections.sort(newPreProcessors, priorityComparator);
            metadataPreProcessors = newPreProcessors;
        }
    }

    /**
     * Separates the registered content preprocessors into cachable and those whose output cannot be
     * cached. Processors which do not support the note or render mode will be ignored and not added
     * to the lists. As soon as an uncachable processor is found all subsequent preprocessors will
     * also be added to the uncachable.
     *
     * If one of the preprocessors states that it replaces the content completely all preprocessors
     * before this one will not be in the lists as this preprocessor would overwrite their
     * modifications anyway.
     *
     * @param renderMode
     *            the current note render mode
     * @param noteItem
     *            the list item holding the details of the note to render
     * @param cachableProcessors
     *            the list to add the cachable processors to
     * @param remainingProcessors
     *            the list to add all the processors to which are not cachable or were found after a
     *            processor that is not cachable
     * @return true if one of the preprocessors in the cachableProcessors or remainingProcessors
     *         replaces the content completely
     */
    private boolean separateContentRenderingPreProcessors(NoteRenderMode renderMode,
            NoteData noteItem, List<NoteContentRenderingPreProcessor> cachableProcessors,
            List<NoteContentRenderingPreProcessor> remainingProcessors) {
        // only cache the portal preprocessors (currently only preprocessors supporting this mode do
        // expensive content modifications)
        boolean uncachableFound = !NoteRenderMode.PORTAL.equals(renderMode);
        boolean contentReplacerFound = false;
        for (NoteContentRenderingPreProcessor processor : this.contentPreProcessors) {
            if (processor.supports(renderMode, noteItem)) {
                if (processor.replacesContent()) {
                    // as processor replaces the content all previously running preprocessors can be
                    // removed
                    cachableProcessors.clear();
                    remainingProcessors.clear();
                    contentReplacerFound = true;
                }
                if (uncachableFound) {
                    remainingProcessors.add(processor);
                } else if (processor.isCachable()) {
                    cachableProcessors.add(processor);
                } else {
                    remainingProcessors.add(processor);
                    uncachableFound = true;
                }
            }
        }
        return contentReplacerFound;
    }

    @Override
    public synchronized void removeProcessor(NoteContentRenderingPreProcessor processor) {
        List<NoteContentRenderingPreProcessor> newPreProcessors = new ArrayList<>(
                this.contentPreProcessors);
        if (newPreProcessors.remove(processor)) {
            this.contentPreProcessors = newPreProcessors;
            // invalidate all cached note contents if the processor supports caching
            if (processor.isCachable()) {
                // no need to pass parameters since provider is only used for invalidation
                cacheManager.getCache().invalidateAll(
                        new NoteContentElementProvider(null, null, null));
            }
        }
    }

    @Override
    public synchronized void removeProcessor(NoteMetadataRenderingPreProcessor processor) {
        if (metadataAfterContentPreProcessors.contains(processor)) {
            List<NoteMetadataRenderingPreProcessor> newPreProcessors = new ArrayList<>(
                    metadataAfterContentPreProcessors);
            newPreProcessors.remove(processor);
            metadataAfterContentPreProcessors = newPreProcessors;
        } else if (metadataPreProcessors.contains(processor)) {
            List<NoteMetadataRenderingPreProcessor> newPreProcessors = new ArrayList<>(
                    metadataPreProcessors);
            newPreProcessors.remove(processor);
            metadataPreProcessors = newPreProcessors;
        }
    }
}
