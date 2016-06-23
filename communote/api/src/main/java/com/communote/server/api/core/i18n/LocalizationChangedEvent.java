package com.communote.server.api.core.i18n;

import com.communote.server.api.core.event.Event;

/**
 * Generic event to inform about changes in a localization providing source like a resource bundle.
 * This event should be triggered when new new localizations are added or existing localizations are
 * removed or modified.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LocalizationChangedEvent implements Event {

    private static final long serialVersionUID = 2176945326054043526L;

}
