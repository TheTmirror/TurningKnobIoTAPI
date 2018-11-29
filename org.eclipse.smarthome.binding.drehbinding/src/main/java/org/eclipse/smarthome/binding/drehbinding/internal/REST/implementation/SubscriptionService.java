package org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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

    Runnable testListener = new Runnable() {

        @Override
        public void run() {
            while (true) {
                logger.debug("I'm running!!!!");
                logger.debug("Gambling for event!");
                Random r = new Random();
                if (r.nextInt(10) >= 8) {
                    logger.debug("Simulated Event");
                    notifyAllSubscriber("meinTopic");
                }

                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    };

    Runnable callbackListener = new Runnable() {

        @Override
        public void run() {
            running = true;
            try {
                ServerSocket callbackSocket = new ServerSocket(2307);

                while (!shutdown) {
                    Socket connection = callbackSocket.accept();
                    BufferedReader inFromClient = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String clientSentence = inFromClient.readLine();
                    logger.debug("Received: " + clientSentence);
                    connection.close();

                    TimeUnit.SECONDS.sleep(5);
                }
            } catch (IOException | InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    private SubscriptionService() {
        schedular = ThreadPoolManager.getPool(POOL_NAME);
        subscriptions = new HashMap<>();

        schedular.execute(testListener);
    }

    public static synchronized SubscriptionService getInstance() {
        if (SubscriptionService.instance == null) {
            SubscriptionService.instance = new SubscriptionService();
        }

        return SubscriptionService.instance;
    }

    public void addSubscription(RESTIOParticipant participant, String topic) {
        subscriptions.put(participant, topic);
    }

    public void shutdown() {
        this.shutdown = true;
    }

    private void notifyAllSubscriber(String topic) {
        for (Entry<RESTIOParticipant, String> entry : subscriptions.entrySet()) {
            RESTIOParticipant participant = entry.getKey();
            String subscribedTopic = entry.getValue();

            if (subscribedTopic.equals(topic)) {
                participant.onSubcriptionEvent(topic, null);
            }
        }
    }

}