package org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    private final Map<String, List<RESTIOParticipant>> subscriptions;

    // private final Map<RESTIOParticipant, String> subscriptions;
    private boolean shutdown = false;
    private final Lock lock;
    private int callbackPort = -1;

    Runnable callbackListener = new Runnable() {

        @Override
        public void run() {
            ServerSocket callbackSocket = null;
            Socket connection = null;
            try {
                callbackSocket = new ServerSocket(0);
                lock.lock();
                callbackPort = callbackSocket.getLocalPort();
                lock.unlock();

                while (!shutdown) {
                    connection = callbackSocket.accept();
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
            } finally {
                if (connection != null && !connection.isClosed()) {
                    try {
                        connection.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (callbackSocket != null && !callbackSocket.isClosed()) {
                    try {
                        callbackSocket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
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
        // subscriptions.put(participant, topic);

        if (!subscriptions.containsKey(topic)) {
            subscriptions.put(topic, new LinkedList<RESTIOParticipant>());
        }

        List<RESTIOParticipant> subscriber = subscriptions.get(topic);
        if (!subscriber.contains(participant)) {
            subscriber.add(participant);
        }
    }

    public synchronized void shutdown() {
        this.shutdown = true;
    }

    private void notifyAllSubscriber(String topic, Map<String, String> values) {
        // for (Entry<RESTIOParticipant, String> entry : subscriptions.entrySet()) {
        // RESTIOParticipant participant = entry.getKey();
        // String subscribedTopic = entry.getValue();
        //
        // if (subscribedTopic.equals(topic)) {
        // participant.onSubcriptionEvent(topic, values);
        // }
        // }

        List<RESTIOParticipant> subscriber = subscriptions.get(topic);
        for (RESTIOParticipant participant : subscriber) {
            participant.onSubcriptionEvent(topic, values);
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
        logger.trace("CrypticText without topic:{}", crypticText);

        while (crypticText.length() > 0) {
            logger.trace("Index: " + crypticText.indexOf(":"));
            String param = crypticText.substring(0, crypticText.indexOf(":"));
            String value = crypticText.substring(crypticText.indexOf(":") + 1, crypticText.indexOf(";"));

            values.put(param, value);

            crypticText = crypticText.substring(crypticText.indexOf(";") + 1, crypticText.length());
        }

        return values;
    }

}