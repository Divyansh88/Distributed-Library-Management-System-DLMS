package DLMSApp;


/**
* DLMSApp/ServerInterfacePOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ServerInterface.idl
* Saturday, 23 March, 2019 10:38:39 PM EDT
*/

public abstract class ServerInterfacePOA extends org.omg.PortableServer.Servant
 implements DLMSApp.ServerInterfaceOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("removeItem", new java.lang.Integer (0));
    _methods.put ("addItem", new java.lang.Integer (1));
    _methods.put ("listItemAvailability", new java.lang.Integer (2));
    _methods.put ("borrowItem", new java.lang.Integer (3));
    _methods.put ("findItem", new java.lang.Integer (4));
    _methods.put ("returnItem", new java.lang.Integer (5));
    _methods.put ("addToQueue", new java.lang.Integer (6));
    _methods.put ("exchangeItem", new java.lang.Integer (7));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // DLMSApp/ServerInterface/removeItem
       {
         String manager_id = in.read_string ();
         String item_id = in.read_string ();
         int quantity = in.read_long ();
         String $result = null;
         $result = this.removeItem (manager_id, item_id, quantity);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 1:  // DLMSApp/ServerInterface/addItem
       {
         String manager_id = in.read_string ();
         String item_id = in.read_string ();
         String item_name = in.read_string ();
         int quantity = in.read_long ();
         String $result = null;
         $result = this.addItem (manager_id, item_id, item_name, quantity);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 2:  // DLMSApp/ServerInterface/listItemAvailability
       {
         String manager_id = in.read_string ();
         String $result = null;
         $result = this.listItemAvailability (manager_id);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 3:  // DLMSApp/ServerInterface/borrowItem
       {
         String user_id = in.read_string ();
         String item_id = in.read_string ();
         int number_of_days = in.read_long ();
         String $result = null;
         $result = this.borrowItem (user_id, item_id, number_of_days);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 4:  // DLMSApp/ServerInterface/findItem
       {
         String user_id = in.read_string ();
         String item_name = in.read_string ();
         String $result = null;
         $result = this.findItem (user_id, item_name);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 5:  // DLMSApp/ServerInterface/returnItem
       {
         String user_id = in.read_string ();
         String item_id = in.read_string ();
         String $result = null;
         $result = this.returnItem (user_id, item_id);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 6:  // DLMSApp/ServerInterface/addToQueue
       {
         String user_id = in.read_string ();
         String item_id = in.read_string ();
         String $result = null;
         $result = this.addToQueue (user_id, item_id);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 7:  // DLMSApp/ServerInterface/exchangeItem
       {
         String user_id = in.read_string ();
         String new_item_id = in.read_string ();
         String old_item_id = in.read_string ();
         String $result = null;
         $result = this.exchangeItem (user_id, new_item_id, old_item_id);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:DLMSApp/ServerInterface:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public ServerInterface _this() 
  {
    return ServerInterfaceHelper.narrow(
    super._this_object());
  }

  public ServerInterface _this(org.omg.CORBA.ORB orb) 
  {
    return ServerInterfaceHelper.narrow(
    super._this_object(orb));
  }


} // class ServerInterfacePOA