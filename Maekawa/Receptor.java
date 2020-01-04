
import java.util.Queue;
import java.util.concurrent.Semaphore;	
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;


public class Receptor extends Thread
{
	
	private ORB orb; //Object remote broker object
	private POA poa; //portable object adapter object
	private int process; //process id
	private receiverImpl receiver; //receiverImplementation object
	private Semaphore semaphore; //semaphore object
	private Queue<Message> queue; // queue delivery object
	private Lamport lamport;  //lamport clock
	private org.omg.CORBA.Object CorbaRX; //the Corba remote object
	public Receptor(int t_process,Semaphore t_semaphore,Lamport t_lamport, Queue<Message> t_queue)
	{
		
		//the receptor object constructor receives the semaphore object , 
		//the queue delivery object  and  the lamport clock object  of the principal thread
		semaphore=t_semaphore;
		lamport=t_lamport;
		try
		{
		queue=t_queue;
		process=t_process;
		String[] args= new String[0];
		//init the object remote broker
		orb = ORB.init(args,null);
		// it narrows  the poa  object  using   the orb.resolve_initial_references whose name is "RootPOA"
		poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
		//activating of poa manager
		poa.the_POAManager().activate();
		//create a new receiver implementation
		receiver=new receiverImpl(semaphore,lamport,process,queue);
		//it creates the Corba object reference
		CorbaRX=poa.servant_to_reference(receiver);
		//it takes the Nameservice remote reference using the orb.resolve_initial_references
		org.omg.CORBA.Object remote_nameservice = orb.resolve_initial_references("NameService");
		//it narrows  the NamingContext object using the NamingContextHelper
		NamingContext namingContext = NamingContextHelper.narrow(remote_nameservice);
		//it creates the new path for the receiverImpl remote reference
		NameComponent[] path= {new NameComponent("P"+process,"Object")};
		//it adds the new remote reference in the name service
		namingContext.rebind(path, CorbaRX);
		//start the thread

		}
		catch(Exception e) { 
			System.err.println("Error: " + e);
			e.printStackTrace(System.out);
		}
	
	}
	
	public void run()
	{	
		try
		{
			//run the "Corba server"
			orb.run();
		}
		catch(Exception e) { 
			System.err.println("Error: " + e);
			e.printStackTrace(System.out);
		}
	}
	

}



