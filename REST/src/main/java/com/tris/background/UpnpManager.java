package com.tris.background;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class UpnpManager extends Thread implements ServletContextListener {
	
	@Override
	public void run() {
		try {
			SearchListener search = new SearchListener();
			search.start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		this.start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		this.stop();
	}

}