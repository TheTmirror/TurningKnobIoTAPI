package com.tris.REST;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("functions")
public class MyResource {
	
	@Path("onlineAndReachable")
	@GET
	public Response onlineAndReachableCheck() {
		return Response.noContent().build();
	}

	@Path("testJson")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response test(@HeaderParam("myParam") String myParam) {
		System.out.println("WORKED!");
		System.out.println("Dies ist mein Param: " + myParam);
		
		ResponseBody responseBody = new ResponseBody();
		responseBody.getParameter().put("A", true);
		responseBody.getParameter().put("A", true);
		responseBody.getParameter().put("B", "Hallo");
		responseBody.getParameter().put("C", 1);
		
		Gson gson = new Gson();
		String responseBodyMessage = gson.toJson(responseBody);
		
		return Response.status(Status.OK).entity(responseBodyMessage).build();
	}

	private class ResponseBody {

		private Map<String, Object> parameter;
		
		private ResponseBody() {
			parameter = new HashMap<>();
		}

		public Map<String, Object> getParameter() {
			return parameter;
		}

		public void setParameter(Map<String, Object> parameter) {
			this.parameter = parameter;
		}
		
	}

}