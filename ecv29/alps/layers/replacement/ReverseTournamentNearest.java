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

/**
 * In ReverseTournamentNearest replacement, when an old  individual from a lower layer is moving to a higher layer
 * with a larger age limit, the tournament individual from the higher layer with the nearest fitness
 * to the new individual is picked for replacement.
 * 
 * @author Anthony Awuley
 *
 */
public class ReverseTournamentNearest extends Replacement{

	/** */
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

	public void layerMigrations(ALPSLayers alps,Population current)
	{
		Population higherPop = null;
		ArrayList<Individual> deleteList = new ArrayList<>();


		if (alps.index < (alps.layers.size() - 1)) 
		{
			for(int subpopulation=0;subpopulation<alps.layers.get(alps.index).evolutionState.population.subpops.size();subpopulation++)
			{
				
				/* total number of populations expected */
				int size = alps.layers.get(alps.index).evolutionState.
						parameters.getInt(new Parameter(Initializer.P_POP).push(Population.P_SUBPOP).push(subpopulation+"").push(POP_SIZE),null);

				/* initialize number of individuals added  */
				alps.layers.get(alps.index+1).individualCount=0;
				//get population of next higher layer
				higherPop = (Population) alps.layers.get(alps.index + 1).evolutionState.population;

				
				for (int i = 0; i < current.subpops.get(subpopulation).individuals.size(); i++) 
				{ 

					/* for an age-gap of 5 and polynomial aging scheme: the age layers are
					 * 5 10 20 45 etc. the age rage for the layers are:
					 * 
					 * Layer 0 : 0-4
					 * Layer 1 : 5-9
					 * Layer 2 : 10-19
					 * etc.. 
					 * Max for a layer = (alps.layers.get(alps.index).getMaxAge()-1)
					 */
					if (current.subpops.get(subpopulation).individuals.get(i).age >= (alps.layers.get(alps.index).getMaxAge())) 
					{   //fill higher layer with individuals that fall within its age limit
						//parameters.getIntWithDefault(new Parameter("jobs"), null, 1);
						if (higherPop.subpops.get(subpopulation).individuals.size() < size) 
						{
							/* activate layer if its open to accept individuals */
							alps.layers.get(alps.index + 1).setIsActive(true);
							
							alps.layers.get(alps.index + 1).evolutionState.population.subpops.get(subpopulation).individuals.
							add((Individual) current.subpops.get(subpopulation).individuals.get(i).clone());
							deleteList.add(current.subpops.get(subpopulation).individuals.get(i)); // now added--remove if problematic

							/* count individuals added */
							alps.layers.get(alps.index+1).individualCount++;
						} 
						else if (higherPop.subpops.get(subpopulation).individuals.size() > 0 ) //once higher layer is filled, do selective replacement based on new individuals that have higher age than in the individual in the  higher layer
						{
							/**
							 * setup tournament selection
							 * modify to dynamically include  thread
							 */
							nearestIndividual = nearestTournamentIndividualFitness(
									subpopulation,alps.layers.get(alps.index + 1).evolutionState, 
									0,(Individual) current.subpops.get(subpopulation).individuals.get(i));

							if(replaceWeakest)  /* always replace weakest tournament individual with new individual */
								alps.layers.get(alps.index + 1).evolutionState.population.subpops.get(subpopulation).individuals.set(nearestIndividual,
								(Individual) current.subpops.get(subpopulation).individuals.get(i).clone());
							else /* only replace weakest tournament individual if its fitness is lower than new individual from lower layer*/
								if(current.subpops.get(subpopulation).individuals.get(i).fitness.betterThan(
										alps.layers.get(alps.index + 1).evolutionState.population.subpops.get(subpopulation).individuals.get(nearestIndividual).fitness))
									alps.layers.get(alps.index + 1).evolutionState.population.subpops.get(subpopulation).individuals.set(nearestIndividual,
									(Individual) current.subpops.get(subpopulation).individuals.get(i).clone());

							//alps.layers.get(alps.index + 1).getEvolution().getCurrentPopulation().
							//        set(this.worseIndividual, current.get(i));
							deleteList.add(current.subpops.get(subpopulation).individuals.get(i));

							/* count individuals added */
							alps.layers.get(alps.index+1).individualCount++;
						}
					}
				}
				//remove all individuals older than current layer
				current.subpops.get(subpopulation).individuals = Operations.emptyPop(current.subpops.get(subpopulation).individuals,deleteList);

				deleteList.clear();

				/* fill empty slots for maximum breeding 
				if(Engine.always_breed_maximum_pop)
					current.subpops.get(subpopulation).individuals =
					fillPopTournament(current.subpops.get(subpopulation).individuals.length,
							size,
							subpopulation,
							alps.layers.get(alps.index).evolutionState,
							0);
                 */
			}//subpops
		}

		/* fill empty slots for maximum breeding */
		if(Engine.always_breed_maximum_pop)
			consolidatePopulation(alps,0);

	}





}
