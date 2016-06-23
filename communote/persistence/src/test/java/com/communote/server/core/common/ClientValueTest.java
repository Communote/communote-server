package com.communote.server.core.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;

/**
 * Test the client value functionality
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ClientValueTest {

    private static long NEXT_ID = 1;

    /**
     * clients to be used throughout the test
     */
    private static Map<String, ClientTO> clients = new HashMap<String, ClientTO>();

    public static synchronized void setClientAsCurrent(String clientId) {
        ClientTO client = clients.get(clientId);
        if (client == null) {
            client = new ClientTO();
            client.setClientId(clientId);
            client.setId(NEXT_ID++);
            client.setName(clientId);
            clients.put(clientId, client);
        }
        ClientAndChannelContextHolder.setClient(client);
    }

    private void checkForSuccess(List<SetCheckValue> runs) {
        for (SetCheckValue run : runs) {
            Assert.assertNull(run.getException(), run.getException() == null ? "" : run
                    .getException().toString());
            Assert.assertTrue(run.isSuccess());
        }
    }

    private void runAll(List<SetCheckValue> runs)
            throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (SetCheckValue run : runs) {
            executorService.execute(run);
        }
        executorService.shutdown();

        long wait = 10 * DateUtils.MILLIS_PER_SECOND;
        while (wait > 0 && !executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
            wait -= 500;
        }

        Assert.assertTrue(wait > 0);
    }

    /**
     * Test it non parallel
     */
    @Test
    public void testNonParallel() {

        ClientValue<Integer> value = new ClientValue<>();

        for (int i = 0; i < 100; i++) {
            setClientAsCurrent("" + i);
            value.setValue(i);
        }
        for (int i = 0; i < 100; i++) {
            setClientAsCurrent("" + i);

            Assert.assertNotNull(value.getValue());
            Assert.assertEquals(value.getValue().intValue(), i);
        }
    }

    /**
     * Test it with parallel threads
     * 
     * @throws Exception
     */
    @Test
    public void testThreaded() throws Exception {

        final ClientValue<Integer> value = new ClientValue<>();

        List<SetCheckValue> runs = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            runs.add(new SetCheckValue(value, i, false));
        }
        runAll(runs);
        checkForSuccess(runs);
        for (int i = 0; i < 500; i++) {
            runs.add(new SetCheckValue(value, i, true));
        }

        runAll(runs);
        checkForSuccess(runs);
    }
}
