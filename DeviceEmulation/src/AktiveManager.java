import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.TimeUnit;

public class AktiveManager extends Thread {

	private static final int UPNP_PORT = 1900;
	private static final String MULTICAST_ADDR = "239.255.255.250";

	private String responseMsg = "HTTP/1.1 200 OK\r\n" + "HOST: %s:%d\r\n" + "EXT:\r\n"
			+ "CACHE-CONTROL: max-age=100\r\n" + "LOCATION: %s\r\n"
			+ "SERVER: FreeRTOS/7.4.2, UPnP/1.0, IpBridge/1.15.0\r\n" + "hue-bridgeid: %s\r\n" + "ST: %s\r\n"
			+ "USN: uuid:%s::upnp:rootdevice\r\n\r\n";

	private DatagramSocket multiSocket;

	public AktiveManager() throws IOException {
		multiSocket = new DatagramSocket();
	}

	@Override
	public void run() {
		try {
			while (true) {
				String msg = "Test";
				DatagramPacket outPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, InetAddress.getLocalHost(), 2307);
				multiSocket.send(outPacket);
				System.out.println("Send MSG");
				
				TimeUnit.SECONDS.sleep(4);
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
