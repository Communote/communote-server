package com.communote.server.web.fe.widgets.admin.application.logging;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.communote.common.io.FileHelper;
import com.communote.common.util.Pair;
import com.communote.server.core.common.util.LogHelper;
import com.communote.server.widgets.AbstractWidget;

/**
 * Widget to display log file content
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class LoggingWidget extends AbstractWidget {

    /**
     * Extract last 1000 entries of log files
     *
     * @return List of log files and their content.
     */
    private Map<String, Pair<String, String>> extractLogFiles() {
        Map<String, String> locations = LogHelper.getLoggingLocations();
        Map<String, Pair<String, String>> logs = new HashMap<String, Pair<String, String>>();
        if (locations != null && locations.size() > 0) {
            for (Entry<String, String> location : locations.entrySet()) {
                String generateLogList = generateLogList(location.getKey());
                if (generateLogList != null) {
                    logs.put(location.getValue(),
                            new Pair<String, String>(new File(location.getKey()).getName(),
                                    generateLogList));
                }
            }
        }
        return logs;
    }

    /**
     * Extract last 1000 entries for a given log file location
     *
     * @param location
     *            of log file
     * @return Last 1000 entries
     */
    private String generateLogList(String location) {
        if (location == null) {
            return null;
        }
        String[] logs = FileHelper.tail(location, 1000);
        if (logs == null || logs.length == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (String log : logs) {
            result.append(log + "\n");
        }
        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTile(String arg0) {
        return "widget.admin.application.logging.overview";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handleRequest() {
        Map<String, Pair<String, String>> logLists = extractLogFiles();
        Map<String, String> logTabNames = new HashMap<String, String>();
        Map<String, String[]> logPanelNames = new HashMap<String, String[]>();

        getRequest().setAttribute("logFiles", logLists);
        int count = 0;
        for (Entry<String, Pair<String, String>> logFile : logLists.entrySet()) {
            String id = "Tab" + count++;
            logTabNames.put(id, new File(logFile.getValue().getLeft()).getName());
            String[] content = new String[2];
            content[0] = logFile.getValue().getRight();
            content[1] = logFile.getKey();
            logPanelNames.put(id + "Panel", content);
        }
        getRequest().setAttribute("tabNames", logTabNames);
        getRequest().setAttribute("panelNames", logPanelNames);
        return null;
    }

    /**
     * Do nothing
     */
    @Override
    protected void initParameters() {
        // Do nothing
    }

}
