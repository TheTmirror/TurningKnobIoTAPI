import java.io.IOException;

public class UpnpManager extends Thread{

	public UpnpManager() {
		
	}
	
	@Override
	public void run() {
		Reader reader = new Reader();
		reader.start();
	}
	
}