package com.tris.REST;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

public interface SubscriptionService {

	public Response subscribe(HttpServletRequest request, int callbackPort, String identifier, String topic);
	
	public Response unsubscribe(HttpServletRequest request, int callbackPort, String identifier, String topic);
	
	public void onEvent(String topic);
	
}