package Servers;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.ws.Endpoint;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import Others.Books;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(endpointInterface = "Servers.ServerInterface")
@SOAPBinding(style = SOAPBinding.Style.RPC)

/**
 * @author Divyansh
 *
 */
public class ConcordiaServer implements ServerInterface {
	
	String return_msg;
	static String path = "src\\Log files";
	static String path1 = "src\\Log files\\";
	 
	Books book = null;
	static Map<String,Books> book_shelf = new HashMap<String,Books>();
	static Map<String,ArrayList<String>> waitlist = new HashMap<String,ArrayList<String>>();
	static Map<String,ArrayList> borrow = new HashMap<String,ArrayList>(); 
	
	public ConcordiaServer() throws RemoteException {
		super();
	}
	
	@Override
	public synchronized String addItem(String manager_id,String item_id,String item_name,int quantity) {
		boolean flag = false;
		String return_value = "";
		book = new Books(item_id, item_name, quantity);
		for(Map.Entry<String, Books> entry: book_shelf.entrySet()){
			Books temp_book = entry.getValue();
			if(temp_book.getItemName().equalsIgnoreCase(book.getItemName()) && temp_book.getItemId().equalsIgnoreCase(book.getItemId())){
				quantity = book.getQuantity() + temp_book.getQuantity();
				temp_book.setQuantity(quantity);
				System.out.println("Book "+book.getItemName() +" already exist, increased the quantity.");
				return_msg = "Book "+book.getItemName() +" already exist, increased the quantity.";
				entry.setValue(temp_book);
				flag = true;
			}
		}
		
		if(!flag){
			book_shelf.put(item_id, book);
			System.out.println("\nBook "+book.getItemName() +" added into library.");
			return_msg = book.getItemName()+" Successfully added.";
			for(Map.Entry<String,Books> entry: book_shelf.entrySet()){
				Books book = entry.getValue();
				System.out.println(book.getItemId()+" " +book.getItemName() +" " +book.getQuantity());
				System.out.println();
			}
		}
		
		int default_days = 10;
		if(!waitlist.isEmpty()) {
			if(waitlist.containsKey(item_id)) {
				ArrayList queue = waitlist.get(item_id);
				int new_quantity = book_shelf.get(item_id).getQuantity();
				for(int i = new_quantity;i>0;i--) {
					if(!queue.isEmpty()) {
						String user_id = (String)queue.get(0);
						System.out.println(user_id);
						try {
							return_value = borrowItem(user_id, item_id,default_days);
						}
						catch (Exception e) {
							
						}
						
						if(return_value.equalsIgnoreCase(item_id +" is successfully borrowed.")) {
							LocalDateTime current_time = LocalDateTime.now();
							String print_response = current_time+"--"+"BORROW"+"--"+user_id +", "+item_id+", "+default_days+"--"+return_value+"\n";
							String path2 = path1+user_id;
							appendUsingFileWriter(path2, print_response);
						}
						else if(return_value.equalsIgnoreCase("User can only borrow 1 item from other server.")) {
							LocalDateTime current_time = LocalDateTime.now();
							String print_response = current_time+"--"+"BORROW"+"--"+user_id +", "+item_id+", "+default_days+"--"+return_value+"\n";
							String path2 = path1+user_id; 
							appendUsingFileWriter(path2, print_response);
							i++;
						}	
						queue.remove(user_id);
					}
					else {
						break;
					}
					
				}
				if(queue.isEmpty()){
					waitlist.remove(item_id);
				}
				else {
					waitlist.put(item_id, queue);
				}
				for(String entry: waitlist.keySet()) {
					ArrayList<String> value = waitlist.get(entry);
					System.out.println(entry+" "+value);
				}
				
			}
		}
		for(String entry: waitlist.keySet()) {
			ArrayList<String> value = waitlist.get(entry);
			System.out.println(entry+" "+value);
		}
											
		LocalDateTime current_time = LocalDateTime.now();
		String print_response = current_time+"--"+"ADD"+"--"+manager_id +", "+item_id+", "+item_name+", "+quantity+"--"+return_msg+"\n";
		appendUsingFileWriter(path, print_response);
		return return_msg;
	
	}
	

