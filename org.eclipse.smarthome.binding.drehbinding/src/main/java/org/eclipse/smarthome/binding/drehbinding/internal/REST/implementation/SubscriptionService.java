package org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.binding.drehbinding.internal.REST.RESTIOParticipant;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionService {

    private final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    private static SubscriptionService instance;

    private static final String POOL_NAME = "DrehbindingSubscriptionPool";
    ExecutorService schedular;

    private final Map<RESTIOParticipant, String> subscriptions;
    private boolean shutdown = false;
    private boolean running;
    private final Lock lock;
    private int callbackPort = -1;

    Runnable testListener = new Runnable() {

        @Override
        public void run() {
            // while (true) {
            // logger.debug("I'm running!!!!");
            // logger.debug("Gambling for event!");
            // Random r = new Random();
            // if (r.nextInt(10) >= 8) {
            // logger.debug("Simulated Event");
            // notifyAllSubscriber("meinTopic");
            // }
            //
            // try {
            // TimeUnit.SECONDS.sleep(2);
            // } catch (InterruptedException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            // }
        }
    };

    Runnable callbackListener = new Runnable() {

        @Override
        public void run() {
            running = true;
            try {
                ServerSocket callbackSocket = new ServerSocket(0);
                lock.lock();
                callbackPort = callbackSocket.getLocalPort();
                lock.unlock();

                while (!shutdown) {
                    Socket connection = callbackSocket.accept();
                    BufferedReader inFromClient = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String clientSentence = inFromClient.readLine();
                    logger.trace("Received: " + clientSentence);
                    connection.close();

                    String topic = decipherTopic(clientSentence);
                    Map<String, String> values = decipherValues(clientSentence);
                    notifyAllSubscriber(topic, values);

                    // GGf delay timer = overflood protection
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    private SubscriptionService() {
        lock = new ReentrantLock();
        schedular = ThreadPoolManager.getPool(POOL_NAME);
        subscriptions = new HashMap<>();

        schedular.execute(callbackListener);
    }

    public static synchronized SubscriptionService getInstance() {
        if (SubscriptionService.instance == null) {
            SubscriptionService.instance = new SubscriptionService();
        }

        return SubscriptionService.instance;
    }

    public synchronized void addSubscription(RESTIOParticipant participant, String topic) {
        subscriptions.put(participant, topic);
    }

    public synchronized void shutdown() {
        this.shutdown = true;
    }

    private void notifyAllSubscriber(String topic, Map<String, String> values) {
        for (Entry<RESTIOParticipant, String> entry : subscriptions.entrySet()) {
            RESTIOParticipant participant = entry.getKey();
            String subscribedTopic = entry.getValue();

            if (subscribedTopic.equals(topic)) {
                participant.onSubcriptionEvent(topic, values);
            }
        }
    }

    public int getCallbackPort() {
        int callbackPort = -1;
        lock.lock();
        callbackPort = this.callbackPort;
        lock.unlock();
        return callbackPort;
    }

    private String decipherTopic(String crypticText) {
        return crypticText.substring("topic:".length(), crypticText.indexOf(";"));
    }

    private Map<String, String> decipherValues(String crypticText) {
        Map<String, String> values = new HashMap<>();
        crypticText = crypticText.substring(crypticText.indexOf(";") + 1, crypticText.length());
        logger.debug("CrypticText without topic:{}", crypticText);

        while (crypticText.length() > 0) {
            logger.debug("Index: " + crypticText.indexOf(":"));
            String param = crypticText.substring(0, crypticText.indexOf(":"));
            String value = crypticText.substring(crypticText.indexOf(":") + 1, crypticText.indexOf(";"));

            values.put(param, value);

            crypticText = crypticText.substring(crypticText.indexOf(";") + 1, crypticText.length());
        }

        return values;
    }

}