import java.util.concurrent.Semaphore;
import java.util.*;
import java.io.*;

public class Maekawa extends Thread
{
	private Semaphore communication_semaphore; //common semaphore that it is used in the communication system
	private Semaphore maekawa_semaphore; //maekawa sempahore
	private int k;				//K number
	private int process;		//the identification of internal process
	private int internal_clock; //when the process wanted to used the mutual exclusion, it saved the clock state of this message
	private String state; 		 	//the process state
	private boolean voted;			//the voted process state
	private int reply_quantity;    //this variable saves the reply quantity that the maekawa received in some enter petition
	private int[] voters; //the voting processes
	//communication system
	private Transmissor transmissor; //transmissor object
	private Queue<Message> queue_deliver;//maekawa does not have direct connection with the receptor object
										 //the maekawa used a queue like a interface between the receptor object
										 //and the maekawa object
										
	private Queue<Message> requests;  //queue of the request messages
	
	/*
	 *3 types of messages:
	 *0. request
	 *1. reply
	 *2. released
	 *
	 */
	public Maekawa(int t_process, Transmissor t_transmissor, Queue<Message> t_queue, Semaphore t_semaphore)
	{
		transmissor=t_transmissor; //receives the transmissor object
		queue_deliver=t_queue;     //receives the  queue deliver
		internal_clock=0;          //initializes the internal clock integer
		process=t_process;         //process number
		communication_semaphore=t_semaphore; //receives the communication semaphore
		maekawa_semaphore= new Semaphore(2); //creates a new semaphore
		reply_quantity=0; //initializes
		k=2; //initializes k
		voters=new int[k]; //voters vector
		voted=false; //initializes in false (Maekawa algorithm)
		state="RELEASED";//initializes in RELEASED (Maekawa algorithm)
		requests = new LinkedList<Message>(); //FIFO request queue
		try 
		{
			
			/*Voters file*/
			String voters_information= new String();
			String file_name="process/"+process+"/Voters.txt";
			FileReader fileReader = new FileReader(file_name);
	        BufferedReader bufferedReader = new BufferedReader(fileReader);
	        String temp=null;
	        while((temp = bufferedReader.readLine()) != null)
	        {
	            voters_information+=temp;
	        }  
	        String[] temporal= voters_information.split(" ");
	        int len = temporal.length;
	        for(int i=0;i<len;i++)
	        {
	        	voters[i]=Integer.parseInt(temporal[i]);
	        	
	        }
	        /*Voters file ended*/
	        System.out.println("The maekawa object was correctly builder");
	        bufferedReader.close();  
		}
		catch(Exception e)
		{
			System.out.println("Maekawa Builder Exception: "+e.getMessage());
		}
	}
	
	public boolean enter(int internal_event)
	{
		try
		{
			reply_quantity=0;
			
			maekawa_semaphore.acquire(); //Maekawa mutual exclusion (maekawa thread used this variable too)
			state="WANTED"; //change state to WANTED
			internal_clock= transmissor.clock(); //lamport clock value
			maekawa_semaphore.release();  //release
			for(int i=0;i<k;i++) {
				
				Message message=new Message(process,voters[i],0,internal_clock); //creates the request message
				transmissor.send(message); //sends a message
			}
			boolean condition=false;
			//wait until all the voters respond
			while(!condition)
			{
				maekawa_semaphore.acquire(); //Maekawa mutual exclusion (maekawa thread used this variable too)
				if(reply_quantity==k)condition=true; //when reply quantity is equal than k, the main process stops  waiting
				maekawa_semaphore.release();	
			}
			maekawa_semaphore.acquire(); //Maekawa mutual exclusion (maekawa thread used this variable too)
			state="HELD"; //change state to HELD
			maekawa_semaphore.release();
			
		}
		catch(Exception e)
		{
			return false; //some problem with the maekawa algorithm
		}
	return true;
	}
	
	public boolean exit(int internal_event)
	{
		try
		{
			maekawa_semaphore.acquire(); //Maekawa mutual exclusion (maekawa thread used this variable too)
			state="RELEASED"; //change state to RELEASED
			maekawa_semaphore.release();
			internal_clock= transmissor.clock(); //lamport clock value
			for(int i=0;i<k;i++) {
				
				Message message=new Message(process,voters[i],2,internal_clock); //creates the released message
				transmissor.send(message); //send the message
			}
		}
		catch(Exception e)
		{
			return false; //some problem with the maekawa algorithm
		}
		return true;
	}
	
	public void run()
	{
		while(true)
		{
			try
			{	
				if(!queue_deliver.isEmpty()) //checks if the queue is not empty
				{
					communication_semaphore.acquire(); //mutual exclusion (shared that resource with receptor object)
					Message  message=queue_deliver.poll(); //deliveries the first message  and delete it (FIFO queue)
					communication_semaphore.release();
					
					System.out.println("Message:");
			    	        System.out.println("Source: P"+message.Source);
					if(message.Type==0) System.out.println("type: Request");
					else if(message.Type==1) System.out.println("type: Reply");
					else System.out.println("type: Release");
				
					System.out.println("Clock: "+message.clock);
						
					
					//maekawa algorithm core
					switch(message.Type) 
					{
						case 0:  //REQUEST
							maekawa_semaphore.acquire();
							if(state!="HELD" && !voted)
							{
								if(
										state=="RELEASED" || 
										(
												state=="WANTED" &&
												(
														message.clock<internal_clock ||
														(
																message.clock==internal_clock && 
																process>=message.Source
														)
												)
										 )
									)
								{
							    
								if(state=="RELEASED")System.out.println("RELEASED-"+process);
								if(state=="WANTED") 
								{
								System.out.println("internal Process P"+process+":"+internal_clock+"   External Process P"+message.Source+":"+message.clock);
								}
								
								    voted=true;
							        maekawa_semaphore.release();
									Message reply=new Message(process,message.Source,1,transmissor.clock());
									transmissor.send(reply);
	
								}
								else 
								{
									requests.add(message);
									maekawa_semaphore.release();
								}
							}
							else 
							{
								requests.add(message);
								maekawa_semaphore.release();
							}
							break;
						case 1: //REPLY
							maekawa_semaphore.acquire();
							reply_quantity++;
							maekawa_semaphore.release();
							break;
						case 2: //RELEASED
							maekawa_semaphore.acquire();
							if(!requests.isEmpty())
							{
								Message request =  requests.poll();
								Message reply=new Message(process,request.Source,1,transmissor.clock());
								transmissor.send(reply);

								voted=true;
								maekawa_semaphore.release();
								
							}
							else
							{
								voted=false;
								maekawa_semaphore.release();
							}
							break;
						default: 
							System.out.println("This message does not have sense for the Maekawa algorithm");
							break;
					}
					
					
				}
				
			}
			catch(Exception e)
			{
				System.out.println("the Maekawa Threads has had an Exception: "+e.getMessage());
			}
		}
	
	}
}
