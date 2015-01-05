package ec.alps.util;

import java.util.Map;
import java.util.Map.Entry;

import ec.EvolutionState;

public class TreeAnalyzer {

	/**
	 * Count all terminals and non-terminals in the GP-Tree
	 * terminals are stored in state.nodeCountTerminalSet
	 * non-terminals are stored in state.nodeCountFunctionSet
	 * @param state
	 */
	public static void countNodes(EvolutionState state)
	{
		for(int x=0;x<state.population.subpops.length;x++)
		{
			//best to start from y=1 to test for best individual. started from 0 because of statistics
			for(int y=0;y<state.population.subpops[x].individuals.length;y++) 
			{
				/* gather statistics of node usage in entire population 
				 * state.nodeCountTerminalSet is updated
				 * */
				state.population.subpops[x].individuals[y].
				gatherIndividualNodeStats(state,state.nodeCountTerminalSet);
				
				/** count function sets */
				state.population.subpops[x].individuals[y].
				gatherIndividualNodeStats(state,state.nodeCountFunctionSet);
			}
		}
	}
	
	/**
	 * Unset node count to zero
	 * @param state Evolution state object
	 * @param map specifies a node type state.nodeCountTerminalSet or state.nodeCountFunctionSet
	 */
	public static Map<String, Double> unsetNodeCount(EvolutionState state,Map<String, Double> map)
	{   //System.out.println("unsetNodeCount"); System.exit(0);
		for (Entry<String, Double> entry : map.entrySet()) 
			map.put(entry.getKey(), 0.0);
		
		return map;
	}
	

}
