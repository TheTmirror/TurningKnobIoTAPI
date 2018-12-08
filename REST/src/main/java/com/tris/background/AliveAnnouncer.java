package com.tris.background;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.TimeZone;

public class AliveAnnouncer {

	private static final String NEW_LINE = "\r\n";
	public static final String BASE = "NOTIFY * HTTP/1.1" + NEW_LINE + "HOST: %s:%d" + NEW_LINE
			+ "CACHE-CONTROL: max-age = %d" + NEW_LINE + "LOCATION: %s" + NEW_LINE + "NT: %s" + NEW_LINE + "NTS: %s"
			+ NEW_LINE + "SERVER: %s" + NEW_LINE + "USN: %s" + NEW_LINE + "BOOTID.UPNP.ORG: %d" + NEW_LINE
			+ "CONFIGID.UPNP.ORG: %d" + NEW_LINE + NEW_LINE;

	private static final String UUID = "meinErsterDrehknopf-v0.02";
	private static final String DOMAIN_NAME = "tristan";
	private static final String DEVICE_TYPE = "turningknob";
	private static final String VERSION = "1";
	private static final String MULTICAST_ADDRESS = "239.255.255.250";
	private static final int MULTICAST_PORT = 1900;
	public static final int EXPIRATION_TIME = 60;
	private static final String DDD_LOCATION = "http://localhost:9090/discovery.xml";
	private static final String NT_1 = "upnp:rootdevice";
	private static final String NT_2 = "uuid:" + UUID;
	private static final String NT_3 = "urn:" + DOMAIN_NAME + ":device:" + DEVICE_TYPE + ":" + VERSION;
	private static final String NTS = "ssdp:alive";
	private static final String SERVER = "SERVER: WINDOWS/7, UPnP/1.0, Drehknopf/1.0";
	private static final String USN_1 = "uuid:" + UUID + "::upnp:rootdevice";
	private static final String USN_2 = "uuid:" + UUID;
	private static final String USN_3 = "uuid:" + UUID + "::urn:" + DOMAIN_NAME + ":device:" + DEVICE_TYPE + ":"
			+ VERSION;

	private long BOOTID;
	private static final int CONFIGID = 1;

	public AliveAnnouncer(long BOOTID) {
//		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+1"));
//		this.BOOTID = calendar.getTimeInMillis() / 1000L;
		this.BOOTID = BOOTID;
	}
	
	public void sendAliveMessageBundle() throws Exception {
		String msg1 = buildAliveMessage1();
		String msg2 = buildAliveMessage2();
		String msg3 = buildAliveMessage3();
		
		DatagramSocket socket = new DatagramSocket();
		InetAddress addr = InetAddress.getByName(MULTICAST_ADDRESS);
		socket.send(new DatagramPacket(msg1.getBytes(), msg1.getBytes().length, addr, MULTICAST_PORT));
		socket.send(new DatagramPacket(msg2.getBytes(), msg2.getBytes().length, addr, MULTICAST_PORT));
		socket.send(new DatagramPacket(msg3.getBytes(), msg3.getBytes().length, addr, MULTICAST_PORT));
		System.out.println("Sent msg bundle");
	}

	private String buildAliveMessage1() {
		return String.format(BASE, MULTICAST_ADDRESS, MULTICAST_PORT, EXPIRATION_TIME,
				DDD_LOCATION, NT_1, NTS, SERVER, USN_1, BOOTID, CONFIGID);
	}

	private String buildAliveMessage2() {
		return String.format(BASE, MULTICAST_ADDRESS, MULTICAST_PORT, EXPIRATION_TIME,
				DDD_LOCATION, NT_2, NTS, SERVER, USN_2, BOOTID, CONFIGID);
	}

	private String buildAliveMessage3() {
		return String.format(BASE, MULTICAST_ADDRESS, MULTICAST_PORT, EXPIRATION_TIME,
				DDD_LOCATION, NT_3, NTS, SERVER, USN_3, BOOTID, CONFIGID);
	}
	
}