package com.communote.server.service.blog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringEditableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringImmutableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorManager;
import com.communote.server.core.blog.notes.processors.NoteStoringPreProcessorManagerImpl;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class NotePreProcessorManagementTest {
    /**
     * Dummy processor for tests.
     */
    private class TestNoteEditableContentPreProcessor implements
            NoteStoringEditableContentPreProcessor {

        private final int order;
        private final List<Integer> callOrder;

        /**
         * @param order
         *            Priority of this processor.
         * @param callOrder
         *            shared collection to record the call order
         */
        public TestNoteEditableContentPreProcessor(int order, List<Integer> callOrder) {
            this.order = order;
            this.callOrder = callOrder;
        }

        /**
         * @return The priority.
         */
        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public boolean isProcessAutosave() {
            return false;
        }

        /**
         * Does nothing, but returning the original note.
         *
         * {@inheritDoc}
         */
        @Override
        public NoteStoringTO process(NoteStoringTO note) throws NoteStoringPreProcessorException {
            callOrder.add(order);
            return note;
        }

    }

    /**
     * Dummy processor for tests.
     */
    private class TestNoteImmutableContentPreProcessor implements
            NoteStoringImmutableContentPreProcessor {

        private final int order;
        private final List<Integer> callOrder;

        /**
         * @param order
         *            Priority of this processor.
         * @param callOrder
         *            shared collection to record the call order
         */
        public TestNoteImmutableContentPreProcessor(int order, List<Integer> callOrder) {
            this.order = order;
            this.callOrder = callOrder;
        }

        /**
         * @return The priority.
         */
        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public boolean isProcessAutosave() {
            return false;
        }

        /**
         * Does nothing, but returning the original note.
         *
         * {@inheritDoc}
         */
        @Override
        public NoteStoringTO process(NoteStoringTO note) throws NoteStoringPreProcessorException {
            callOrder.add(order);
            return note;
        }

    }

    /**
     * This tests, if the processors are called within the correct order.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testPriorityOfProcessors() throws Exception {
        List<Integer> callOrder = new ArrayList<Integer>();
        NoteStoringImmutableContentPreProcessor lowPriorityImmutable = new TestNoteImmutableContentPreProcessor(
                -1000, callOrder);
        NoteStoringImmutableContentPreProcessor midPriorityImmutable = new TestNoteImmutableContentPreProcessor(
                0, callOrder);
        NoteStoringImmutableContentPreProcessor highPriorityImmutable = new TestNoteImmutableContentPreProcessor(
                1000, callOrder);
        TestNoteEditableContentPreProcessor lowPriorityEditable = new TestNoteEditableContentPreProcessor(
                -1001, callOrder);
        TestNoteEditableContentPreProcessor midPriorityEditable = new TestNoteEditableContentPreProcessor(
                10, callOrder);
        TestNoteEditableContentPreProcessor highPriorityEditable = new TestNoteEditableContentPreProcessor(
                2000, callOrder);
        NoteStoringPreProcessorManager notePreProcessorManagement = new NoteStoringPreProcessorManagerImpl();
        // Remove default entries,as they might need access to Spring context.
        Field processors = notePreProcessorManagement.getClass().getDeclaredField(
                "notePreProcessors");
        processors.setAccessible(true);
        ((List<?>) processors.get(notePreProcessorManagement)).clear();
        processors = notePreProcessorManagement.getClass().getDeclaredField(
                "noteContentPreProcessors");
        processors.setAccessible(true);
        ((List<?>) processors.get(notePreProcessorManagement)).clear();
        processors = notePreProcessorManagement.getClass()
                .getDeclaredField("runAfterPreProcessors");
        processors.setAccessible(true);
        ((List<?>) processors.get(notePreProcessorManagement)).clear();
        processors = notePreProcessorManagement.getClass().getDeclaredField(
                "runAfterContentPreProcessors");
        processors.setAccessible(true);
        ((List<?>) processors.get(notePreProcessorManagement)).clear();

        notePreProcessorManagement.addProcessor(lowPriorityImmutable);
        notePreProcessorManagement.addProcessor(highPriorityImmutable);
        notePreProcessorManagement.addProcessor(lowPriorityEditable);
        notePreProcessorManagement.addProcessor(midPriorityImmutable);
        notePreProcessorManagement.addProcessor(midPriorityEditable);
        notePreProcessorManagement.addProcessor(highPriorityEditable);
        NoteStoringTO noteStoringTO = new NoteStoringTO();
        noteStoringTO.setContent("Test");
        noteStoringTO.setPublish(true);
        notePreProcessorManagement.process(noteStoringTO);
        Assert.assertEquals(6, callOrder.size());
        Assert.assertEquals(highPriorityEditable.getOrder(), (int) callOrder.get(0));
        Assert.assertEquals(midPriorityEditable.getOrder(), (int) callOrder.get(1));
        Assert.assertEquals(lowPriorityEditable.getOrder(), (int) callOrder.get(2));
        Assert.assertEquals(highPriorityImmutable.getOrder(), (int) callOrder.get(3));
        Assert.assertEquals(midPriorityImmutable.getOrder(), (int) callOrder.get(4));
        Assert.assertEquals(lowPriorityImmutable.getOrder(), (int) callOrder.get(5));
    }

}
