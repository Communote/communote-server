package com.communote.plugins.activity.base.processor;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.plugins.activity.base.service.ActivityServiceException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringImmutableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.template.NoteTemplateService;
import com.communote.server.model.blog.Blog;

/**
 * Note storing pre processor which asserts that an activity message can be created. For activities
 * that can not be created an exception is thrown. Creation will only be possible if the activity is
 * active for the target topic. A TO is considered to be an activity if the <code>templateId</code>
 * property is set and the property <code>contentTypes.activity</code> is set to
 * <code>activity</code>.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate(name = "ActivityNoteStoringPreProcessor")
@Provides
public class ActivityNoteStoringPreProcessor implements NoteStoringImmutableContentPreProcessor {

    @Requires
    private ActivityService activityService;
    private BlogManagement blogManagement;

    /**
     * @return the lazily initialized blog management
     */
    private BlogManagement getBlogManagement() {
        if (this.blogManagement == null) {
            this.blogManagement = ServiceLocator.instance().getService(BlogManagement.class);
        }
        return this.blogManagement;
    }

    @Override
    public int getOrder() {
        return NoteStoringImmutableContentPreProcessor.DEFAULT_ORDER + 1;
    }

    /**
     * Test whether the note is an activity.
     * 
     * @param noteStoringTO
     *            the TO describing the note
     * @return the templateId identifying the activity or null if the note is not an activity
     */
    private String isActivityNote(NoteStoringTO noteStoringTO) {
        String templateId = null;
        boolean activityTypeFound = false;
        for (StringPropertyTO property : noteStoringTO.getProperties()) {
            if (PropertyManagement.KEY_GROUP.equals(property.getKeyGroup())
                    && NoteTemplateService.NOTE_PROPERTY_KEY_TEMPLATE_ID.equals(property
                            .getPropertyKey())) {
                templateId = property.getPropertyValue();

            }
            if (ActivityService.PROPERTY_KEY_GROUP.equals(property.getKeyGroup())
                    && ActivityService.NOTE_PROPERTY_KEY_ACTIVITY.equals(property
                            .getPropertyKey())
                    && ActivityService.NOTE_PROPERTY_VALUE_ACTIVITY.equals(property
                            .getPropertyValue())) {
                activityTypeFound = true;
            }
            if (templateId != null && activityTypeFound) {
                return templateId;
            }
        }
        return null;
    }

    @Override
    public boolean isProcessAutosave() {
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ActivityDeactivatedNotePreProcessorException
     *             Thrown, when the given activity is deactivated.
     */
    @Override
    public NoteStoringTO process(NoteStoringTO noteStoringTO)
            throws ActivityDeactivatedNotePreProcessorException, NoteStoringPreProcessorException,
            NoteManagementAuthorizationException {
        String templateId = isActivityNote(noteStoringTO);
        if (templateId != null) {
            try {
                if (!activityService.isActivityActive(templateId, noteStoringTO.getBlogId())) {
                    throw new ActivityDeactivatedNotePreProcessorException(templateId);
                }
                // remove crosspost blogs because it is currently not possible/too expensive
                // to check activity configurations for all blogs
                noteStoringTO.setAdditionalBlogs(null);
            } catch (AuthorizationException e) {
                Blog blog = null;
                try {
                    blog = getBlogManagement().getBlogById(noteStoringTO.getBlogId(), false);
                } catch (BlogNotFoundException e1) {
                    // ignore
                } catch (BlogAccessException e1) {
                    // ignore
                }
                String title = blog != null ? blog.getTitle() : "undefined";
                throw new NoteManagementAuthorizationException(
                        "Current user is not authorized to create an activity in the selected topic",
                        e, title);
            } catch (NotFoundException e) {
                throw new NoteStoringPreProcessorException("The target topic does not exist", e);
            } catch (ActivityServiceException e) {
                throw new NoteStoringPreProcessorException(
                        "Internal error occurred while evaluating the activity configuration", e);
            }
        }
        return noteStoringTO;
    }
}
