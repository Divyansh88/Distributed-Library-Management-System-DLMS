import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import Servers.ServerInterface;


/**
 * @author Divyansh
 *
 */
public class TestConcurrent {
	static ServerInterface siu = null;
	
	public static void main(String arg[]) {
		Runnable task1 = () -> {
			try {
				firstUser(arg);
			} catch (MalformedURLException | RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
		Runnable task2 = () -> {
			try {
				secondUser(arg);
			} catch (MalformedURLException | RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
				
		Thread thread1 = new Thread(task1);
		Thread thread2 = new Thread(task2);	
		thread1.start();
		thread2.start();
		
	}
	
	public static void firstUser(String arg[]) throws MalformedURLException, RemoteException {
		String user_id = "CONU1111";
		
		URL compURL = new URL("http://localhost:1111/comp?wsdl");
		QName compQName = new QName("http://Servers/", "ConcordiaServerService");
		Service compService = Service.create(compURL, compQName);
		siu = compService.getPort(ServerInterface.class);
		System.out.println("Concordia's user");
		
		String item_id = "CON1122";
		int number_of_days = 26;
		String response;
		response = siu.borrowItem(user_id, item_id, number_of_days);
		System.out.println("User 1:"+response);
	}
	
	public static void secondUser(String arg[]) throws MalformedURLException, RemoteException {
		String user_id = "MCGU1111";
		
		URL compURL = new URL("http://localhost:2222/comp?wsdl");
		QName compQName = new QName("http://Servers/", "McGillServerService");
		Service compService = Service.create(compURL, compQName);
		siu = compService.getPort(ServerInterface.class);
		System.out.println("McGill's's user");
		
		String item_id = "CON1122";
		int number_of_days = 17;
		String response;
		response = siu.borrowItem(user_id, item_id, number_of_days);
		System.out.println("User 2:"+response);
	}
}
