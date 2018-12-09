package com.tris.background;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Random;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class UpnpManager extends Thread implements ServletContextListener {
	
	private static final int EXPIRATION_TIME = 60;
	
	@Override
	public void run() {
//		delayBoot();
		
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+1"));
		long bootid = calendar.getTimeInMillis() / 1000L;
		
		try {
//			SearchListener search = new SearchListener();
//			search.start();
			
			CorrectedSearchListener search = new CorrectedSearchListener(this.EXPIRATION_TIME, bootid);
			search.start();
			
			Announcer announcer = new Announcer(60, bootid);
//			announcer.start();
			
			TimeUnit.SECONDS.sleep(120);
//			System.out.println("Kicking off shutdown");
//			announcer.shutdown();
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Call this method to ensure, every message from earlier runtimes
	 * are expired
	 */
	private void delayBoot() {
		try {
			TimeUnit.SECONDS.sleep(this.EXPIRATION_TIME);
		} catch (InterruptedException e) {
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