package ec.alps.util;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.Individual;

public class Operations {

	public Operations() {
		// TODO Auto-generated constructor stub
	}


    /**
     * Empties the individuals specifield individuals in a subpopulation
     * 
     * @param individuals population from a subpopulation
     * @return
     */
	public static Individual[] emptyPop(Individual[] individuals)
	{
		ArrayList<Individual> dummy = listArrayAdaptor(individuals);
		@SuppressWarnings("unchecked")
		ArrayList<Individual> deleteList = (ArrayList<Individual>) dummy.clone();

		for (int id = 0; id < deleteList.size(); id++) 
			dummy.remove(deleteList.get(id));

		return arrayListAdaptor(dummy);
	}

    /**
     * this deletes deletesList from subpopulation individuals 
     * 
     * @param individuals individuals in a subpopulation
     * @param deleteList arrayList to delete
     * @return
     */
	public static Individual[] emptyPop(Individual[] individuals, ArrayList<Individual> deleteList )
	{
		ArrayList<Individual> dummy = listArrayAdaptor(individuals);
		for (int id = 0; id < deleteList.size(); id++) 
			dummy.remove(deleteList.get(id));

		return arrayListAdaptor(dummy);
	}


	/**
	 * @param Individuals
	 * @return
	 */
	private static ArrayList<Individual> listArrayAdaptor(Individual [] individuals)
	{
		ArrayList<Individual> list = new ArrayList<>();
		for(Individual i: individuals)
			list.add(i);
		return list;
	}


	/**
	 * 
	 * @param individuals
	 * @return
	 */
	private static Individual [] arrayListAdaptor(ArrayList<Individual> individuals)
	{
		Individual [] list = new Individual [individuals.size()];
		for(int i=0;i<individuals.size();i++)
			list[i] = individuals.get(i);
		
		return list;
	}
	
	
	/**
	 * Calculate population size of all sub-populations in a layer
	 * @param state
	 * @return
	 */
	public static int popSize(EvolutionState state)
	{
		int total = 0;
		for(int x=0;x<state.population.subpops.length;x++)
			total +=state.population.subpops[x].individuals.length;
		
		return total;
	}
	
	
	

}
