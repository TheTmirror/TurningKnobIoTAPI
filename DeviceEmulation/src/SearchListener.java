import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.concurrent.TimeUnit;

public class SearchListener extends Thread {

	private static final int DELAY = 0;
	
	private static final int UPNP_PORT = 1900;
	private static final String MULTICAST_ADDR = "239.255.255.250";
	private static final String DDD_LOCATION = "http://localhost:9090/discovery.xml";
	private static final String ST = "upnp:rootdevice";
	private static final String USN = "meinErsterDrehknopf-v0.01";

	private static final String NEW_LINE = "\r\n";
	private String responseMsg = "HTTP/1.1 200 OK" + NEW_LINE + "HOST: %s:%d" + NEW_LINE + "EXT:" + NEW_LINE
			+ "CACHE-CONTROL: max-age=100" + NEW_LINE + "LOCATION: %s" + NEW_LINE
			+ "SERVER: WINDOWS/7, UPnP/1.0, Drehknopf/1.0" + NEW_LINE + "ST: %s" + NEW_LINE +
//			+ "USN: uuid:%s::upnp:rootdevice\r\n\r\n";
			"USN: uuid:%s" + NEW_LINE + NEW_LINE;

	private MulticastSocket multiSocket;
	private byte[] buf;
	private DatagramPacket recv;

	private DatagramSocket sendSocket;

	public SearchListener() throws IOException {
		multiSocket = new MulticastSocket(UPNP_PORT);
		multiSocket.joinGroup(InetAddress.getByName(MULTICAST_ADDR));

		sendSocket = new DatagramSocket();

		buf = new byte[1024];
		recv = new DatagramPacket(buf, buf.length);
	}

	@Override
	public void run() {
		try {
			System.out.println("Running");
			while (true) {
				receiveMessage();
				if (isSearch()) {
					System.out.println("Here");
					sendResponse(buildResponse());
				}

				if (isDelayEnabled()) {
					TimeUnit.SECONDS.sleep(DELAY);
				}
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void receiveMessage() throws IOException {
		System.out.println("Waiting for message");
//		System.out.println(multiSocket.getLocalAddress() + ":" + multiSocket.getLocalPort() + multiSocket.getPort());
		multiSocket.receive(recv);
		System.out.println("Received message from " + recv.getAddress() + ":" + recv.getPort());

//		if (recv.getLength() > 0) {
//			String data = new String(recv.getData());
//
//			if (data.startsWith("M-SEARCH")) {
//				return true;
//			}
//		}
//
//		return false;
	}

	private boolean isSearch() {
		return (new String(recv.getData())).startsWith("M-SEARCH");
	}

	private String buildResponse() {
		String response = String.format(responseMsg, MULTICAST_ADDR, UPNP_PORT, DDD_LOCATION, ST, USN);
		return response;
	}

	private void sendResponse(String msg) throws IOException {
		sendSocket.send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, recv.getAddress(), recv.getPort()));
		System.out.println("Sent out message to: " + recv.getAddress() + ":" + recv.getPort());
		System.out.println(msg);
	}
	
	private boolean isDelayEnabled() {
		return DELAY > 0;
	}

}
