package com.tris.REST;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tris.internal.Event;
import org.tris.internal.Subscription;
import org.tris.internal.SubscriptionManager;

@Path("subscriptionService")
public class SubscriptionServiceImpl implements SubscriptionService {

	public SubscriptionServiceImpl() {
		System.out.println("New Instance was created");
	}

	@Override
	@POST
	public Response subscribe(@Context HttpServletRequest request, @HeaderParam("callbackPort") int callbackPort, @HeaderParam("identifier") String identifier, @HeaderParam("topic") String topic, @HeaderParam("bootid") long bootid) {
		System.out.println("NEW CALL!!!!!!!");
		Subscription sub = new Subscription();
		sub.setSubscriberIdentifier(identifier);
		sub.setTopic(topic);
		sub.setCallbackAddress(request.getRemoteAddr());
		sub.setPort(callbackPort);
		sub.setBootid(bootid);
		
		SubscriptionManager.getInstance().addSubscription(sub);

		return Response.noContent().build();
	}

	@Override
	@DELETE
	public Response unsubscribe(@Context HttpServletRequest request, @HeaderParam("callbackPort") int callbackPort, @HeaderParam("identifier") String identifier, @HeaderParam("topic") String topic, @HeaderParam("bootid") long bootid) {
		SubscriptionManager.getInstance().removeSubscription(topic, identifier);
		
		return Response.noContent().build();
	}

	@Override
	public void onEvent(Event event) {
		Map<String, Subscription> subsForTopicX = SubscriptionManager.getInstance().getCopyOfSubsForTopic(event.getTopic());
		
		if(subsForTopicX == null) {
			return;
		}
		
		for(Entry<String, Subscription> e : subsForTopicX.entrySet()) {
			Subscription sub = e.getValue();
			sendEventNotification(sub, event);
		}
	}

	private void sendEventNotification(Subscription sub, Event event) {
		System.out.println("I'm notifying someone!");
		String sentence = "topic:" + event.getTopic() + ";";
		
		for(Entry<String, String> e : event.getValues().entrySet()) {
			sentence = sentence + e.getKey() + ":" + e.getValue() + ";";
		}
		
		Socket clientSocket;
		try {
			clientSocket = new Socket(sub.getCallbackAddress(), sub.getPort());
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			outToServer.writeBytes(sentence);
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