	@Override
	public synchronized String removeItem(String manager_id, String item_id, int quantity) {
		String book_id;
		
		Books book = book_shelf.get(item_id);
		if(book_shelf.get(item_id)!=null && item_id.equalsIgnoreCase(book.getItemId())) {
			book_id = item_id;
			if(quantity == -1) {
				try {
					removeItemData(book_id);
				}
				catch(Exception e){
					
				}
				
				return_msg = "Book " +book_id +" Completely removed from library.";
				System.out.println(return_msg);
				
			}
			else if(quantity>=0 && quantity<=book.getQuantity()) {
				System.out.println(book.getQuantity()-quantity);
				book.setQuantity(book.getQuantity()-quantity);
				return_msg =quantity +" book of ID " +book_id +" removed from library.";
			}
			else {
				return_msg = "Remove item fails.";
			}
			
		}
		
		else {
			return_msg = "Book "+item_id +" doesn't exist in library.";
			System.out.println(return_msg);
		}
		
		LocalDateTime current_time = LocalDateTime.now();
		String print_response = current_time+"--"+"REMOVE"+"--"+manager_id +", "+item_id+", "+quantity+"--"+return_msg+"\n";
		appendUsingFileWriter(path, print_response);
		return return_msg;
		
	}
	
	public synchronized void removeItemData(String book_id) {
		book_shelf.remove(book_id);
		waitlist.remove(book_id);
		for(String entry: borrow.keySet()){
			ArrayList<Books> list = borrow.get(entry);
			for(Books b : list) {
				if(book_id.equalsIgnoreCase(b.getItemId())) {
					list.remove(b);
				}
			}
			borrow.put(entry, list);
		}
	}
	
	@Override
	public synchronized String listItemAvailability(String manager_id) {
		String message;
		return_msg = "";
		for(Map.Entry<String,Books> entry: book_shelf.entrySet()){
			Books book = entry.getValue();
			System.out.println(book.getItemId()+" " +book.getItemName() +" " +book.getQuantity());
			System.out.println();
			message = book.getItemId()+" " +book.getItemName() +" " +book.getQuantity();
			return_msg = return_msg +message+", ";
		}
		LocalDateTime current_time = LocalDateTime.now();
		String print_response = current_time+"--"+"LIST"+"--"+manager_id +"--"+return_msg+"\n";
		appendUsingFileWriter(path, print_response);
		return return_msg;
		
	}
	
	///////////User's Methods starts
		
		
	@Override
	public synchronized String borrowItem(String user_id, String item_id, int number_of_days) {
		String server,return_value = "",req_message,return_check1="";
		int check;
		
		req_message = "5"+user_id+item_id+number_of_days;
		server = item_id.substring(0, 3);
		if(server.equalsIgnoreCase("CON")) {
			return_value = borrowItemData(user_id, item_id, number_of_days);
		}
		else if(!(server.equalsIgnoreCase("CON"))) {
			if(server.equalsIgnoreCase("MCG")) {
				return_check1 = sendMessage(2222, req_message);
				if(return_check1.equalsIgnoreCase("not ok")) {     
					return_value = "User can only borrow 1 item from other server.";
				}
				else if(return_check1.equalsIgnoreCase("ok")) {
					req_message = "1"+user_id+item_id+number_of_days;
					return_value = sendMessage(2222, req_message);
				}
				
			}
			else if(server.equalsIgnoreCase("MON")) {
				return_check1 = sendMessage(3333, req_message);
				if(return_check1.equalsIgnoreCase("not ok")) {    
					return_value = "User can only borrow 1 item from other server.";
				}
				else if(return_check1.equalsIgnoreCase("ok")) {
					req_message = "1"+user_id+item_id+number_of_days;
					return_value = sendMessage(3333, req_message);
				}
			}			
		}
		
		LocalDateTime current_time = LocalDateTime.now();
		String print_response = current_time+"--"+"BORROW"+"--"+user_id +", "+item_id+", "+number_of_days+"--"+return_value+"\n";
		appendUsingFileWriter(path, print_response);
		return return_value;
	}
	
	public synchronized String checkValue(String user_id, String item_id, int number_of_days) {
		String count="";
		
		if(borrow.containsKey(user_id)) {
			count = "not ok";
		}
		else {
			count = "ok";
		}
		
		return count;
	}
	
