package com.tris.REST;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("test")
public class MyResource {

	@GET
	public void test(@HeaderParam("myParam") String myParam) {
		System.out.println("WORKED!");
		System.out.println("Dies ist mein Param: " + myParam);
	}

}