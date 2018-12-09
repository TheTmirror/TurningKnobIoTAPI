package com.tris.ui;

import org.tris.internal.Event;
import org.tris.internal.Subscription;
import org.tris.internal.SubscriptionManager;

import com.tris.REST.SubscriptionService;
import com.tris.REST.SubscriptionServiceImpl;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

public class UIFrame extends JFrame {

	JTextArea subscriptions;

	public UIFrame() {
		setTitle("Simulation");
		setSize(1000, 620);
		setResizable(true);
		setLocation(50, 50);
		setVisible(true);

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		initUI();
	}

	private void initUI() {
		getContentPane().setLayout(new GridLayout(2, 1));

		final JTextField name = new JTextField();
		JButton gestenEvent = new JButton("Geste auslösen");
		gestenEvent.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (name.getText().equals("")) {
					return;
				}
				String topic = decipherTopic(name.getText());
				Map<String, String> values = decipherValues(name.getText());
				System.out.println(topic);

				for (Entry<String, String> val : values.entrySet()) {
					System.out.println(val.getKey() + " : " + val.getValue());
				}

				Event event = new Event(topic, values);
				
				SubscriptionService subService = new SubscriptionServiceImpl();
				subService.onEvent(event);
			}
		});

		subscriptions = new JTextArea();
		new Thread(new Runnable() {

			@Override
			public void run() {

				while (true) {

					String displayText = "Subs\n\n";
					
					for (Entry<String, Map<String, Subscription>> e : SubscriptionManager.getInstance().getCopyOfSubs()
							.entrySet()) {
						String topic = e.getKey();
						Map<String, Subscription> subsForTopicX = e.getValue();
						
						displayText = displayText + topic + ":\n";

						for (Entry<String, Subscription> e2 : subsForTopicX.entrySet()) {
							String subscriberIdentifier = e2.getKey();
							Subscription sub = e2.getValue();

							displayText = displayText + subscriberIdentifier + " : " + sub.getBootid() + "\n";
						}
						
						displayText = displayText + "\n";
					}
					
					subscriptions.setText(displayText);
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}).start();

		getContentPane().add(name);
		getContentPane().add(gestenEvent);
		getContentPane().add(subscriptions);
	}

	private String decipherTopic(String crypticText) {
		return crypticText.substring("topic:".length(), crypticText.indexOf(";"));
	}

	private Map<String, String> decipherValues(String crypticText) {
		Map<String, String> values = new HashMap<>();
		crypticText = crypticText.substring(crypticText.indexOf(";") + 1, crypticText.length());
		System.out.println("CrypticText without topic: " + crypticText);

		while (crypticText.length() > 0) {
//            logger.trace("Index: " + crypticText.indexOf(":"));
			String param = crypticText.substring(0, crypticText.indexOf(":"));
			String value = crypticText.substring(crypticText.indexOf(":") + 1, crypticText.indexOf(";"));

			values.put(param, value);

			crypticText = crypticText.substring(crypticText.indexOf(";") + 1, crypticText.length());
		}

		return values;
	}

}
