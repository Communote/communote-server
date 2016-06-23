package com.communote.common.virusscan.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.virusscan.exception.VirusScannerException;

/**
 * TCP / IP Connector to the clamav daemon
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
class ClamAVDaemonConnector {

    private static final byte[] INIT_COMMAND = { 'S', 'T', 'R', 'E', 'A', 'M', '\n' };

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClamAVDaemonConnector.class);

    private Socket protocolSocket = null;
    private Socket dataSocket = null;
    private String message = "";
    private int dataPort = -1;

    private int connectionTimeout = 90;
    private final String clamdHost;
    private final int clamdPort;

    /**
     * ClamAVDaemonConnector constructor
     * 
     * @param clamdHost
     *            Host to the clamav daeon
     * @param clamdPort
     *            Port of daemon
     * @param connectionTimeout
     *            Connection timeout
     */
    public ClamAVDaemonConnector(String clamdHost, int clamdPort, int connectionTimeout) {
        this.clamdHost = clamdHost;
        this.clamdPort = clamdPort;
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Close the channel to the daemon
     */
    private void closeChannels() {
        if (protocolSocket != null) {
            try {
                protocolSocket.close();
            } catch (IOException e) {
                LOGGER.debug("Error closing protocol channel", e);
            }
        }
        if (dataSocket != null) {
            try {
                dataSocket.close();
            } catch (IOException e) {
                LOGGER.debug("Error closing data channel", e);
            }
        }
    }

    /**
     * Open TCP/IP channel to clamav daemon
     * 
     * @throws VirusScannerException
     *             Will throws when the connection fails
     */
    private void openProtocolChannel() throws VirusScannerException {

        String serverResponse = "";
        protocolSocket = new Socket();
        SocketAddress sockaddr = new InetSocketAddress(clamdHost, clamdPort);
        try {
            protocolSocket.setSoTimeout(connectionTimeout * 1000);
        } catch (SocketException e) {
            throw new VirusScannerException(
                    "Could not set timeout parameter to configurationSocket", e);
        }

        try {
            // First, try to connect to the clamd
            protocolSocket.connect(sockaddr);
            protocolSocket.getOutputStream().write(INIT_COMMAND); // Write the initialisation
            // command

            // Now, read byte per byte until we find a LF.
            byte[] received = new byte[1];
            while (true) {
                protocolSocket.getInputStream().read(received);
                if (received[0] == '\n') {
                    break;
                }
                serverResponse += new String(received);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Channel request response: " + serverResponse);
            }

            // In the response value, there's an integer.
            // It's the TCP port that the clamd server has allocated for us for the actual data
            // stream.
            if (serverResponse.contains(" ")) {
                dataPort = Integer.parseInt(serverResponse.split(" ")[1]);
            } else {
                throw new VirusScannerException("Could not find data port, server's response is "
                        + serverResponse);
            }
        } catch (NumberFormatException e) {
            throw new VirusScannerException(
                    "Could not parse the server port to connect to in response: "
                            + serverResponse);
        } catch (IOException e) {
            throw new VirusScannerException("Error while requesting protocol channel", e);
        }
    }

    /**
     * Will block until scan is performed
     * 
     * @param inputStream
     *            Input Stream
     * @return Returns <code>true</code> when no virus was found, else <code>false</code>
     * @throws VirusScannerException
     *             Encapsulate all exception
     */
    public boolean performScan(InputStream inputStream) throws VirusScannerException {
        try {
            openProtocolChannel();
            requestScan(inputStream);
            if ((message == null)) {
                throw new VirusScannerException("Clamd responded with an empty message.");
            }
            return message.equals("stream: OK");
        } catch (VirusScannerException e) {
            LOGGER.debug(e.getMessage(), e);
            throw e;
        } finally {
            closeChannels();
        }
    }

    /**
     * Scan the input stream by clamav
     * 
     * @param inputStream
     *            Stream for test
     * @throws VirusScannerException
     *             Throws when the connection fails
     */
    private void requestScan(InputStream inputStream) throws VirusScannerException {
        byte[] received = new byte[1];
        dataSocket = new Socket();
        SocketAddress sockaddrData = new InetSocketAddress(clamdHost, dataPort);
        try {
            dataSocket.setSoTimeout(connectionTimeout * 1000);
        } catch (SocketException e) {
            throw new VirusScannerException("Could not set timeout parameter to dataSocket", e);
        }
        try {
            dataSocket.connect(sockaddrData);
            IOUtils.copy(inputStream, dataSocket.getOutputStream()); // Write to the data stream
            // the content of the mail
            dataSocket.close(); // Then close the stream, to let clamd know it's the end of the
            // stream.
            inputStream.close();
        } catch (IOException e) {
            throw new VirusScannerException("Error while initializing clamd data channel", e);
        }

        // Wait for the response on the chat stream.
        while (true) {
            try {
                protocolSocket.getInputStream().read(received);
            } catch (IOException e) {
                throw new VirusScannerException("Error while waiting for clamd response", e);
            }
            if (received[0] == '\n') {
                break;
            }
            message += new String(received);
        }
        LOGGER.debug("response: {}", message);
    }
}
