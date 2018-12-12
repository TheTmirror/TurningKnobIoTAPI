package com.tris.background;

import java.awt.print.Printable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.plaf.synth.SynthSeparatorUI;

public class CorrectedSearchListener extends Thread {

	private static final String NEW_LINE = "\r\n";
	public static final String BASE = "HTTP/1.1 200 OK" + NEW_LINE + "CACHE-CONTROL: max-age = %d" + NEW_LINE +
	// "DATE: " Only recommended
			"EXT:" + NEW_LINE + "LOCATION: %s" + NEW_LINE + "SERVER: %s" + NEW_LINE + "ST: %s" + NEW_LINE + "USN: %s"
			+ NEW_LINE + "BOOTID.UPNP.ORG: %d" + NEW_LINE + NEW_LINE;

	private static final String UUID = "meinErsterDrehknopf-v0.02";
	private static final String DOMAIN_NAME = "tristan";
	private static final String DEVICE_TYPE = "turningknob";
	private static final String VERSION = "1";

	private static final String MULTICAST_ADDRESS = "239.255.255.250";
	private static final int MULTICAST_PORT = 1900;

	public int EXPIRATION_TIME;
	private static final String DDD_LOCATION = "http://localhost:9090/discovery.xml";
	private static final String SERVER = "SERVER: WINDOWS/7, UPnP/1.0, Drehknopf/1.0";
	private static final String ST_ALL = "ssdp:all";
	private static final String ST_1 = "upnp:rootdevice";
	private static final String ST_2 = "uuid:" + UUID;
	private static final String ST_3 = "urn:" + DOMAIN_NAME + ":device:" + DEVICE_TYPE + ":" + VERSION;
	private static final String USN_1 = "uuid:" + UUID + "::upnp:rootdevice";
	private static final String USN_2 = "uuid:" + UUID;
	private static final String USN_3 = "uuid:" + UUID + "::urn:" + DOMAIN_NAME + ":device:" + DEVICE_TYPE + ":"
			+ VERSION;

	private long BOOTID;
	private static final int CONFIGID = 1;

	private MulticastSocket multiSocket;

	private BlockingQueue<DatagramPacket> jobList;
	private BlockingQueue<Response> doneJobs;
	private ExecutorService service;

	public CorrectedSearchListener(int expirationTime, long bootid) throws IOException {
		this.EXPIRATION_TIME = expirationTime;
		this.BOOTID = bootid;

		multiSocket = new MulticastSocket(MULTICAST_PORT);
		multiSocket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));

		jobList = new LinkedBlockingQueue<>();
		doneJobs = new LinkedBlockingQueue<>();

		service = Executors.newFixedThreadPool(9);
	}

	@Override
	public void run() {
		service.execute(receiver);
		for (int i = 0; i < 4; i++) {
			service.execute(processor);
			service.execute(sender);
		}
	}

	private Runnable receiver = new Runnable() {

		@Override
		public void run() {
			while (true) {
				try {
					byte[] buf = new byte[1024];
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					multiSocket.receive(packet);
//					System.out.println("Got something from " + packet.getAddress() + ":" + packet.getPort());
					jobList.put(packet);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}

			}
		}

	};

	private Runnable processor = new Runnable() {

		@Override
		public void run() {
			while (true) {
				try {
					DatagramPacket packet = jobList.take();
					if (packet == null) {
						System.out.println("EXCEPTION HAD TO HAPPEN BEFORE!!!!");
						continue;
					}
					if (isSearch(packet)) {
						System.out.println("Got search");
						processMessage(packet);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void processMessage(DatagramPacket packet) throws InterruptedException {
			String st = getST(packet);
			System.out.println("ST is !" + st + "!");

			switch (st) {
			case ST_ALL:
				String msgA1 = buildMessage1();
				String msgA2 = buildMessage2();
				String msgA3 = buildMessage3();

				doneJobs.put(new Response(msgA1, packet.getAddress(), packet.getPort()));
				doneJobs.put(new Response(msgA1, packet.getAddress(), packet.getPort()));
				doneJobs.put(new Response(msgA1, packet.getAddress(), packet.getPort()));
				break;

			case ST_1:
				String msg1 = buildMessage1();
				doneJobs.put(new Response(msg1, packet.getAddress(), packet.getPort()));
				break;

			case ST_2:
				String msg2 = buildMessage2();
				doneJobs.put(new Response(msg2, packet.getAddress(), packet.getPort()));
				break;

			case ST_3:
				String msg3 = buildMessage3();
				doneJobs.put(new Response(msg3, packet.getAddress(), packet.getPort()));
				break;

			default:
				break;
			}
		}

		private String buildMessage1() {
			return String.format(BASE, EXPIRATION_TIME, DDD_LOCATION, SERVER, ST_1, USN_1, BOOTID);
		}

		private String buildMessage2() {
			return String.format(BASE, EXPIRATION_TIME, DDD_LOCATION, SERVER, ST_2, USN_2, BOOTID);
		}

		private String buildMessage3() {
			return String.format(BASE, EXPIRATION_TIME, DDD_LOCATION, SERVER, ST_3, USN_3, BOOTID);
		}

		private boolean isSearch(DatagramPacket packet) {
			return (new String(packet.getData()).startsWith("M-SEARCH"));
		}

		private String getST(DatagramPacket packet) {
			String data = new String(packet.getData());

			data = data.substring(data.indexOf("ST:"), data.length());
			int indexN = data.indexOf("\n");
			int indexR = data.indexOf("\r");
			int index = -1;
			if (indexN < indexR) {
				index = indexN;
			} else {
				index = indexR;
			}

			data = data.substring(0, index);

			data = data.substring("ST:".length(), data.length()).replaceAll(" ", "");

			return data;
		}

	};

	private Runnable sender = new Runnable() {

		@Override
		public void run() {
			try {
				DatagramSocket sendSocket = new DatagramSocket();
				while (true) {
					Response response = doneJobs.take();
					if (response == null) {
						System.out.println("EXCEPTION HAD TO HAPPEN BEFORE!!!!");
						continue;
					}
					DatagramPacket packet = new DatagramPacket(response.getMsg().getBytes(),
							response.getMsg().getBytes().length, response.getAddr(), response.getPort());
					sendSocket.send(packet);
//					System.out.println("SEARCH Response sent");
//					System.out.println(new String(packet.getData()));
//					System.out.println("An " + packet.getAddress() + ":" + packet.getPort());
					
				}
			} catch (NullPointerException | InterruptedException | IOException e) {
				e.printStackTrace();
			}
		}
	};

	private class Response {

		private String msg;
		private InetAddress addr;
		private int port;

		private Response(String msg, InetAddress addr, int port) {
			this.msg = msg;
			this.addr = addr;
			this.port = port;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public InetAddress getAddr() {
			return addr;
		}

		public void setAddr(InetAddress addr) {
			this.addr = addr;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

	}

}