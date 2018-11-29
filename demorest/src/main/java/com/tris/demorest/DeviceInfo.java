package com.tris.demorest;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.internal.ws.api.message.Message;

@Path("testService/control")
public class DeviceInfo {
	
	@POST
	public Response getInfo() {
		System.out.println("Es hat funktioniert!!!!");
		
		return Response.ok().build();
	}
	
}