package ec.alps.fsalps.probability;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import ec.alps.fsalps.Roulette;

public class UniformFrequency extends Roulette{

	
	public String toString()
	{
		return this.getClass().getName();
	}
	
	/**
	 * 
	 */
	public ArrayList<ArrayList<Double>> convertFreqToProb()
	{
		roulette =  new ArrayList<>();
		/* make a copy of map before modification */
		Map<String, Double> mapcopy  = copy(map);
		/* computer uniform average frequency by modifying mapcopy
		 * this function must be called before total*/
		mapcopy = computeUniformAverageFreq(mapcopy);
		/* this is the total of averge + uniform */
		double total = totalFrequency(mapcopy);
		
		int c = 0;
		for (Entry<String, Double> entry : mapcopy.entrySet()) 
		{ 
			ArrayList<Double> nodeEntry = new ArrayList<>();
			
			/* if no probability parameter is set for all nodes, and its initialization for
			 * layer 0 during the begning of evolution,  use uniform distribution
			 */
			if(total==0) 
				nodeEntry.add(1.0 / (double) mapcopy.size());
			else
				nodeEntry.add((double) entry.getValue()/(double) total);
			
			if(c==0)
				nodeEntry.add((double) (nodeEntry.get(0))); //set upper bound for first node
			else //set upper bound for other nodes by adding current frequency to total  
				nodeEntry.add((double) (roulette.get(c-1).get(1) + (double) nodeEntry.get(0)));
			c++; 
			System.out.println("::::"+ entry.getKey() + " VAL: "+ entry.getValue() + "--" + nodeEntry.get(1)); 

			roulette.add(nodeEntry);
		}
		return roulette;
	}
	
	
	/**
	 * computerUniformAverageFreq(..) calculates the average of all mapcopy values
	 * and adds the average to the original values
	 * @param mapcopy
	 */
	public Map<String, Double> computeUniformAverageFreq(Map<String, Double> mapcopy)
	{
		double average = totalFrequency(mapcopy)/(double)mapcopy.size();
		//System.out.println("Total: "+ totalFrequency(mapcopy) + " SIZE:"+mapcopy.size());
		for (Entry<String, Double> entry : mapcopy.entrySet()) {
			//System.out.print("original " + entry.getValue() + " New: ");
			mapcopy.put(entry.getKey(), entry.getValue() + average);
		    //System.out.println(mapcopy.get(entry.getKey()) + "---------" + average);	
		}
		return mapcopy;
		
	}
	
	

}
