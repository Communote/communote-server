package com.communote.server.core.common.util;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.core.vo.query.Query;
import com.communote.server.core.vo.query.post.NoteQuery;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LogHelper {

    /** Logger to track performance. */
    public static final org.slf4j.Logger PERF_LOG = LoggerFactory.getLogger("performanceLog");

    /**
     * Find registered log file appender
     * 
     * @return Map of log file locations and appender names
     */
    public static Map<String, String> getLoggingLocations() {
        Map<String, String> locations = new HashMap<String, String>();
        Logger root = Logger.getRootLogger();
        if (root == null) {
            return locations;
        }

        for (Enumeration<Appender> allAppender = root.getAllAppenders(); allAppender
                .hasMoreElements();) {
            Appender appender = allAppender.nextElement();
            if (appender instanceof FileAppender) {
                locations.put(((FileAppender) appender).getFile(), appender.getName());
            }
        }

        for (Enumeration<Logger> loggers = root.getLoggerRepository().getCurrentLoggers(); loggers
                .hasMoreElements();) {
            for (Enumeration<Appender> appenders = loggers.nextElement().getAllAppenders(); appenders
                    .hasMoreElements();) {
                Appender appender = appenders.nextElement();
                if (appender instanceof FileAppender) {
                    locations.put(((FileAppender) appender).getFile(), appender.getName());
                }
            }
        }
        File startupLog = new File(System.getProperty("user.dir") + File.separator
                + "communote-startup.log");
        if (startupLog.exists()) {
            locations.put(startupLog.getAbsolutePath(), "startup-log.log");
        }
        return locations;
    }

    /**
     * Log performance if its enabled, and its a definition we want to check
     * 
     * @param queryDefinition
     *            the definition to use for checking
     * @return true to log the performance
     */
    public static boolean logPerformance(Query<?, ?> queryDefinition) {
        return LogHelper.PERF_LOG.isDebugEnabled()
                && queryDefinition instanceof NoteQuery;
    }

    /**
     * Log the stop watch
     * 
     * @param message
     *            the message, preferably the class name and method
     * @param stopWatch
     *            the stop time to watch
     */
    public static void logPerformance(String message, StopWatch stopWatch) {
        PERF_LOG.debug("{} took: {} in ms: {}"
                ,
                new Object[] { message, stopWatch, stopWatch.getTime() });
    }

    /**
     * Default log helper
     */
    private LogHelper() {

    }
}
