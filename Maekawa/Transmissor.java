
import java.util.concurrent.Semaphore;	
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
public class Transmissor 
{
	
	private ORB orb; //object remote broker
	private int process; //process number
	private receiver[] receptors; //Receptor vector ( 3 Corba Server in this problem)
	//private org.omg.CORBA.Object[] objects_remote;
	private  Semaphore semaphore;  //this semaphore is used in all 
	private Lamport lamport;
	private int process_quantity; 
	public  Transmissor(int t_process,Lamport t_lamport,Semaphore t_semaphore)
	{
		lamport=t_lamport; //receives the lamport object
		process=t_process; //receives the process number
		semaphore=t_semaphore; //receives the communication semaphore
		process_quantity=3; //3 process
		try
		{

			String[] args=new String[0]; 
			orb= ORB.init(args,null); //the object remote broker is initiated
			//name service remote reference
			org.omg.CORBA.Object NameServiceRemoteReference= orb.resolve_initial_references("NameService"); 
			//namingContext narrow (using the NamingContextHelper)
			NamingContext NameService = NamingContextHelper.narrow(NameServiceRemoteReference); 
			receptors = new receiver[process_quantity]; //create a vector of receptors
			boolean synchronization=false;
			System.out.println("Synchronization process has started");
			while(!synchronization)
			{
				//if the process 1,2 and 3 exits in the Name Service server
				//the synchronization is ended
				synchronization=true;
				for(int i=1;i<=process_quantity;i++)
				{
					try 
					{
						
						NameComponent[] path = { new NameComponent(("P"+i),"Object")};	
						//searches the remote object reference  for the processes 
						org.omg.CORBA.Object remote_object= NameService.resolve(path); 
						//narrows the receptor object (using the receiverhelper.narrow)
						receptors[i-1]=receiverHelper.narrow(remote_object); 
						
					}
					catch(Exception e) //this exception happens when some process does not exist in the name service
					{
						synchronization=false;
						//if some process is not in the name service server
						//the synchronization process is not ended yet
						
					}
							
				}
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in the Transmissor class constructor: "+e.getMessage());
		}

	}
	
	public boolean send(Message message)
	{
		try
		{
             //sends a message
			int receptor=message.Destination;
			receptors[receptor-1].send(message);
			return true;
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in the Transmissor class of the procces "+process+": "+e.getMessage());
		}
		return false;
	}
	
	public int clock()
	{
		//increase the lamport clock value.
		int clock_value=0;
		try
		{
		semaphore.acquire();
		clock_value=lamport.increase();
		semaphore.release();
		}
		catch(Exception e)
		{
			System.out.println("Exception in the Transmissor class of the procces "+process+": "+e.getMessage());
		}
		return clock_value;
	}
	

}


