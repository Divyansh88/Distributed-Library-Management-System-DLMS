package Servers;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)

/**
 * @author Divyansh
 *
 */
public interface ServerInterface extends Remote{
	/**
	 * @param manager_id
	 * @param item_id
	 * @param item_name
	 * @param quantity
	 * @return
	 * @throws RemoteException
	 */
	public String addItem(String manager_id,String item_id,String item_name,int quantity) throws RemoteException;

	/**
	 * @param manager_id
	 * @param item_id
	 * @param quantity
	 */
	public String removeItem(String manager_id, String item_id, int quantity) throws RemoteException;

	/**
	 * @param manager_id
	 */
	public String listItemAvailability(String manager_id) throws RemoteException;
	/**
	 * @param user_id
	 * @param item_id
	 * @param number_of_days
	 */
	public String borrowItem(String user_id, String item_id, int number_of_days) throws RemoteException;

	/**
	 * @param user_id
	 * @param item_name
	 */
	public String findItem(String user_id, String item_name) throws RemoteException;

	/**
	 * @param user_id
	 * @param item_id
	 * @return 
	 */
	public String returnItem(String user_id, String item_id) throws RemoteException;

	/**
	 * @param user_id
	 * @param item_id
	 */
	public String addToQueue(String user_id, String item_id) throws RemoteException;

	/**
	 * @param user_id
	 * @param new_item_id
	 * @param old_item_id
	 * @return
	 */
	public String exchangeItem(String user_id, String new_item_id, String old_item_id) throws RemoteException;

}

