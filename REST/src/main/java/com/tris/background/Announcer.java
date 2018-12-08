package com.tris.background;

import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Announcer extends Thread {

	private AliveAnnouncer alive;
	private ByeByeAnnouncer bye;
	private Thread timer;
	private Runnable sendRun;

	public Announcer() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+1"));
		long bootid = calendar.getTimeInMillis() / 1000L;

		alive = new AliveAnnouncer(bootid);
		bye = new ByeByeAnnouncer(bootid);
		
		sendRun = new Runnable() {
			
			@Override
			public void run() {
				onEvent("alive");
			}
		};
		
		timer = new Thread(new Runnable() {

			@Override
			public void run() {
				Random r = new Random();
				while (true) {
					try {
						int timeout = r.nextInt(AliveAnnouncer.EXPIRATION_TIME / 2);
						System.out.println("Next alive msg in: " + timeout + " seconds");
						TimeUnit.SECONDS.sleep(timeout);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					new Thread(sendRun).start();
				}
			}
		});

	}
	
	@Override
	public void run() {
		new Thread(sendRun).start();
		timer.start();
	}

	private void onEvent(String type) {
		switch (type) {
		case "alive":
			try {
				alive.sendAliveMessageBundle();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case "shutdown":
			timer.stop();
			try {
				bye.sendByeByeMessageBundle();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		default:
			break;
		}
	}

	public void shutdown() {
		onEvent("shutdown");
	}

}