	public synchronized String borrowItemData(String user_id, String item_id, int number_of_days) {
		Books book = book_shelf.get(item_id);
		ArrayList books = new ArrayList();
		String return_value="";
		int count=0;
		boolean flag = false;
		
		for(String entry: borrow.keySet()){
			if(entry.contains(user_id)) {
				ArrayList<Books> list = borrow.get(entry);
				for(Books b : list) {
					if(item_id.equalsIgnoreCase(b.getItemId())) {
						count++;
						break;
					}
				}
			}
		}
		System.out.println("Count after:::"+count);
		if(count>0) {
			return_value = "Book is already borrowed by the user.";
		}
		else {
			if(book_shelf.containsKey(item_id)) {
				if(book.getQuantity()>0) {
					for(String entry: borrow.keySet()){
							if(entry.contains(user_id)) {
								flag = true;
							}
							else {
								books.add(book);
								borrow.put(user_id, books);
								return_value = book.getItemId() +" is successfully borrowed.";
								flag = true;
								book.borrowBook();
							}
					}
				
			
					if(!flag) {
						books.add(book); 
						borrow.put(user_id, books); 
						book.borrowBook();
					}
				
					System.out.println(book.getItemId() +" is successfully borrowed.");
					return_value = book.getItemId() +" is successfully borrowed.";
				}
				else if(book.getQuantity()==0) {
					System.out.println("Book is not available to for borrowing.");
					return_value = "Book is not available to for borrowing.";
				}
			}
			else{
				System.out.println("Book you searched is not exist. Please enter correct book.");
				return_value = "Book you searched is not exist. Please enter correct book.";
			}
		}
		return return_value;
	}
	
	
	
	@Override
	public synchronized String findItem(String user_id, String item_name) {
		String req_message,reply1,reply2,reply3,combine_message;
		req_message = "2"+user_id+item_name;
		
		reply1 = findItemData(user_id, item_name);
		
		reply2 = sendMessage(2222, req_message);
	
		reply3 = sendMessage(3333, req_message);
		
		combine_message = reply1 +", " +reply2 +", " +reply3;
		System.out.println(combine_message);
		
		LocalDateTime current_time = LocalDateTime.now();
		String print_response = current_time+"--"+"FIND"+"--"+user_id +", "+item_name+"--"+combine_message+"\n";
		appendUsingFileWriter(path, print_response);
		return combine_message;
	
	}
	
	public synchronized String findItemData(String user_id, String item_name) {
		String reply = "";
		for(Map.Entry<String,Books> entry: book_shelf.entrySet()){
			Books book = entry.getValue();
			if(item_name.equalsIgnoreCase(book.getItemName())) {
					reply = book.getItemId() +" " +book.getQuantity();
				}
		}
		
		return reply;
	}
	
	
	
	@Override
	public synchronized String returnItem(String user_id, String item_id) {
		String server,return_value = "",req_message;
		req_message = "3"+user_id+item_id;
		server = item_id.substring(0, 3);
		if(server.equalsIgnoreCase("CON")) {
			try {
				return_value = returnItemData(user_id, item_id);
			}
			catch (Exception e) {
			}
		}
		else if(server.equalsIgnoreCase("MCG")) {
			try {
				return_value = sendMessage(2222, req_message);
			}
			catch (Exception e) {
			}
		}
		else if(server.equalsIgnoreCase("MON")) {
			try {
				return_value = sendMessage(3333, req_message);
			}
			catch (Exception e) {
			}
		}
		else {
			return_value = "Enter correct item ID.";
		}
		
		LocalDateTime current_time = LocalDateTime.now();
		String print_response = current_time+"--"+"RETURN"+"--"+user_id +", "+item_id+"--"+return_value+"\n";
		appendUsingFileWriter(path, print_response);
		return return_value;
	}
	
