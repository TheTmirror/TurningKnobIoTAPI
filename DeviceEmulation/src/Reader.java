import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

public class Reader extends Thread {

	@Override
	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket(2307);
			while (true) {

				byte[] buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				System.out.println("Waiting");
				socket.receive(packet);

				System.out.println("Received something from: " + packet.getAddress() + ":" + packet.getPort());
				System.out.println(new String(packet.getData()));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}