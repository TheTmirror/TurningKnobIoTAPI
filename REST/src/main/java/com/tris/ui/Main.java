package com.tris.ui;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Main implements ServletContextListener{

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		UIFrame frame = new UIFrame();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}

}