	public synchronized String returnItemData(String user_id, String item_id) {
		Books book = book_shelf.get(item_id);
		String return_value = "Book Returning Fails",return_value1 = "";
		for(String entry: borrow.keySet()) {
			if(entry.contains(user_id)) {
				ArrayList<Books> list = borrow.get(entry);
				for(Books b : list) {
					if(item_id.equalsIgnoreCase(b.getItemId())) {
						if(item_id.equalsIgnoreCase(book.getItemId())) {
							book.returnBook();
							System.out.println(book.getItemId() +" " +book.getItemName()+" is successfully returned.");
							return_value = "Successfully Returned";
							borrow.remove(user_id);
						}
						else if(!item_id.equalsIgnoreCase(book.getItemId())){
							return_value = "You don't have this book.";
						}
						else {
							return_value = "You don't have this book.";
						}
						
					}
					else if(!item_id.equalsIgnoreCase(b.getItemId())){
						return_value = "You don't have this book.";
					}
				}
			}
		}
		
		if(return_value.equalsIgnoreCase("Successfully Returned")) {
			int default_days = 10;
			if(!waitlist.isEmpty()) {
				if(waitlist.containsKey(item_id)) {
					ArrayList queue = waitlist.get(item_id);
					int new_quantity = book_shelf.get(item_id).getQuantity();
					for(int i = new_quantity;i>0;i--) {
						if(!queue.isEmpty()) {
							String temp_user_id = (String)queue.get(0);
							System.out.println(temp_user_id);
							try {
								return_value1 = borrowItem(temp_user_id, item_id,default_days);
							}
							catch (Exception e) {
								
							}
							
							if(return_value1.equalsIgnoreCase(item_id +" is successfully borrowed.")) {
								LocalDateTime current_time = LocalDateTime.now();
								String print_response = current_time+"--"+"BORROW"+"--"+temp_user_id +", "+item_id+", "+default_days+"--"+return_value+"\n";
								String path2 = path1+temp_user_id; 
								appendUsingFileWriter(path2, print_response);
							}
							else if(return_value1.equalsIgnoreCase("User can only borrow 1 item from other server.")) {
								LocalDateTime current_time = LocalDateTime.now();
								String print_response = current_time+"--"+"BORROW"+"--"+temp_user_id +", "+item_id+", "+default_days+"--"+return_value+"\n";
								String path2 = path1+temp_user_id; 
								appendUsingFileWriter(path2, print_response);
								i++;
							}	
							queue.remove(temp_user_id);
						}
						else {
							break;
						}
					}
					if(queue.isEmpty()){
						waitlist.remove(item_id);
					}
					else {
						waitlist.put(item_id, queue);
					}
					for(String entry: waitlist.keySet()) {
						ArrayList<String> value = waitlist.get(entry);
						System.out.println(entry+" "+value);
					}
					
				}
			}
		}
		if(return_value.equalsIgnoreCase("Book Returning Fails")) {
			System.out.println("Book was not borrowed by a user.");
		}
		for(String entry: waitlist.keySet()) {
			ArrayList<String> value = waitlist.get(entry);
			System.out.println(entry+" "+value);
		}
				
		return return_value;
	}
	
	
	@Override
	public synchronized String addToQueue(String user_id, String item_id) {
		String server,return_value = "",req_message;
		req_message = "4"+user_id+item_id;
		server = item_id.substring(0, 3);
		if(server.equalsIgnoreCase("CON")) {
			return_value = addToQueueData(user_id, item_id);
		}
		else if(server.equalsIgnoreCase("MCG")) {
			return_value = sendMessage(2222, req_message);
		}
		else if(server.equalsIgnoreCase("MON")) {
			return_value = sendMessage(3333, req_message);
		}
		else {
			return_value = "Enter correct item ID.";
		}
		System.out.println(return_value);
		LocalDateTime current_time = LocalDateTime.now();
		String print_response = current_time+"--"+"WAITING QUEUE"+"--"+user_id +", "+item_id+"--"+return_value+"\n";
		appendUsingFileWriter(path, print_response);
		return return_value;
	}
	
	public synchronized String addToQueueData(String user_id, String item_id) {
		int count = 0;
		String return_value="";
		ArrayList<String> users = new ArrayList<>();
		
		for(String entry: borrow.keySet()){
			if(entry.contains(user_id)) {
				ArrayList<Books> list = borrow.get(entry);
				for(Books b : list) {
					if(item_id.equalsIgnoreCase(b.getItemId())) {
						count++;
						break;
					}
				}
			}
		}
		if(count>0) {
			return_value = "Book is already borrowed by the user.";
		}
		else {
			if(waitlist.containsKey(item_id)) {
				ArrayList<String> value = waitlist.get(item_id);
				if(value.contains(user_id)) {
					return_value = "User is already in waitlist.";
				}
				else {
					value.add(user_id);
					waitlist.put(item_id, value);
					return_value = "Successfully added into existing waiting list.";
				}
			}
			else {
				users.add(user_id);
				waitlist.put(item_id, users);
				return_value = "Successfully added into new waiting list.";
			}
		}
		
		for(String entry: waitlist.keySet()) {
			ArrayList<String> value = waitlist.get(entry);
			System.out.println(entry+" "+value);
		}
		return return_value;
	}
	
