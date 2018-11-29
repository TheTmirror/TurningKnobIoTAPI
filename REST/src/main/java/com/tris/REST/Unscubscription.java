package com.tris.REST;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;

@Path("unsubscribe")
public class Unscubscription {

	@DELETE
	public void unsubscribe(@HeaderParam("topic") String topic) {
		System.out.println("Has unscubscribed from topic: " + topic);
	}
	
}