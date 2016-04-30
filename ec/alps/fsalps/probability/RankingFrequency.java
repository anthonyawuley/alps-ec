package ec.alps.fsalps.probability;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import ec.alps.fsalps.Roulette;
import ec.alps.util.MapUtil;

public class RankingFrequency extends Roulette{

	
	
	public String toString()
	{
		return this.getClass().getName();
	}
	
	
	
	public ArrayList<ArrayList<Double>> convertFreqToProb()
	{
		roulette =  new ArrayList<>();
		
		Map<String, Double> mapcopy  = copy(map);
		/* perform reverse ranking and change values to ranks*/
		reverseRanking(mapcopy);
		/* this is the total of averge + uniform */
		double total = totalFrequency(mapcopy);
		
		int c = 0;
		for (Entry<String, Double> entry : mapcopy.entrySet()) 
		{ 
			ArrayList<Double> nodeEntry = new ArrayList<>();
			if(total==0) //if no probability parameter is set for all nodes, use uniform distribution
				nodeEntry.add(1.0 / (double) mapcopy.size());
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
	 * this is used to sort the <b>unsortedMap</b> such that
	 * elements(values) in the unsorted map are assigned their 
	 * respective rank values <br>
	 * Unsort Map...... <br>
     * [Key] : f [Value] : 9 <br>
     * [Key] : d [Value] : 1 <br>
     * [Key] : e [Value] : 9 <br>
     * [Key] : b [Value] : 5 <br>
     * [Key] : c [Value] : 8 <br>
     * [Key] : a [Value] : 6 <br>
     * [Key] : n [Value] : 99 <br>
     * [Key] : m [Value] : 2 <br>
     * [Key] : j [Value] : 50 <br>
     * [Key] : z [Value] : 10 <br>
     * [Key] : y [Value] : 8 <br>
     *  <br>
     * is first sorted to
     * Sorted Map...... <br>
     * [Key] : d [Value] : 1 <br>
     * [Key] : m [Value] : 2 <br>
     * [Key] : b [Value] : 5 <br>
     * [Key] : a [Value] : 6 <br>
     * [Key] : c [Value] : 8 <br>
     * [Key] : y [Value] : 8 <br>
     * [Key] : f [Value] : 9 <br>
     * [Key] : e [Value] : 9 <br>
     * [Key] : z [Value] : 10 <br>
     * [Key] : j [Value] : 50 <br>
     * [Key] : n [Value] : 99 <br> 
     *  <br> 
     * FINALLY STORED AS
	 * Unsort Map...... <br>
     * [Key] : f [Value] : 7 <br>
     * [Key] : d [Value] : 1 <br>
     * [Key] : e [Value] : 7 <br>
     * [Key] : b [Value] : 3 <br>
     * [Key] : c [Value] : 5 <br>
     * [Key] : a [Value] : 4 <br>
     * [Key] : n [Value] : 11 <br>
     * [Key] : m [Value] : 2 <br>
     * [Key] : j [Value] : 10 <br>
     * [Key] : z [Value] : 9 <br>
     * [Key] : y [Value] : 5 <br>
     * 
     *  <br><b>NB</b>
     *  
	 * @param unsortedMap values are changed to ranks, so its best to pass a copy
	 */
	public void reverseRanking(Map<String, Double> unsortedMap)
	{
		Map<String, Double> sortedMap;
		//sorted in an ascending order
		sortedMap = MapUtil.sortByComparator(unsortedMap);
		Entry<String, Double> previsousEntry = null;
		
		int c = 1;
		for (Entry<String, Double> entry : sortedMap.entrySet()) 
		{ 
			if(c==1)
				unsortedMap.put(entry.getKey(), (double) c);
			else
			{   /* Assign the previous rank if current entry value matches previous entry value
			     * remember that comparison doesnt work without the cast */
				if((double)previsousEntry.getValue()==(double)entry.getValue())
					unsortedMap.put(entry.getKey(), (double) unsortedMap.get(previsousEntry.getKey()));
				else
					unsortedMap.put(entry.getKey(), (double) c);
			}
			/* 
			 * keep a copy of the previous entry
			 * this way, if the current entry value matches the previous entry
			 * they are assigned the sme rank
			 */
			previsousEntry = entry;
			
			c++;
		}
		
	}
	
	
	

}
