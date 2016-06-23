package com.communote.common.virusscan.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.io.CopyInputStreamBuffer;
import com.communote.common.virusscan.VirusScanner;
import com.communote.common.virusscan.exception.InitializeException;
import com.communote.common.virusscan.exception.TimeoutException;
import com.communote.common.virusscan.exception.VirusFoundException;
import com.communote.common.virusscan.exception.VirusScannerException;

/**
 * This class use the command line for scanning content.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommandlineScanner implements VirusScanner {

    /**
     * Thread for scan
     */
    private class ScanThread extends Thread {

        private int exitValue;
        private final String commandLine;
        private Exception exception = null;
        private boolean isFinished = false;

        /**
         * @param commandLine
         *            Command
         */
        public ScanThread(String commandLine) {
            // super();
            this.commandLine = commandLine;
        }

        /**
         * Get occurred exception
         *
         * @return An exception, which was occurred by the process
         */
        public Exception getException() {
            return exception;
        }

        /**
         * Return value of the executed process
         *
         * @return Exit value
         */
        public int getExitValue() {
            return exitValue;
        }

        /**
         * Indicates whether the current thread already finished
         *
         * @return <code>true</code> if the thread finished, otherwise <code>flase</code>
         */
        public boolean isFinished() {
            return isFinished;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            isFinished = false;
            Runtime rt = Runtime.getRuntime();
            Process process;
            try {
                process = rt.exec(commandLine);

                InputStream stdin = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(stdin);
                BufferedReader br = new BufferedReader(isr);
                String line = null;

                LOGGER.debug("<BEGIN SCAN OUTPUT>");
                while ((line = br.readLine()) != null) {
                    LOGGER.debug(line);
                }
                LOGGER.debug("<END SCAN OUTPUT>");

                exitValue = process.waitFor();
            } catch (IOException e) {
                exception = e;
            } catch (InterruptedException e) {
                exception = e;
            }
            isFinished = true;
            super.run();
        }
    }

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandlineScanner.class);
    /**
     * Command line string which will execute
     */
    public static final String COMMAND_LINE_PROP = "command.line.string";

    /**
     * Timeout of scan process
     */
    public static final String COMMAND_LINE_PROCESS_TIMEOUT_PROP = "command.line.process.timeout";

    /**
     * Exit code of the scan process
     */
    public static final String EXIT_CODE_PROP = "command.line.exit.code";

    /**
     * Directory for temporary files
     */
    public static final String TEMP_DIR_PROP = "command.line.temp.dir";

    /**
     * Name prefix for files
     */
    public static final String TEMP_FILE_PREFIX_PROP = "command.line.temp.file.prefix";

    /**
     * Name suffix for files
     */
    public static final String TEMP_FILE_SUFFIX_PROP = "command.line.temp.file.suffix";

    private String commandLine;
    private String tempDir;
    private int exitCode;
    private String tempFilenamePrefix;
    private String tempFilenameSuffix;
    private Long processTimeout;

    /**
     * This is just necessary for java reflection
     */
    public CommandlineScanner() {
    }

    /**
     * Get value of property key
     *
     * @param properties
     *            Virus Scanner Properties
     * @param key
     *            Property key
     * @return Value of the given property key
     */
    private String getProperty(Properties properties, String key) {
        String p = properties.getProperty(key);
        if (StringUtils.isEmpty(p)) {
            throw new InitializeException("The property '" + key + "' was not found");
        }
        return p;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Properties properties) throws InitializeException {
        commandLine = getProperty(properties, COMMAND_LINE_PROP);
        tempDir = getProperty(properties, TEMP_DIR_PROP);
        tempDir = (tempDir.endsWith("/")) ? (tempDir) : (tempDir + "/");
        File tempDirectory = new File(tempDir);
        if (!tempDirectory.exists()) {
            if (!tempDirectory.mkdir()) {
                throw new InitializeException("Unable to create temp directory.");
            }
        }
        tempFilenamePrefix = getProperty(properties, TEMP_FILE_PREFIX_PROP);
        tempFilenameSuffix = getProperty(properties, TEMP_FILE_SUFFIX_PROP);
        String strExitCode = getProperty(properties, EXIT_CODE_PROP);
        if (StringUtils.isNumeric(strExitCode)) {
            exitCode = Integer.parseInt(strExitCode);
        } else {
            throw new InitializeException("The value for '" + EXIT_CODE_PROP
                    + "' is not a valid integer");
        }
        String strTimeout = properties.getProperty(COMMAND_LINE_PROCESS_TIMEOUT_PROP);
        if (StringUtils.isNumeric(strTimeout)) {
            processTimeout = Long.parseLong(strTimeout) * 1000;
        } else {
            throw new InitializeException("The property '" + COMMAND_LINE_PROCESS_TIMEOUT_PROP
                    + "' is not parseable. It must be a long value.");
        }
    }

    /**
     * Scan file by command line
     *
     * @param file
     *            File
     * @return Process exit code
     * @throws IOException
     *             {@link IOException}
     * @throws InterruptedException
     *             {@link InterruptedException}
     * @throws TimeoutException
     *             {@link TimeoutException}
     * @throws VirusScannerException
     *             {@link VirusScannerException}
     */
    private int process(File file) throws IOException, InterruptedException, TimeoutException,
    VirusScannerException {
        String commandline = commandLine.replace("%f", file.getAbsolutePath());
        LOGGER.info("Excecute the following command line: '" + commandline + "'");
        ScanThread scanThread = new ScanThread(commandline);
        scanThread.start();
        long startTime = System.currentTimeMillis();
        while (!scanThread.isFinished()
                && (startTime + processTimeout > System.currentTimeMillis())) {
            ;
        }
        if (!scanThread.isFinished()) {
            scanThread.interrupt();
            throw new TimeoutException("The scan process for the command '" + commandline
                    + "' was canceled");
        }
        if (scanThread.getException() != null) {
            throw new VirusScannerException("Error occurred in command line virus scan",
                    scanThread.getException());
        }
        return scanThread.getExitValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scan(byte[] bytes) throws VirusScannerException, VirusFoundException {
        File tempFile = null;
        try {
            tempFile = CopyInputStreamBuffer.getTempFile(tempDir, tempFilenamePrefix,
                    tempFilenameSuffix);
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            outputStream.write(bytes);
            outputStream.close();
            scan(tempFile);
        } catch (IOException e) {
            throw new VirusScannerException(e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scan(File file) throws VirusScannerException, VirusFoundException {
        Integer ec = null;
        try {
            ec = process(file);
        } catch (IOException e) {
            throw new VirusScannerException(e);
        } catch (InterruptedException e) {
            throw new VirusScannerException(e);
        } catch (TimeoutException e) {
            throw new VirusScannerException(e);
        }
        if (ec != exitCode) {
            throw new VirusFoundException("A virus was found in stream.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream scan(InputStream stream) throws VirusScannerException, VirusFoundException {
        FileOutputStream output = null;
        File tempFile = null;
        CopyInputStreamBuffer copyBuffer = null;
        int ec;
        try {
            copyBuffer = new CopyInputStreamBuffer(stream, CopyInputStreamBuffer.getTempFile(
                    tempDir, tempFilenamePrefix, tempFilenameSuffix));
            tempFile = CopyInputStreamBuffer.getTempFile(tempDir, tempFilenamePrefix,
                    tempFilenameSuffix);
            output = new FileOutputStream(tempFile);
            IOUtils.copy(copyBuffer, output);
            output.close();

            ec = process(tempFile);
        } catch (Exception e) {
            throw new VirusScannerException("Unable to scan stream", e);
        } finally {
            // TODO: Read file to stream and override the close method in FileInputStream. The
            // overridden methods must delete the temporary file.
            try {
                if (copyBuffer != null) {
                    copyBuffer.close();
                }
            } catch (IOException e) {
                LOGGER.warn("Unable to close input stream buffer", e);
            }
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }

        InputStream returnStream = null;
        try {
            returnStream = copyBuffer.getInputStream();
        } catch (IOException e) {
            LOGGER.warn("Unable to return the input stream from the buffer");
        }

        if (!(ec == exitCode)) {
            if (returnStream != null) {
                try {
                    LOGGER.info("A virus was detected in stream and will be closed");
                    returnStream.close();
                } catch (IOException e) {
                    LOGGER.error("A virus was detected but the copy input stream could not be closed.");
                }
            }
            throw new VirusFoundException("A virus was found in stream.");
        }
        // if the both codes are equal then no virus was detected
        return returnStream;
    }

}
