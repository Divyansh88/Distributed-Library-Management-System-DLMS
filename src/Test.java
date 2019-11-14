import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.time.LocalDateTime;

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
public class Test {
	static ServerInterface siu = null;
	
	public static void main(String arg[]) {
		Runnable task1 = () -> {
			try {
				concordiaUser(arg);
			} catch (MalformedURLException | RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
		Runnable task2 = () -> {
			try {
				mcgillUser(arg);
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

	//Case 1: User tries already borrowed a book from other server and tries to exchange the same book from the same server. ---> Success
	//        User again tries to exchange the book from other server which is not available so atomicity fails.  ---> Fail
	//        User again tries to exchange with the unborrowed book so atomicity fails.  ---> Fail
	public static void concordiaUser(String arg[]) throws MalformedURLException, RemoteException {
		String user_id = "CONU1111";
		
		URL compURL = new URL("http://localhost:1111/comp?wsdl");
		QName compQName = new QName("http://Servers/", "ConcordiaServerService");
		Service compService = Service.create(compURL, compQName);
		siu = compService.getPort(ServerInterface.class);
		System.out.println("Concordia's user");
		
		String item_id = "MCG2288";
		int number_of_days = 17;
		String response;
		response = siu.borrowItem(user_id, item_id, number_of_days);
		System.out.println(response);
		
		String new_item_id = "MCG2222", old_item_id = "MCG2288";
		response = siu.exchangeItem(user_id, new_item_id, old_item_id);  // Success
		System.out.println(response);
		
		new_item_id = "CON1144";
		old_item_id = "MCG2222";
		response = siu.exchangeItem(user_id, new_item_id, old_item_id);  // Fail
		System.out.println(response);
		
		new_item_id = "CON1188";
		old_item_id = "CON1144";
		response = siu.exchangeItem(user_id, new_item_id, old_item_id);  // Fail
		System.out.println(response);
		
		
		
	}
	
	
	//Case 2: User tries already borrowed a book from other server and tries to exchange a book from the same server. ---> Success
	public static void mcgillUser(String arg[]) throws MalformedURLException, RemoteException {
		String user_id = "MCGU1111";
		
		URL compURL = new URL("http://localhost:2222/comp?wsdl");
		QName compQName = new QName("http://Servers/", "McGillServerService");
		Service compService = Service.create(compURL, compQName);
		siu = compService.getPort(ServerInterface.class);
		System.out.println("McGill's's user");
		
		String item_id = "CON1188";
		int number_of_days = 26;
		String response;
		response = siu.borrowItem(user_id, item_id, number_of_days);
		System.out.println(response);
		
		item_id = "MCG2288";
		number_of_days = 8;
		response = siu.borrowItem(user_id, item_id, number_of_days);
		System.out.println(response);
		
		String new_item_id = "CON1122", old_item_id = "MCG2288";
		response = siu.exchangeItem(user_id, new_item_id, old_item_id);  // Fail
		System.out.println(response);
		
	}

}