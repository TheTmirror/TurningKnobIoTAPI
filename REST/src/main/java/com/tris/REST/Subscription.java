package com.tris.REST;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

@Path("subscribe")
public class Subscription {
	
	@POST
	public void subscribe(@HeaderParam("topic") String topic) {
		System.out.println("Has subscribed for topic: " + topic);
	}

}