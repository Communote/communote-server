package com.communote.server.core.blog.helper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.access.AccessDeniedException;

import com.communote.common.validation.EmailValidator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.blog.BlogIdentifierValidationException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.UserBlogData;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.core.security.SecurityHelper;

/**
 * Class with helper methods related to blog management<br>
 * When changing this class name or package take care of the full text search definition, since this
 * method is used for dynamic data creation.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class BlogManagementHelper {

    @SuppressWarnings("unchecked")
    private static final Comparator<Object> TITLE_COMPARATOR = new BeanComparator("title");

    /**
     * Regular expression for the name identifier of a topic
     */
    public final static String REG_EXP_TOPIC_NAME_IDENTIFIER = "[a-zA-Z0-9\\-\\_\\.]*[a-zA-Z0-9]+";

    /**
     * @return true if the current user may set the all can read write flag
     */
    public static boolean canSetAllCanReadWrite() {
        return SecurityHelper.isClientManager()
                || CommunoteRuntime.getInstance().getConfigurationManager()
                        .getClientConfigurationProperties()
                .getProperty(ClientProperty.ALLOW_ALL_CAN_READ_WRITE_FOR_ALL_USERS, true);
    }

    /**
     * Convert the exception to an {@link AccessDeniedException}. Needed to not break existing
     * behavior after removing old assertBlogAccess(Blog) method. This method should not not be used
     * when writing new backend code.
     *
     * @param ba
     *            the exception to convert
     * @return the converted runtime exception
     */
    public static AccessDeniedException convertException(BlogAccessException ba) {
        return new AccessDeniedException("Current User " + SecurityHelper.getCurrentUserId()
                + " is not allowed to access topic " + ba.getBlogId() + " with role "
                + ba.getRequiredRole(), ba);
    }

    /**
     * Convert the exception to an {@link AccessDeniedException} Needed to not break existing
     * behavior after removing old assertBlogAccess(Blog) method. This method should not not be used
     * when writing new backend code.
     *
     * @param nf
     *            the exception to convert
     * @return the converted runtime exception
     */
    public static AccessDeniedException convertException(BlogNotFoundException nf) {
        return new AccessDeniedException("Blog not found for topicId:" + nf.getBlogId(), nf);
    }

    /**
     * Gets the blog limit.
     *
     * @return the limit
     */
    public static long getCountLimit() {
        return CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.CLIENT_BLOG_COUNT_LIMIT,
                        ClientConfigurationHelper.DEFAULT_CLIENT_BLOG_COUNT_LIMIT);
    }

    /**
     * Sort a list of topics alphabetically by the title
     *
     * @param blogList
     *            the list of blog items
     * @return the sorted list of blogs
     */
    public static <E extends BlogData> List<E> sortedBlogList(List<E> blogList) {
        Collections.sort(blogList, TITLE_COMPARATOR);
        return blogList;
    }

    /**
     * Returns the list of user blog list items whose blog titles are localized.
     *
     * @param userBlogListItem
     *            the list of user blog list items
     * @return the list of user blog list items with the localized blog title
     */
    public static List<UserBlogData> sortedUserBlogList(List<UserBlogData> userBlogListItem) {
        Collections.sort(userBlogListItem, TITLE_COMPARATOR);
        return userBlogListItem;
    }

    /**
     * Validate if the given identifier can be used as topic/blog alias
     *
     * @param identifier
     *            the identifier to check
     * @throws BlogIdentifierValidationException
     *             thrown if the identifier is not valid
     */
    public static void validateNameIdentifier(String identifier)
            throws BlogIdentifierValidationException {
        if (identifier.length() > EmailValidator.MAX_SAFE_LENGTH_LOCAL_PART) {
            throw new BlogIdentifierValidationException("Blog alias is too long. Alias provided: "
                    + identifier, identifier);
        }
        if (StringUtils.isBlank(identifier)) {
            throw new BlogIdentifierValidationException("Blog alias is empty. Alias provided: "
                    + identifier, identifier);
        }
        // validate against pattern
        Pattern pattern = Pattern.compile(REG_EXP_TOPIC_NAME_IDENTIFIER);
        Matcher matcher = pattern.matcher(identifier);
        if (!matcher.matches()) {
            throw new BlogIdentifierValidationException(
                    "Blog identifier contains unsupported cahracters. Alias provided: "
                            + identifier, identifier);
        }
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private BlogManagementHelper() {
        // Do nothing
    }
}
