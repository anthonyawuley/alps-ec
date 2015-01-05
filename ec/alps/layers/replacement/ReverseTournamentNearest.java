package ec.alps.layers.replacement;

import java.util.ArrayList;

import ec.Individual;
import ec.Initializer;
import ec.Population;
import ec.alps.Engine;
import ec.alps.layers.ALPSLayers;
import ec.alps.layers.Replacement;
import ec.alps.util.Operations;
import ec.util.Parameter;


public class ReverseTournamentNearest extends Replacement{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1;

	public ReverseTournamentNearest() 
	{
	}

	public String toString()
	{
		return "Nearest Neighbour Replacment";
	}

	/**
	 * loop through highest layer to current layer
	 * attempt to move individuals from current layer that have age values within 
	 * higher layer.
	 * 
	 * @param layers
	 * @param to
	 * @return
	 */

	public void layerMigrations(ALPSLayers alpsLayers,Population current)
	{
		Population higherPop = null;
		ArrayList<Individual> deleteList = new ArrayList<>();


		if (alpsLayers.index < (alpsLayers.layers.size() - 1)) 
		{
			for(int subpopulation=0;subpopulation<alpsLayers.layers.get(alpsLayers.index).evolutionState.population.subpops.length;subpopulation++)
			{
				/* try fetching pop size of subpopulation from parameter file
				 * if this fails use population size of subpopulation 0

				int size =  alpsLayers.layers.get(alpsLayers.index).parameterDatabase.
						getIntWithDefault(new Parameter("pop.subpop."+subpopulation+".size"), null, 
								alpsLayers.layers.get(alpsLayers.index).parameterDatabase.
								getInt(new Parameter("pop.subpop.0.size"), null));
				 */
				/** total number of populations expected */
				int size = alpsLayers.layers.get(alpsLayers.index).evolutionState.
						parameters.getInt(new Parameter(Initializer.P_POP).push(Population.P_SUBPOP).push(subpopulation+"").push(POP_SIZE),null);

				/* initialize number of individuals added  */
				alpsLayers.layers.get(alpsLayers.index+1).individualCount=0;
				//get population of next higher layer
				higherPop = (Population) alpsLayers.layers.get(alpsLayers.index + 1).evolutionState.population;

				for (int i = 0; i < current.subpops[subpopulation].individuals.length; i++) 
				{ 

					/* for an age-gap of 5 and polynomial aging scheme: the age layers are
					 * 5 10 20 45 etc. the age rage for the layers are:
					 * 
					 * Layer 0 : 0-4
					 * Layer 1 : 5-9
					 * Layer 2 : 10-19
					 * etc.. 
					 * Max for a layer = (alpsLayers.layers.get(alpsLayers.index).getMaxAge()-1)
					 */
					if (current.subpops[subpopulation].individuals[i].age >= (alpsLayers.layers.get(alpsLayers.index).getMaxAge())) 
					{   //fill higher layer with individuals that fall within its age limit
						//parameters.getIntWithDefault(new Parameter("jobs"), null, 1);
						if (higherPop.subpops[subpopulation].individuals.length < size) 
						{
							alpsLayers.layers.get(alpsLayers.index + 1).evolutionState.population.subpops[subpopulation].
							add((Individual) current.subpops[subpopulation].individuals[i].clone());
							deleteList.add(current.subpops[subpopulation].individuals[i]); // now added--remove if problematic

							/* count individuals added */
							alpsLayers.layers.get(alpsLayers.index+1).individualCount++;
						} 
						else if (higherPop.subpops[subpopulation].individuals.length > 0 ) //once higher layer is filled, do selective replacement based on new individuals that have higher age than in the individual in the  higher layer
						{
							/**
							 * setup tournament selection
							 * modify to dynamically include  thread
							 */
							nearestIndividual = nearestTournamentIndividualFitness(
									subpopulation,alpsLayers.layers.get(alpsLayers.index + 1).evolutionState, 
									0,(Individual) current.subpops[subpopulation].individuals[i]);

							if(replaceWeakest)  /* always replace weakest tournament individual with new individual */
								alpsLayers.layers.get(alpsLayers.index + 1).evolutionState.population.subpops[subpopulation].individuals[nearestIndividual] = 
								(Individual) current.subpops[subpopulation].individuals[i].clone();
							else /* only replace weakest tournament individual if its fitness is lower than new individual from lower layer*/
								if(current.subpops[subpopulation].individuals[i].fitness.betterThan(
										alpsLayers.layers.get(alpsLayers.index + 1).evolutionState.population.subpops[subpopulation].individuals[nearestIndividual].fitness))
									alpsLayers.layers.get(alpsLayers.index + 1).evolutionState.population.subpops[subpopulation].individuals[nearestIndividual] = 
									(Individual) current.subpops[subpopulation].individuals[i].clone();

							//alpsLayers.layers.get(alpsLayers.index + 1).getEvolution().getCurrentPopulation().
							//        set(this.worseIndividual, current.get(i));
							deleteList.add(current.subpops[subpopulation].individuals[i]);

							/* count individuals added */
							alpsLayers.layers.get(alpsLayers.index+1).individualCount++;
						}
					}
				}
				//remove all individuals older than current layer
				current.subpops[subpopulation].individuals = Operations.emptyPop(current.subpops[subpopulation].individuals,deleteList);

				/*
	        System.out.println("DeleteList "+ deleteList.size()+ "  Current "+alpsLayers.index +": "+current.subpops[subpopulation].individuals.length+
	             " NextLayer "+(alpsLayers.index+1)+" :"+alpsLayers.layers.get(alpsLayers.index+1).evolutionState.population.subpops[subpopulation].individuals.length+
	             " Generation: "+ alpsLayers.layers.get(alpsLayers.index).evolutionState.generation+
	             " Max age layer: "+alpsLayers.layers.get(alpsLayers.index).getMaxAge()+""); //System.exit(0);
				 */
				deleteList.clear();

				/* fill empty slots for maximum breeding */
				if(Engine.always_breed_maximum_pop)
					current.subpops[subpopulation].individuals =
					fillPopTournament(current.subpops[subpopulation].individuals.length,
							size,
							subpopulation,
							alpsLayers.layers.get(alpsLayers.index).evolutionState,
							0);
			}//subpops

		}

	}





}
