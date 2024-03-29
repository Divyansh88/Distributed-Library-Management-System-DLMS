package DLMSApp;


/**
* DLMSApp/ServerInterfaceOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ServerInterface.idl
* Saturday, 23 March, 2019 10:38:39 PM EDT
*/

public interface ServerInterfaceOperations 
{
  String removeItem (String manager_id, String item_id, int quantity);
  String addItem (String manager_id, String item_id, String item_name, int quantity);
  String listItemAvailability (String manager_id);
  String borrowItem (String user_id, String item_id, int number_of_days);
  String findItem (String user_id, String item_name);
  String returnItem (String user_id, String item_id);
  String addToQueue (String user_id, String item_id);
  String exchangeItem (String user_id, String new_item_id, String old_item_id);
} // interface ServerInterfaceOperations
