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
 * In ReverseTournamentWorst replacement, when an old  individual from a lower layer is moving to a higher layer
 * with a larger age limit, the individual from the higher layer's population with the worst fitness
 * is picked for replacement.
 * 
 * @author Anthony Awuley
 *
 */
public class Worst  extends Replacement{


	/** */
	private static final long serialVersionUID = 1;


	public Worst() 
	{ }


	public String toString()
	{
		return "Worst Individual Replacement";
	}

    @Override
	public void layerMigrations(ALPSLayers alps,Population current)
	{
		Population higherPop = null;
		ArrayList<Individual> deleteList = new ArrayList<>();

		// Only migrate from layers that exist
		if (alps.index < (alps.layers.size() - 1)) 
		{
			// Subpopulation support (typically only one subpop)
			for(int subpopulation=0;subpopulation<alps.layers.get(alps.index).evolutionState.population.subpops.size();subpopulation++)
			{
				/* total number of populations expected */
				int size = alps.layers.get(alps.index).evolutionState.
						parameters.getInt(new Parameter(Initializer.P_POP).push(Population.P_SUBPOP).push(subpopulation+"").push(POP_SIZE),null);

				//get population of next higher layer
				higherPop = (Population) alps.layers.get(alps.index + 1).evolutionState.population;


				// Loop over individuals of population
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
					{
						//fill higher layer with individuals that fall within its age limit
						//parameters.getIntWithDefault(new Parameter("jobs"), null, 1);
						if (higherPop.subpops.get(subpopulation).individuals.size() < size) 
						{
							/* activate layer if its open to accept individuals */
							alps.layers.get(alps.index + 1).setIsActive(true);
							
							alps.layers.get(alps.index + 1).evolutionState.population.subpops.get(subpopulation).individuals.
							add((Individual) current.subpops.get(subpopulation).individuals.get(i).clone());


							deleteList.add(current.subpops.get(subpopulation).individuals.get(i));
						} 
						else if (higherPop.subpops.get(subpopulation).individuals.size() > 0 ) //once higher layer is filled, do selective replacement based on new individuals that have higher age than in the individual in the  higher layer
						{
							/* setup tournament selection
							 * modify to dynamically include  thread
							 */
							worseIndividual = worst(subpopulation,
									alps.layers.get(alps.index + 1).evolutionState, 0);

							if(replaceWeakest)  /* always replace weakest tournament individual with new individual */
								alps.layers.get(alps.index + 1).evolutionState.population.subpops.get(subpopulation).individuals.set(worseIndividual, 
								(Individual) current.subpops.get(subpopulation).individuals.get(i).clone());
							else /* only replace weakest tournament individual if its fitness is lower than new individual from lower layer*/
								if(current.subpops.get(subpopulation).individuals.get(i).fitness.betterThan(
										alps.layers.get(alps.index + 1).evolutionState.population.subpops.get(subpopulation).individuals.get(worseIndividual).fitness))
									alps.layers.get(alps.index + 1).evolutionState.population.subpops.get(subpopulation).individuals.set(worseIndividual, 
									(Individual) current.subpops.get(subpopulation).individuals.get(i).clone());

							deleteList.add(current.subpops.get(subpopulation).individuals.get(i));
						}
					}
				}
				//remove all individuals older than current layer
				current.subpops.get(subpopulation).individuals = Operations.emptyPop(current.subpops.get(subpopulation).individuals,deleteList);

				deleteList.clear();


				/* fill empty slots for maximum breeding 
				if(Engine.always_breed_maximum_pop)
					current.subpops.get(subpopulation).individuals =
					fillPopTournament(current.subpops.get(subpopulation).individuals.size(),
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
