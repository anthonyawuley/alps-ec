package ec.alps.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import ec.EvolutionState;
import ec.alps.Engine;
import ec.alps.layers.ALPSLayers;


public class Roulette {

	//private int node  =  -1; //set value to index selected in roulette
	private  Map<String, Double> map;      //loads default node probability settings

	private ArrayList<ArrayList<Double>> roulette;

	/**
	 * state 
	 *      EvolutionState
	 * @param state
	 */
	public Roulette(ALPSLayers alps,EvolutionState state) 
	{

		try
		{   //Avoid Engine.alps.layers exception when alps.layers have not been set up
			if (Engine.fsalps_use_all_layers)
			{   /* Obtain initial node, value pairs from last layer */
				map = alps.layers.get(0).evolutionState.nodeCountTerminalSet;
				/* loop through remaining layers and add respective node frequency counts */
				for(int i=1;i< Engine.alps.layers.size();i++)
					for (Entry<String, Double> node : Engine.alps.layers.get(i).evolutionState.nodeCountTerminalSet.entrySet())
						map.put(node.getKey(), node.getValue() + map.get(node.getKey()));
						//System.out.println("ENGINE "+i + " "+ node.getKey() + ": "+ node.getValue()+" TOTAL:"+ map.get(node.getKey()));
			}
			else /*use last layer */
				map = alps.layers.get(alps.layers.size()-1).evolutionState.nodeCountTerminalSet; 
				//map = state.nodeCountTerminalSet; will also work if last layer evolutionState is parsed
		}
		catch(NullPointerException e) 
		{   //this occurs during initialization, when ALPS is setting up
			map = state.nodeCountTerminalSet;
		}
		
		/* When Engine.fsalps_use_only_default_node_pr is "true", only default node probabilities are used
		 * note that values set for each node are converted to probabilities
		 * this assignment overrites all operations performed in the try{..} catch(...){...} above  */
		if(Engine.fsalps_use_only_default_node_pr)
			map = Engine.nodeCountTerminalSet;
		
        
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
	public static int totalFrequency(Map<String, Double> map)
	{
		int total = 0;
		for (Entry<String, Double> entry : map.entrySet()) 
			total += entry.getValue(); //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue()); 

		return total;
	}


	/**
	 * Uses random probability (prob) to select from the roulette frequency mappings
	 * generated in convertFreqToProb()
	 * 
	 * @param prob generated probability used in selection
	 * @return int that represents a selected index for a node
	 * @author anthony
	 */
	public int spin(double prob) 
	{ //System.out.println("PRINTING PROBABILITY::::"+ prob);
		
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
