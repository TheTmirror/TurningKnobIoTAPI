package com.tris.background;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.TimeZone;

public class ByeByeAnnouncer {

	private static final String NEW_LINE = "\r\n";
	public static final String BASE = "NOTIFY * HTTP/1.1" + NEW_LINE + "HOST: %s:%d" + NEW_LINE 
			+ "NT: %s" + NEW_LINE + "NTS: %s"
			+ NEW_LINE + "USN: %s" + NEW_LINE + "BOOTID.UPNP.ORG: %d" + NEW_LINE
			+ "CONFIGID.UPNP.ORG: %d" + NEW_LINE + NEW_LINE;

	private static final String UUID = "meinErsterDrehknopf-v0.02";
	private static final String DOMAIN_NAME = "tristan";
	private static final String DEVICE_TYPE = "turningknob";
	private static final String VERSION = "1";
	private static final String MULTICAST_ADDRESS = "239.255.255.250";
	private static final int MULTICAST_PORT = 1900;
	private static final String NT_1 = "upnp:rootdevice";
	private static final String NT_2 = "uuid:" + UUID;
	private static final String NT_3 = "urn:" + DOMAIN_NAME + ":device:" + DEVICE_TYPE + ":" + VERSION;
	private static final String NTS = "ssdp:byebye";
	private static final String USN_1 = "uuid:" + UUID + "::upnp:rootdevice";
	private static final String USN_2 = "uuid:" + UUID;
	private static final String USN_3 = "uuid:" + UUID + "::urn:" + DOMAIN_NAME + ":device:" + DEVICE_TYPE + ":"
			+ VERSION;

	private long BOOTID;
	private static final int CONFIGID = 1;
	
	public ByeByeAnnouncer(long BOOTID) {
//		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+1"));
//		this.BOOTID = calendar.getTimeInMillis() / 1000L;
		this.BOOTID = BOOTID;
	}
	
	public void sendByeByeMessageBundle() throws Exception {
		String msg1 = buildByeByeMessage1();
		String msg2 = buildByeByeMessage2();
		String msg3 = buildByeByeMessage3();
		
		DatagramSocket socket = new DatagramSocket();
		InetAddress addr = InetAddress.getByName(MULTICAST_ADDRESS);
		socket.send(new DatagramPacket(msg1.getBytes(), msg1.getBytes().length, addr, MULTICAST_PORT));
		socket.send(new DatagramPacket(msg2.getBytes(), msg2.getBytes().length, addr, MULTICAST_PORT));
		socket.send(new DatagramPacket(msg3.getBytes(), msg3.getBytes().length, addr, MULTICAST_PORT));
		System.out.println("Sent msg bundle");
	}

	private String buildByeByeMessage1() {
		return String.format(BASE, MULTICAST_ADDRESS, MULTICAST_PORT, NT_1, NTS, USN_1, BOOTID, CONFIGID);
	}

	private String buildByeByeMessage2() {
		return String.format(BASE, MULTICAST_ADDRESS, MULTICAST_PORT, NT_2, NTS, USN_2, BOOTID, CONFIGID);
	}

	private String buildByeByeMessage3() {
		return String.format(BASE, MULTICAST_ADDRESS, MULTICAST_PORT, NT_3, NTS, USN_3, BOOTID, CONFIGID);
	}
	
}
