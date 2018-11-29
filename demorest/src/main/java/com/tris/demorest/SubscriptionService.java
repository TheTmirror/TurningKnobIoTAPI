package com.tris.demorest;

import javax.websocket.server.PathParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("subscriptionService")
public class SubscriptionService {
	
	@Path("{subscriptionMethod}")
	@GET
	public Response fetchSubscription(@PathParam("subsciptionMethod") String method) {
		if(method == null || method.equals("")) {
			System.out.println("ERRROR!!!!");
		} else {
			System.out.println(method);
		}
		return Response.ok().build();
	}
	public Response addSubscription() {
		return Response.ok().build();
	}

}