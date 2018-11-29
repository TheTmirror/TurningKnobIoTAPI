package com.tris.service;

import javax.jws.WebService;

@WebService(endpointInterface = "com.tris.service.SOAPService")
public class SOAPServiceImpl implements SOAPService{

	@Override
	public void action(int i) {
		System.out.println(String.format("Der Wert %d kam an", i));
	}

}