

import java.util.Queue;
import java.util.concurrent.Semaphore;

public class receiverImpl extends receiverPOA
{
	//this semaphore is used when  the receiver interface is using  the lamport clock
	private Semaphore semaphore; 
	//the Lamport object
	private Lamport lamport;
	//the queue_delivery, when a message arrived is insert in this object.
	private Queue<Message> queue;
	//the process id
	private int  process;
	
	
	public receiverImpl(Semaphore t_semaphore,Lamport t_lamport,int t_process, Queue<Message>  t_queue)  
	{ 
		//the receiver object constructor receives the semaphore object , 
		//the queue delivery object  and  the lamport clock object  of the principal thread
		queue=t_queue;
		process=t_process;
		lamport=t_lamport;
		semaphore=t_semaphore;
		System.out.println("The ReceiverImpl of "+process+" was correctly created");
	}
	public void send (Message message) 
	{
		
		//the send function is the only interface open to be used to the transmitter 
		try 
		{
			semaphore.acquire();
			//when it receives a new message, it inserts the new clock time, 
			//this clock is a critical section and for this reason 
			//it used the semaphore to the protect it
			lamport.evaluate(message.clock); //evaluates a new lamport value
			//it inserts the message in the queue delivery
			queue.add(message);
			semaphore.release();
		}
		catch(Exception e) 
		{
			System.out.print("Exception in Receiver Implementation: "+e.getMessage());
		}
		
		
	}
}
