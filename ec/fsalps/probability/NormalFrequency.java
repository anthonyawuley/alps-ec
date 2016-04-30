package ec.fsalps.probability;

import java.util.ArrayList;
import java.util.Map.Entry;

import ec.alps.fsalps.Roulette;

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
			//System.out.println("::::"+ entry.getKey() + " VAL: "+ entry.getValue() + "--" + nodeEntry.get(1)); 

			roulette.add(nodeEntry);
		}
		return roulette;
	}
	
	
	

}
