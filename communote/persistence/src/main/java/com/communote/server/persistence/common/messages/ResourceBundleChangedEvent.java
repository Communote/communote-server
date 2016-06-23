package com.communote.server.persistence.common.messages;

import com.communote.server.api.core.i18n.LocalizationChangedEvent;

/**
 * Event to inform about a change in the application resource bundle resulting form add or removal
 * of localizations.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ResourceBundleChangedEvent extends LocalizationChangedEvent {

    private static final long serialVersionUID = 1L;

}
