

public class Lamport 
{
private int counter;


public Lamport() 
	{

		counter=0;
		System.out.println("The lamport clock was correctly created");
	
	};
	
public int increase()
	{
		counter++;
		//System.out.println("LAMPORT: "+counter);
		return counter;
	};
	
public int evaluate(int event)
	{
		if(event>counter) counter=event;
		counter++;
		//System.out.println("LAMPORT EVALUATE: "+counter);
		return counter;
	}
}



