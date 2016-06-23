package com.communote.server.api.core.event;

import java.io.Serializable;

/**
 * Defines an event. An event can be triggered by using the {@link EventDispatcher} and observed by
 * registering an appropriate {@link EventListener} to the {@link EventDispatcher}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface Event extends Serializable {

}
