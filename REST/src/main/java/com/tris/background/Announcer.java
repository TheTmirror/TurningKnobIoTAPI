package com.tris.background;

import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Announcer extends Thread {

	private AliveAnnouncer alive;
	private ByeByeAnnouncer bye;
	private boolean shutdown = false;

	public Announcer() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+1"));
		long bootid = calendar.getTimeInMillis() / 1000L;

		alive = new AliveAnnouncer(bootid);
		bye = new ByeByeAnnouncer(bootid);
	}

	@Override
	public void run() {
		Random r = new Random();

		try {
			alive.sendAliveMessageBundle();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (!shutdown) {
			try {
				int timeout = r.nextInt(AliveAnnouncer.EXPIRATION_TIME / 2);
				System.out.println("Next alive msg in: " + timeout + " seconds");
				TimeUnit.SECONDS.sleep(timeout);
				alive.sendAliveMessageBundle();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			bye.sendByeByeMessageBundle();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void shutdown() {
		this.shutdown = true;
	}

}