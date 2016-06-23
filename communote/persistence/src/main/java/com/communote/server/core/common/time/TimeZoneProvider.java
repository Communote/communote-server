package com.communote.server.core.common.time;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.communote.server.persistence.common.messages.ResourceBundleManager;


/**
 * The TimeZoneProvider.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TimeZoneProvider {

    private final static Logger LOG = Logger.getLogger(TimeZoneProvider.class);

    private static final String MSG_KEY_PREFIX = "time.zones.gmt.";

    /**
     * The comparator to use for sorting the resulting items. If null they will not be sorted.
     * 
     * @return The comparator
     */
    private static Comparator<Object> getComparator() {
        Collator primaryCollator = Collator.getInstance(Locale.ENGLISH);
        primaryCollator.setStrength(Collator.IDENTICAL);

        ComparatorChain chain = new ComparatorChain();
        chain.addComparator(new BeanComparator("rawOffset"));
        chain.addComparator(new BeanComparator("sortingName", primaryCollator));
        return chain;
    }

    private final List<SimplifiedTimeZone> timeZones = new ArrayList<SimplifiedTimeZone>();

    /**
     * Constructor.
     */
    public TimeZoneProvider() {
        initTimeZoneList();
    }

    /**
     * Finds a SimplifiedTimeZone by the time zone id.
     * 
     * @param timeZoneId
     *            the time zone id to find within the existing list
     * @return the SimplifiedTimeZone for the given time zone id
     */
    public SimplifiedTimeZone getKenmeiTimeZone(String timeZoneId) {
        SimplifiedTimeZone tz = null;
        for (SimplifiedTimeZone timeZone : getTimeZones()) {
            if (timeZone.getTimeZoneId().equals(timeZoneId)) {
                return timeZone;
            }
        }
        return tz;
    }

    /**
     * @return the timeZones
     */
    public List<SimplifiedTimeZone> getTimeZones() {
        return timeZones;
    }

    /**
     * Initializes the time zone list.
     */
    private void initTimeZoneList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("start to initialize the time zone list");
        }

        ResourceBundleManager resourceManager = ResourceBundleManager.instance();

        String[] availableTimeZoneIds = TimeZone.getAvailableIDs();

        int timeZoneRawOffset = 0;
        String timeZoneSortingName = StringUtils.EMPTY;

        for (String timeZoneId : availableTimeZoneIds) {
            boolean knowsMessageKey = resourceManager.knowsMessageKey(MSG_KEY_PREFIX
                    + timeZoneId,
                    null, Locale.ENGLISH);

            if (knowsMessageKey) {
                timeZoneRawOffset = TimeZone.getTimeZone(timeZoneId).getRawOffset();
                timeZoneSortingName = StringUtils.substringAfterLast(timeZoneId, "/");
                timeZones.add(new SimplifiedTimeZone(MSG_KEY_PREFIX + timeZoneId, timeZoneId,
                        timeZoneRawOffset, timeZoneSortingName));
            }
        }

        Collections.sort(timeZones, getComparator());

        if (LOG.isDebugEnabled()) {
            LOG.debug("    the time zone list contains " + timeZones.size() + " entries ");
            LOG.debug("finish creation of the time zone list");
        }
    }
}