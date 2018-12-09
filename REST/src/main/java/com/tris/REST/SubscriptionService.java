package com.tris.REST;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.tris.internal.Event;

public interface SubscriptionService {

	public Response subscribe(HttpServletRequest request, int callbackPort, String identifier, String topic, long bootid);
	
	public Response unsubscribe(HttpServletRequest request, int callbackPort, String identifier, String topic, long bootid);
	
	public void onEvent(Event event);
	
}