	public void receive() {
		DatagramSocket aSocket = null;
		String user_id, item_name, received_data = "", item_id;
		int number_of_days;
		try {
			aSocket = new DatagramSocket(1111);
			
			System.out.println("Server 1111 Started............");
			while (true) {
				byte[] buffer = new byte[100000];// to stored the received data from
				// the client.
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				// Server waits for the request to
				// come------------------------------------------------------------------
				aSocket.receive(request);// request received
			
				
				String data = new String(request.getData()).trim();
				int i = request.getLength();
				String call = data.substring(0,1);
				if(call.equalsIgnoreCase("1")) {
					user_id = data.substring(1, 9);
					item_id = data.substring(9, 16);
					number_of_days = Integer.parseInt(data.substring(16, i));
					received_data = borrowItemData(user_id, item_id, number_of_days);
				}
				else if(call.equalsIgnoreCase("2")) {
					user_id = data.substring(1, 9);
					item_name = data.substring(9, i);
					received_data = findItemData(user_id, item_name);
				}
				else if(call.equalsIgnoreCase("3")) {
					user_id = data.substring(1, 9);
					item_id = data.substring(9, i);
					received_data = returnItemData(user_id, item_id);
				}
				else if(call.equalsIgnoreCase("4")) {
					user_id = data.substring(1, 9);
					item_id = data.substring(9, i);
					received_data = addToQueueData(user_id, item_id);
				}
				else if(call.equalsIgnoreCase("5")) {
					user_id = data.substring(1, 9);
					item_id = data.substring(9, 16);
					number_of_days = Integer.parseInt(data.substring(16, i));
					received_data = checkValue(user_id, item_id, number_of_days);
				}
				else if(call.equalsIgnoreCase("6")) {
					user_id = data.substring(1, 9);
					item_id = data.substring(9, i);
					received_data = checkAvailable(user_id, item_id);
				}
				else if(call.equalsIgnoreCase("7")) {
					user_id = data.substring(1, 9);
					item_id = data.substring(9, i);
					received_data = checkBorrow(user_id, item_id);
				}
				
				DatagramPacket reply = new DatagramPacket(received_data.getBytes(), received_data.length(), request.getAddress(),
						request.getPort());// reply packet ready
				aSocket.send(reply);// reply sent
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
		
	}
	
	public String sendMessage(int serverPort, String req_message) {
		DatagramSocket aSocket = null;
		String reply_message = "";
		try {
			aSocket = new DatagramSocket();
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(req_message.getBytes(), req_message.length(), aHost, serverPort);
			aSocket.send(request);
			System.out.println("Request message sent from the client to server with port number " + serverPort + " is: "
					+ new String(request.getData()));
			byte[] buffer = new byte[100000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
	
			aSocket.receive(reply);
			reply_message = new String(reply.getData()).trim();
			System.out.println("Reply received from the server with port number " + serverPort + " is: "
					+ reply_message);
			
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
		return reply_message;
	}
	
	public void activeServer() throws RemoteException {
		ConcordiaServer stub = new ConcordiaServer();
		Endpoint endpoint = Endpoint.publish("http://localhost:1111/comp", stub);
		System.out.println("Concordia's server is started.");
	}
		
	public static void main(String arg[]) throws RemoteException, MalformedURLException, IOException {
		path = path + "\\ConcordiaServer" ;
		File file = new File(path);
		file.createNewFile();
		ConcordiaServer con = new ConcordiaServer();
		con.addItem("CONM1111", "CON1188", "Java", 4);
		con.addItem("CONM2222", "CON1144", "C", 0);
		con.addItem("CONM2222", "CON1122", "C++", 1);
		
		Runnable task1 = () -> {
			con.receive();
		};
		Runnable task2 = () -> {
			try {
				con.activeServer();
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		
		Thread thread1 = new Thread(task1);
		Thread thread2 = new Thread(task2);

		thread1.start();
		thread2.start();

	}
	
	public static void appendUsingFileWriter(String filePath, String text) {
		File file = new File(filePath);
		FileWriter fr = null;
		try {
			fr = new FileWriter(file, true);
			fr.write(text);
		} 
		catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized String exchangeItem(String user_id, String new_item_id, String old_item_id) {
		String server_new, server_old, return_value1 = "", return_value2 = "",return_value3 = "", return_value4 = "", return_value5 = "", return_value6 = "", req_message1, req_message2,  req_message3, return_message = "";
		int default_days = 10;
		
		req_message1 = "6"+user_id+new_item_id;      // checks availability
		server_new = new_item_id.substring(0, 3);
		if(server_new.equalsIgnoreCase("CON")) {
			return_value1 = checkAvailable(user_id, new_item_id);
		}
		else if(server_new.equalsIgnoreCase("MCG")) {
			return_value1 = sendMessage(2222, req_message1);
		}
		else if(server_new.equalsIgnoreCase("MON")) {
			return_value1 = sendMessage(3333, req_message1);
		}
		
		req_message2 = "7"+user_id+old_item_id;      // checks borrow
		server_old = old_item_id.substring(0, 3);
		if(server_old.equalsIgnoreCase("CON")) {
			return_value2 = checkBorrow(user_id, old_item_id);
		}
		else if(server_old.equalsIgnoreCase("MCG")) {
			return_value2 = sendMessage(2222, req_message2);
		}
		else if(server_old.equalsIgnoreCase("MON")) {
			return_value2 = sendMessage(3333, req_message2);
		}
		
		req_message3 = "5"+user_id+new_item_id+default_days;      // checks borrow single book
		server_new = new_item_id.substring(0, 3);
		if(server_new.equalsIgnoreCase("CON")) {
			return_value6 = "ok";
		}
		else if(!server_new.equalsIgnoreCase("CON")) {
			
			if(server_new.equalsIgnoreCase("MCG")) {
				return_value6 = sendMessage(2222, req_message3);
			}
			else if(server_new.equalsIgnoreCase("MON")) {
				return_value6 = sendMessage(3333, req_message3);
			}
			
			if(server_old.equalsIgnoreCase(server_new) && return_value6.equalsIgnoreCase("not ok")) {
				return_value6 = "ok";
			}
			
		}
		System.out.println("Check availability:::"+return_value1);
		System.out.println("Check borrow:::"+return_value2);
		System.out.println("Check single book borrow:::"+return_value6);
		
		if(return_value1.equalsIgnoreCase("ok") && return_value2.equalsIgnoreCase("ok") && return_value6.equalsIgnoreCase("ok")) {
			return_value4 = returnItem(user_id, old_item_id);
			
			return_value3 = borrowItem(user_id, new_item_id, default_days);
			
			System.out.println(return_value4+"                  "+return_value3); 
			
			return_message = "Successfully exchange "+new_item_id+" with "+old_item_id;
//			if(return_value4.equalsIgnoreCase("Successfully Returned") && return_value3.equalsIgnoreCase(new_item_id+" is successfully borrowed.")) {
//				return_message = "Successfully exchange "+new_item_id+" with "+old_item_id;
//			}
//			else {
//				return_message = "Fail exchange "+new_item_id+" with "+old_item_id;
//				if(return_value3.equalsIgnoreCase("User can only borrow 1 item from other server.")) {
//					return_value5 = borrowItem(user_id, old_item_id, default_days);
//					System.out.println(return_value5);
//				}
//			}
			
			
		}
		else {
			return_message = "Fail exchange "+new_item_id+" with "+old_item_id;
		}
		
		System.out.println(return_message);
		LocalDateTime current_time = LocalDateTime.now();
		String print_response = current_time+"--"+"EXCHANGE"+"--"+user_id +", "+", "+new_item_id+", "+old_item_id+"--"+return_message+"\n";
		appendUsingFileWriter(path, print_response);
		return return_message;
	}
	
	public synchronized String checkAvailable(String user_id, String new_item_id) {
		String check = "";
		Books book = book_shelf.get(new_item_id);

		if(book_shelf.containsKey(new_item_id)) {
			if(book.getQuantity()>0) {
				check = "ok";
			}
			else {
				check = "not ok";
			}
		}
		else {
			check = "not ok";
		}
		
		return check;
	}

	public synchronized String checkBorrow(String user_id, String old_item_id) {
		String check;
		int count = 0;

		for(String entry: borrow.keySet()){
			if(entry.contains(user_id)) {
				ArrayList<Books> list = borrow.get(entry);
				for(Books b : list) {
					if(old_item_id.equalsIgnoreCase(b.getItemId())) {
						count++;
						break;
					}
				}
			}
		}
		if(count>0) {
			check = "ok";
		}
		else {
			check = "not ok";
		}
		
		return check;
	}
}
