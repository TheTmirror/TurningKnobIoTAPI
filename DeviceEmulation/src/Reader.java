import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketException;

public class Reader extends Thread {

	@Override
	public void run() {
		try {
			MulticastSocket multiSocket = new MulticastSocket(1900);
			multiSocket.joinGroup(InetAddress.getByName("239.255.255.250"));
			while (true) {

				byte[] buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				multiSocket.receive(packet);
				String msg = new String(packet.getData());
//				if(msg.contains("meinErsterDrehknopf-v0.02")) {
				System.out.println("Received the following msg:");
				System.out.println(msg);
//				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}