package com.communote.server.core.messaging.connectors.xmpp;

import org.jivesoftware.smack.packet.Message;
import org.springframework.security.core.context.SecurityContext;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.client.ClientDelegateCallback;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.core.blog.OnlyCrosspostMarkupException;
import com.communote.server.core.blog.notes.processors.exceptions.DirectMessageMissingRecipientException;
import com.communote.server.core.delegate.client.DelegateCallbackException;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.CreateBlogPostHelper;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.service.NoteService;

/**
 * Simple {@link ClientDelegateCallback} for posting the message to the correct client.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class XMPPDatabaseCallback implements ClientDelegateCallback<Object> {

    private final Message message;
    private final AliasPacketExtension alias;

    /**
     * Constructor for {@link XMPPDatabaseCallback}.
     * 
     * @param message
     *            The message.
     * @param alias
     *            The alias representing the blog.
     */
    public XMPPDatabaseCallback(Message message, AliasPacketExtension alias) {
        this.message = message;
        this.alias = alias;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object doOnClient(ClientTO client) throws DelegateCallbackException {
        User sender = XMPPPatternUtils.extractKenmeiUserWithoutDatabaseDelegate(message
                .getFrom());
        NoteStoringTO post = new NoteStoringTO();
        CreateBlogPostHelper.setDefaultFailLevel(post);
        post.setPublish(true);
        post.setVersion(0L);
        post.setCreationSource(NoteCreationSource.XMPP);
        post.setSendNotifications(true);
        NoteModificationResult result;
        SecurityContext currentContext = null;
        try {
            post.setBlogId(XMPPPatternUtils.extractBlogId(alias.getValue()));
            post.setContent(message.getBody());
            post.setContentType(NoteContentType.PLAIN_TEXT);
            post.setCreatorId(sender.getId());
            currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
            result = ServiceLocator.instance().getService(NoteService.class).createNote(post, null);
        } catch (OnlyCrosspostMarkupException e) {
            throw new DelegateCallbackException(ResourceBundleManager.instance().getText(
                    "error.blogpost.create.no.real.content", sender.getLanguageLocale()), e);
        } catch (BlogNotFoundException e) {
            throw new DelegateCallbackException(ResourceBundleManager.instance().getText(
                    "xmpp.message.wrong.blog", sender.getLanguageLocale()), e);
        } catch (NoteManagementAuthorizationException e) {
            throw new DelegateCallbackException(ResourceBundleManager.instance().getText(
                    "xmpp.message.wrong.blog", sender.getLanguageLocale()), e);
        } catch (DirectMessageMissingRecipientException e) {
            throw new DelegateCallbackException(e.getLocalizedMessage(sender.getLanguageLocale()),
                    e);
        } catch (NoteStoringPreProcessorException e) {
            throw new DelegateCallbackException(ResourceBundleManager.instance().getText(
                    "xmpp.message.error.convertion", sender.getLanguageLocale()), e);
        } finally {
            AuthenticationHelper.setSecurityContext(currentContext);
        }
        return result;
    }
}
