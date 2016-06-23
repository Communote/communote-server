package com.communote.server.web.commons.helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.time.SimplifiedTimeZone;
import com.communote.server.core.user.MasterDataManagement;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RegistrationHelper {

    /**
     * Builds a java script object containing the rawOffset as key and the time zone id as value.
     *
     * @return the list as string
     */
    public static String buildTimeZoneOffsetList() {
        List<SimplifiedTimeZone> timeZoneList = ServiceLocator.findService(MasterDataManagement.class)
                .getTimeZones();

        Map<Integer, String> rawOffsetList = new HashMap<Integer, String>();

        for (SimplifiedTimeZone timeZone : timeZoneList) {
            Integer offset = TimeZone.getTimeZone(timeZone.getTimeZoneId()).getRawOffset();

            if (offset != null && !rawOffsetList.containsKey(offset)) {
                rawOffsetList.put(offset, timeZone.getTimeZoneId());
            }
        }

        StringBuilder list = new StringBuilder("{");
        String suffix = "'";

        Iterator<?> it = rawOffsetList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<?, ?> pairs = (Map.Entry<?, ?>) it.next();
            list.append(suffix);
            list.append("O");
            list.append(pairs.getKey());
            list.append("':'");
            list.append(pairs.getValue());
            list.append("'");
            suffix = ",'";
        }

        list.append("}");
        return list.toString();
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private RegistrationHelper() {
        // Do nothing
    }
}
