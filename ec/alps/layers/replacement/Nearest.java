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
 * In Nearest replacement, when an old  individual from a lower layer is moving to a higher layer
 * with a larger age limit, the  individual from the higher layer's population with the nearest fitness
 * to the new individual is picked for replacement.
 * 
 * @author Anthony Awuley
 *
 */
public class Nearest extends Replacement{


	/**  */
	private static final long serialVersionUID = 1;

	public Nearest() 
	{}

	public String toString()
	{
		return "Nearest Neighbour Replacment";
	}

	@Override
	public void layerMigrations(ALPSLayers alps, Population current) {
		Population higherPop = null;
		ArrayList<Individual> deleteList = new ArrayList<>();

		if (alps.index < (alps.layers.size() - 1)) 
		{
			for(int subpopulation=0;subpopulation<alps.layers.get(alps.index).evolutionState.population.subpops.length;subpopulation++)
			{
				
				/** total number of populations expected */
				int size = alps.layers.get(alps.index).evolutionState.
						parameters.getInt(new Parameter(Initializer.P_POP).push(Population.P_SUBPOP).push(subpopulation+"").push(POP_SIZE),null);


				/* initialize number of individuals added  */
				alps.layers.get(alps.index+1).individualCount=0;

				//get population of next higher layer
				higherPop = (Population) alps.layers.get(alps.index + 1).evolutionState.population;

				
				for (int i = 0; i < current.subpops[subpopulation].individuals.length; i++) 
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
					if (current.subpops[subpopulation].individuals[i].age >= (alps.layers.get(alps.index).getMaxAge())) 
					{   
						/*fill higher layer with individuals that fall within its age limit
						parameters.getIntWithDefault(new Parameter("jobs"), null, 1); */
						
						if (higherPop.subpops[subpopulation].individuals.length < size) 
						{
							/* activate layer if its open to accept individuals */
							alps.layers.get(alps.index + 1).setIsActive(true);
							
							alps.layers.get(alps.index + 1).evolutionState.population.subpops[subpopulation].
							add((Individual) current.subpops[subpopulation].individuals[i].clone());
							deleteList.add(current.subpops[subpopulation].individuals[i]); // now added--remove if problematic

							/* count individuals added */
							alps.layers.get(alps.index+1).individualCount++;
						} 
						else if (higherPop.subpops[subpopulation].individuals.length > 0 ) //once higher layer is filled, do selective replacement based on new individuals that have higher age than in the individual in the  higher layer
						{
							/**
							 * setup tournament selection
							 * modify to dynamically include  thread
							 */
							nearestIndividual = nearestPopulationIndividualFitness(subpopulation,
									alps.layers.get(alps.index + 1).evolutionState, 
									0,(Individual) current.subpops[subpopulation].individuals[i]);

							if(replaceWeakest)  /* always replace weakest tournament individual with new individual */
								alps.layers.get(alps.index + 1).evolutionState.population.subpops[subpopulation].individuals[nearestIndividual] = 
								(Individual) current.subpops[subpopulation].individuals[i].clone();
							else /* only replace weakest tournament individual if its fitness is lower than new individual from lower layer*/
								if(current.subpops[subpopulation].individuals[i].fitness.betterThan(
										alps.layers.get(alps.index + 1).evolutionState.population.subpops[subpopulation].individuals[nearestIndividual].fitness))
									alps.layers.get(alps.index + 1).evolutionState.population.subpops[subpopulation].individuals[nearestIndividual] = 
									(Individual) current.subpops[subpopulation].individuals[i].clone();

							//alps.layers.get(alps.index + 1).getEvolution().getCurrentPopulation().
							//        set(this.worseIndividual, current.get(i));
							deleteList.add(current.subpops[subpopulation].individuals[i]);

							/* count individuals added */
							alps.layers.get(alps.index+1).individualCount++;
						}
					}
				}

				current.subpops[subpopulation].individuals = 
						Operations.emptyPop(current.subpops[subpopulation].individuals,deleteList);
				deleteList.clear();


				/* fill empty slots for maximum breeding 
				if(Engine.always_breed_maximum_pop)
					current.subpops[subpopulation].individuals =
					fillPopTournament(current.subpops[subpopulation].individuals.length,
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
