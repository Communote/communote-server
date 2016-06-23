package com.communote.common.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FileHelper {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileHelper.class);

    /**
     * Adapted code from http://stackoverflow.com/a/7322581/1165132 <br>
     * Inspired by http://forums.sun.com/thread.jspa?threadID=572557
     *
     * @param pathToFile
     *            Path to the file.
     * @param lines
     *            Number of lines to read,
     * @return The lines as String array.
     *
     */
    public static String[] tail(String pathToFile, int lines) {
        lines++;
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile(pathToFile, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();
            int line = 0;

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) { // \n
                    line++;
                    if (line == lines) {
                        if (filePointer == fileLength) {
                            continue;
                        } else {
                            break;
                        }
                    }
                } else if (readByte == 0xD) { // \r
                    if (line == lines) {
                        if (filePointer == fileLength - 1) {
                            continue;
                        } else {
                            break;
                        }
                    }
                }
                sb.append((char) readByte);
            }
            String lastLine = sb.reverse().toString();
            return lastLine.trim().split("\n");
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        } finally {
            IOHelper.close(fileHandler);
        }
        return new String[0];
    }

    /**
     * Validates that the resolved directory exists and is a directory. If it not exists it is
     * created.
     *
     * @param dir
     *            the directory to validate
     * @return the validated directory
     * @throws FileNotFoundException
     *             if the validation failed
     */
    public static File validateDir(File dir) throws FileNotFoundException {
        if (!dir.exists() && !dir.mkdirs()) {
            String errorMsg = "Creating the directory " + dir.getAbsolutePath() + " failed";
            LOGGER.error(errorMsg);
            throw new FileNotFoundException(errorMsg);
        } else if (!dir.isDirectory()) {
            String errorMsg = "The specified directory " + dir.getAbsolutePath()
                    + " is not a directory";
            LOGGER.error(errorMsg);
            throw new FileNotFoundException(errorMsg);
        }
        return dir;
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private FileHelper() {
        // Do nothing
    }
}