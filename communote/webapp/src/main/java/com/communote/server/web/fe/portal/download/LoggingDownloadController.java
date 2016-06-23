package com.communote.server.web.fe.portal.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.server.core.common.util.LogHelper;

/**
 * Response a requested log file
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class LoggingDownloadController implements Controller {

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String logfile = request.getParameter("logfile");
        Map<String, String> loggingLocations = LogHelper.getLoggingLocations();
        if (!loggingLocations.containsValue(logfile)) {
            return null;
        }
        for (Entry<String, String> location : loggingLocations.entrySet()) {
            if (location.getValue().equals(logfile)) {
                logfile = location.getKey();
                break;
            }
        }
        File file = new File(logfile);

        if (file.exists() && file.isFile() && file.canRead()) {
            Writer responseWriter = response.getWriter();

            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            while (bufferedReader.ready()) {
                responseWriter.append(bufferedReader.readLine() + "\n");
            }

            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();
        }

        return null;
    }
}
