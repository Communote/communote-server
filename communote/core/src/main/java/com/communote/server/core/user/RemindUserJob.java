package com.communote.server.core.user;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.ClientDelegateCallback;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.task.TaskHandler;
import com.communote.server.api.core.task.TaskHandlerException;
import com.communote.server.api.core.task.TaskTO;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Job to send reminder mails to user.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RemindUserJob implements TaskHandler {

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(RemindUserJob.class);

    /**
     * {@inheritDoc}
     *
     * @return <code>null</code>
     */
    @Override
    public Date getRescheduleDate(Date now) {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void run(TaskTO task) throws TaskHandlerException {
        try {
            // only send on global client
            // TODO why?
            ClientTO client = ServiceLocator.findService(ClientRetrievalService.class).findClient(
                    ClientHelper.getGlobalClientId());
            new ClientDelegate(client).execute(new ClientDelegateCallback<Object>() {
                @Override
                public Object doOnClient(ClientTO client) throws Exception {
                    ServiceLocator.instance().getService(UserManagement.class).sendReminderMails();
                    return null;
                }
            });
        } catch (Exception e) {
            LOG.error("error in send remind mails job", e);
        }
    }
}
