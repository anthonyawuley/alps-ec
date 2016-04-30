package ec.fsalps.probability;

import java.util.ArrayList;
import java.util.Map.Entry;

import ec.fsalps.Roulette;
/**
 * This requires direct conversion of terminal frequency distribution into terminal prob- abilities
 * Two terminal sets (x and y) with a frequency count of 20 and 5 respectively will produce 
 * probability values of 0.8 and 0.2 respectively.
 * 
 * @author Anthony Awuley
 *
 */
public class NormalFrequency extends Roulette{

	
	public String toString()
	{
		return this.getClass().getName();
	}
	
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
			
			roulette.add(nodeEntry);
		}
		return roulette;
	}
	
	
	

}
