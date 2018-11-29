import java.io.IOException;

public class UpnpManager extends Thread{

	public UpnpManager() {
		
	}
	
	@Override
	public void run() {
		try {
			SearchListener search = new SearchListener();
			search.start();
			
			Reader reader = new Reader();
			reader.start();
			
			AktiveManager aktive = new AktiveManager();
//			aktive.start();
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}