import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.Scanner;

public class Process 
{
	public static void main(String[] args)
	{
		Scanner scanner = new Scanner(System.in);
		Lamport clock=new Lamport(); //lamport clock
		Semaphore semaphore= new Semaphore(1); //communication system semaphore
		Queue<Message> queue=  new LinkedList<Message>();//queue
		int process = Integer.parseInt(args[0]); //identification process
		if(process ==1 || process ==2 || process ==3 )
		{
			try
			{
				System.out.println("The process "+process+" is starting the receptor thread");
				Receptor receptor=new Receptor(process,semaphore,clock,queue); 
				receptor.start();
				Transmissor transmissor = new Transmissor(process,clock,semaphore);
				System.out.println("The Communication system is working");
				Maekawa maekawa = new Maekawa(process,transmissor,queue,semaphore);
				maekawa.start();
				
	            /*FILE*/
				String petitions_information= new String();
				String file_name="process/"+process+"/Petitions.txt";
				FileReader fileReader = new FileReader(file_name);
		        BufferedReader bufferedReader = new BufferedReader(fileReader);
		        String temp=null;

		        while((temp = bufferedReader.readLine()) != null)
		        {
		        	petitions_information+=temp;
		        }

		        String[] temporal= petitions_information.split(" ");
		        int len = temporal.length;
		        bufferedReader.close();  
		        /*END FILE*/
		        for(int i=0;i<len;i++)
		        {
		        	int request=Integer.parseInt(temporal[i]);
		        	maekawa.enter(request);
		        	System.out.println("Mutual exclusion request: P"+request+process);
		        	scanner.nextLine();// bloqueio no scanner esperando por qualquer toque no teclado
		        	System.out.println("Releasing request: P"+request+process); 
		        	maekawa.exit(request);
		        }
		        System.out.println("The request lists was ended");
		        scanner.close();

		     	System.out.println("The program is ended");
		        maekawa.join();
		        receptor.join();
		     	
				
			}
			catch(Exception e)
			{
				System.out.println("Exception in the main process of  "+process+" "+e.getMessage());
			}
			
		}
		else System.out.println("The process is  not a correct process");
	}

}
