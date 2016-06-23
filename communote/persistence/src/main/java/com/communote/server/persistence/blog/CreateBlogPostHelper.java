package com.communote.server.persistence.blog;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.note.NoteStoringFailDefinition;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.core.blog.AttachmentAlreadyAssignedException;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.core.vo.uti.UserNotificationResult;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * Helper class for blog post creation.<br>
 * TODO rwi move to service.blog
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CreateBlogPostHelper {

    /**
     * Create the blog warning if necessary.
     * 
     * @param result
     *            the result object
     * @param locale
     *            the locale to use
     * @param rbm
     *            the resource bundle manager for retrieving localized messages
     * @return the string builder holding the message
     */

    private static StringBuilder createBlogWarning(NoteModificationResult result, Locale locale,
            ResourceBundleManager rbm) {
        StringBuilder blogWarningSb = new StringBuilder();
        if (result.getUnresolvableBlogs().size() > 0) {
            blogWarningSb.append(rbm.getText("blogpost.create.success.warning.blogs.unresolvable",
                    locale, StringUtils.join(result.getUnresolvableBlogs(), ", ")));
        }
        if (result.getUnwritableBlogs().size() > 0) {
            if (blogWarningSb.length() > 0) {
                blogWarningSb.append(rbm.getText("blogpost.create.success.warning.blogs.connector",
                        locale));
            }
            blogWarningSb.append(rbm.getText("blogpost.create.success.warning.blogs.unwritable",
                    locale, StringUtils.join(result.getUnwritableBlogs(), ", ")));
        }
        return blogWarningSb;
    }

    /**
     * Create an error message.
     * 
     * @param result
     *            the result object returned by modification service method
     * @param locale
     *            the locale to use for the message
     * @return the message or null
     */
    private static String createErrorMessage(NoteModificationResult result, Locale locale) {
        String message;
        ResourceBundleManager rbm = ResourceBundleManager.instance();
        switch (result.getStatus()) {
        case NOTIFICATION_ERROR:
            StringBuilder messageBuilder = new StringBuilder();
            if (result.getUserNotificationResult().getUnresolvableUsers().size() > 0) {
                messageBuilder.append(rbm.getText("error.blogpost.create.users.unresolvable",
                        locale, StringUtils.join(result.getUserNotificationResult()
                                .getUnresolvableUsers(), ", ")));
            }
            if (result.getUserNotificationResult().getUninformableUsers().size() > 0) {
                if (messageBuilder.length() != 0) {
                    messageBuilder.append(rbm.getText("error.blogpost.create.users.connector",
                            locale));
                }
                messageBuilder.append(rbm.getText("error.blogpost.create.users.uninformable",
                        locale, StringUtils.join(result.getUserNotificationResult()
                                .getUninformableUsers(), ", ")));
            }
            message = rbm.getText("error.blogpost.create.users", locale, messageBuilder.toString());
            break;
        case CROSSPOST_ERROR:
            messageBuilder = new StringBuilder();
            if (result.getUnresolvableBlogs().size() > 0) {
                messageBuilder.append(rbm.getText("error.blogpost.create.blogs.unresolvable",
                        locale, StringUtils.join(result.getUnresolvableBlogs(), ", ")));
            }
            if (result.getUnwritableBlogs().size() > 0) {
                if (messageBuilder.length() != 0) {
                    messageBuilder.append(rbm.getText("error.blogpost.create.blogs.connector",
                            locale));
                }
                messageBuilder.append(rbm.getText("error.blogpost.create.blogs.unwritable", locale,
                        StringUtils.join(result.getUnwritableBlogs(), ", ")));
            }
            message = rbm.getText("error.blogpost.create.blogs", locale, messageBuilder.toString());
            break;
        case LIMIT_REACHED:
        case MISSING_ATTACHMENT:
            return rbm.getText("error.blogpost.create." + result.getStatus(), locale);
        default:
            return createErrorMessageKeyForSystemError(result, rbm, locale);
        }
        return message;
    }

    /**
     * Returns a suitable error message for the system error case.
     * 
     * @param result
     *            the result object returned by modification service method
     * @param rbm
     *            the resource bundle manager
     * @param locale
     *            the locale to use for the message
     * @return the error message
     */
    private static String createErrorMessageKeyForSystemError(NoteModificationResult result,
            ResourceBundleManager rbm, Locale locale) {
        Throwable cause = result.getErrorCause();
        if (cause != null && cause instanceof AttachmentAlreadyAssignedException) {
            AttachmentAlreadyAssignedException e = (AttachmentAlreadyAssignedException) cause;
            return rbm
                    .getText("error.blogpost.create.attachment.assigned", locale, e.getFilename());
        }
        return rbm.getText("error.blogpost.create.failed", locale);
    }

    /**
     * Create a warning message if necessary.
     * 
     * @param result
     *            the result object returned by modification service method
     * @param locale
     *            the locale to use for the message
     * @return the message or null
     */
    private static String createWarningMessage(NoteModificationResult result, Locale locale) {
        ResourceBundleManager resourceBundleManager = ResourceBundleManager.instance();
        UserNotificationResult userNotificationResult = result.getUserNotificationResult();
        StringBuilder warningSb = new StringBuilder();
        StringBuilder userWarningSb = new StringBuilder();
        if (userNotificationResult.getUnresolvableUsers().size() > 0) {
            userWarningSb.append(resourceBundleManager.getText(
                    "blogpost.create.success.warning.users.unresolvable", locale,
                    StringUtils.join(userNotificationResult.getUnresolvableUsers(), ", ")));
        }
        if (userNotificationResult.getUninformableUsers().size() > 0) {
            if (userWarningSb.length() > 0) {
                userWarningSb.append(resourceBundleManager.getText(
                        "blogpost.create.success.warning.users.connector", locale));
            }
            userWarningSb.append(resourceBundleManager.getText(
                    "blogpost.create.success.warning.users.uninformable.is.direct."
                            + Boolean.toString(result.isDirect()).toLowerCase(), locale,
                    StringUtils.join(userNotificationResult.getUninformableUsers(), ", ")));
        }
        if (userWarningSb.length() != 0) {
            String userWarning = resourceBundleManager.getText(
                    "blogpost.create.success.warning.users", locale, userWarningSb.toString());
            userWarning = resourceBundleManager.getText("blogpost.create.success.warning", locale,
                    userWarning);
            warningSb.append(userWarning);
        }
        // blog warnings
        StringBuilder blogWarningSb = createBlogWarning(result, locale, resourceBundleManager);
        if (blogWarningSb.length() != 0) {
            if (warningSb.length() != 0) {
                warningSb.append(" ");
                warningSb.append(resourceBundleManager.getText(
                        "blogpost.create.success.warning.blogs.moreover", locale,
                        blogWarningSb.toString()));
            } else {
                String blogWarning = resourceBundleManager.getText(
                        "blogpost.create.success.warning.blogs", locale, blogWarningSb.toString());
                blogWarning = resourceBundleManager.getText("blogpost.create.success.warning",
                        locale, blogWarning);
                warningSb.append(blogWarning);
            }
        }
        if (!result.getTagsWithProblems().isEmpty()) {
            warningSb.append(resourceBundleManager.getText(
                    "blogpost.create.success.warning.tags.alone." + (warningSb.length() == 0),
                    locale, StringUtils.join(result.getTagsWithProblems(), ",")));
        }
        if (warningSb.length() != 0) {
            return warningSb.toString();
        }
        return null;
    }

    /**
     * Creates a feedback message after post creation / update that contains errors or warnings. If
     * no error or warning occurred null is returned.
     * 
     * @param result
     *            the result object returned by modification service method
     * @param locale
     *            the locale to use for the message
     * @return the message or null
     */
    public static String getFeedbackMessageAfterModification(NoteModificationResult result,
            Locale locale) {
        String message;
        if (result.getStatus().equals(NoteModificationStatus.SUCCESS)) {
            message = createWarningMessage(result, locale);
        } else {
            message = createErrorMessage(result, locale);
        }
        return message;
    }

    /**
     * Helper for setting the default fail level on post creation.
     * 
     * @param storingTO
     *            the post storing TO to modify
     */
    static public void setDefaultFailLevel(NoteStoringTO storingTO) {
        if (storingTO != null) {
            NoteStoringFailDefinition def = storingTO.getFailDefinition();
            if (def == null) {
                def = new NoteStoringFailDefinition();
                storingTO.setFailDefinition(def);
            }
            def.setFailOnMissingBlogWriteAccess(false);
            def.setFailOnUninformableUser(false);
            def.setFailOnUnresolvableBlogs(false);
            def.setFailOnUnresolvableUsers(false);
        }
    }

    /**
     * private constructor to avoid initialization.
     */
    private CreateBlogPostHelper() {

    }

}
