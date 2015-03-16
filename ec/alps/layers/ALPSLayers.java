package ec.alps.layers;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.util.ParameterDatabase;



public class ALPSLayers {
	/**ArrayList of all running ALPS layers **/
	public ArrayList<Layer> layers;
	/**Points to the current active layer in ALPS */
	public int index;
	//public int evalCounter = 0; //keeps count of number of evaluations completed in a layer
	//public int evaluations = 0; //keep count of number of evaluations in steady state

	public ALPSLayers(ArrayList<Layer> l,int id) 
	{
		this.layers = l;
		this.index  = id;
	}

	/** test method to dump individual ages to standard output*/
	public  void printAge()
	{
		System.out.println("\n Layer :" + this.index + 
				" Generation :"+this.layers.get(this.index).evolutionState.generation);
		
		for(int i=0;i<this.layers.get(this.index).evolutionState.population.subpops[0].individuals.length;i++)
			System.out.print(this.layers.get(this.index).evolutionState.population.subpops[0].individuals[i].age +" ");

		System.out.println("\n");
	}
	
	/** test method to dump individual population size to standard output*/
	public  void printPopSize()
	{
		System.out.println("\nLayer :" + this.index + 
				" Generation :"+this.layers.get(this.index).evolutionState.generation);
		for(int i=0;i<this.layers.get(this.index).evolutionState.population.subpops.length;i++)
			System.out.print("subpop:"+i +" size:"+this.layers.get(this.index).evolutionState.population.subpops[0].individuals.length +" ");

		System.out.println("\n");
	}


}
