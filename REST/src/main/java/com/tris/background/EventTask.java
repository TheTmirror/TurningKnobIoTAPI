package com.tris.background;

import static org.tris.internal.Constants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.tris.internal.Event;
import org.tris.internal.Subscription;
import org.tris.internal.SubscriptionManager;

import com.tris.REST.SubscriptionService;
import com.tris.REST.SubscriptionServiceImpl;

public class EventTask implements ServletContextListener {

	private boolean shutdown = false;
	private Runnable smLogger;
	private Runnable task;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println("Now I can do something!");
		smLogger = new Runnable() {

			@Override
			public void run() {
				SubscriptionManager sm = SubscriptionManager.getInstance();

				while (!shutdown) {
					for(Entry<String, Map<String, Subscription>> e : SubscriptionManager.getInstance().getCopyOfSubs().entrySet()) {
						String topic = e.getKey();
						Map<String, Subscription> subsForTopicX = e.getValue();
						
						for(Entry<String, Subscription> e2 : subsForTopicX.entrySet()) {
							String subscriberIdentifier = e2.getKey();
							Subscription sub = e2.getValue();
							
							System.out.println(subscriberIdentifier + " has subscribed to topic " + topic);
						}
					}

					try {
						TimeUnit.SECONDS.sleep(5);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}

			}
		};

		task = new Runnable() {

			@Override
			public void run() {
				Scanner input = new Scanner(System.in);
				while (true) {
					String in = input.nextLine();
					
					Map<String, String> values = new HashMap<>();
					
					String param;
					while(!(param = input.nextLine()).equals(";")) {
						String value = input.nextLine();
						values.put(param, value);
					}

					Event event = new Event(in, values);
					
					SubscriptionService subService = new SubscriptionServiceImpl();
					subService.onEvent(event);
				}
			}

		};

		new Thread(smLogger).start();
		new Thread(task).start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		System.out.println("Context was destroyed");
		shutdown = true;
	}

}
