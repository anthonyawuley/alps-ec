package ec.alps.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;


public class Roulette {

	//private int node  =  -1; //set value to index selected in roulette
	private Map<String, Double> map;      //loads default node probability settings
	
	private ArrayList<ArrayList<Double>> roulette;

	public Roulette(Map<String, Double> m) 
	{
		this.map = m;
		convertFreqToProb();
	}


	/**
	 * @author anthony
	 * 
	 * Explanation taken from http://stackoverflow.com/questions/298301/roulette-wheel-selection-algorithm
	 * Assume you have 10 items to choose from and you choose by generating a random number between 0 and 1. 
	 * You divide the range 0 to 1 up into ten NON-OVERLAPPING segments, each proportional to the fitness of 
	 * one of the ten items. For example, this might look like this:<br><br>
	 * 
	 * 0    - 0.30 is item 1<br>
	 * 0.3  - 0.40 is item 2<br>
	 * 0.4  - 0.50 is item 3<br>
	 * 0.5  - 0.57 is item 4<br>
	 * 0.57 - 0.63 is item 5<br>
	 * 0.63 - 0.68 is item 6<br>
	 * 0.68 - 0.80 is item 7<br>
	 * 0.8  - 0.85 is item 8<br>
	 * 0.85 - 0.98 is item 9<br>
	 * 0.98 - 1.00 is item 10<br><br>
	 * 
	 * This is your roulette wheel. Your random number between 0 and 1 is your spin. 
	 * If the random number is 0.46, then the chosen item is item 3. If it's 0.92, then it's item 9.
	 * 
	 */
	public ArrayList<ArrayList<Double>> convertFreqToProb()
	{
		roulette =  new ArrayList<>();
		double total = totalFrequency(map);
		int c = 0;
		for (Entry<String, Double> entry : map.entrySet()) 
		{ 
			ArrayList<Double> nodeEntry = new ArrayList<>();
			if(total==0) //if no probability parameter is set for all nodes, use uniform distribution
				nodeEntry.add(1.0 / (double) map.size());
			else
				nodeEntry.add((double) entry.getValue()/(double) total);
				
			if(c==0)
				nodeEntry.add((double) (nodeEntry.get(0))); //set upper bound for first node
			else //set upper bound for other nodes by adding current frequency to total  
				nodeEntry.add((double) (roulette.get(c-1).get(1) + (double) nodeEntry.get(0)));
			
			c++; 
			//System.out.println("::::"+ entry.getKey() + " VAL: "+ entry.getValue() + "--" + nodeEntry.get(1)); 
			
			roulette.add(nodeEntry);
		}
		return roulette;
	}
	
	

	/**
	 * @param map
	 * @return
	 */
	public int totalFrequency(Map<String, Double> map)
	{
		int total = 0;
		for (Entry<String, Double> entry : map.entrySet()) 
			total += entry.getValue(); //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue()); 
		
		return total;
	}


	/**
	 * @param prob generated probability used in selection
	 * @return int that represents a selected index for a node
	 */
	public int spin(double prob) 
	{
		for(int i=0;i<roulette.size();i++ )
		{
			if(i==0)
			{
				if(prob<=roulette.get(i).get(1))
					return i;
			}
			else
			{
				if(prob>roulette.get(i-1).get(1) && prob<=roulette.get(i).get(1))
					return i;  
			}
		} 
		return -1; //Unsuccessful : Very unlikely
	}


	/**
	 *  Gets the node the ball landed on.
	 *  @return the number
	 */
	public String getNode(int index) 
	{
		int c = 0;
		for (Entry<String, Double> entry : map.entrySet()) 
		{ 
			if(index == c)
				return entry.getKey();
			c++;
		}
		return "unknown"; //code is in error
	}


}
