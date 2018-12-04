package com.tris.REST;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import org.tris.internal.Subscription;
import org.tris.internal.SubscriptionManager;

@Path("subscriptionService")
public class SubscriptionServiceImpl implements SubscriptionService {

	private Logger logger = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

	public SubscriptionServiceImpl() {
		System.out.println("New Instance was created");
	}

	@Override
	@POST
	public Response subscribe(@Context HttpServletRequest request, @HeaderParam("callbackPort") int callbackPort, @HeaderParam("identifier") String identifier, @HeaderParam("topic") String topic) {
		System.out.println("NEW CALL!!!!!!!");
		Subscription sub = new Subscription();
		sub.setSubscriberIdentifier(identifier);
		sub.setTopic(topic);
		sub.setCallbackAddress(request.getRemoteAddr());
		sub.setPort(callbackPort);
		
		SubscriptionManager.getInstance().addSubscription(sub);

		return Response.noContent().build();
	}

	@Override
	@DELETE
	public Response unsubscribe(@Context HttpServletRequest request, @HeaderParam("callbackPort") int callbackPort, @HeaderParam("identifier") String identifier, @HeaderParam("topic") String topic) {
		SubscriptionManager.getInstance().removeSubscription(topic, identifier);
		
		return Response.noContent().build();
	}

	@Override
	public void onEvent(String topic) {
		Map<String, Subscription> subsForTopicX = SubscriptionManager.getInstance().getCopyOfSubsForTopic(topic);
		
		if(subsForTopicX == null) {
			return;
		}
		
		for(Entry<String, Subscription> e : subsForTopicX.entrySet()) {
			Subscription sub = e.getValue();
			sendEventNotification(sub);
		}
	}

	private void sendEventNotification(Subscription sub) {
		System.out.println("I'm notifying someone!");
		String motionName = "PLACEHOLDER";
		String sentence = "topic:" + sub.getTopic() + ";name:" + motionName + ";";
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
