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
 * 
 * @author anthony
 *
 * scans through higher layer and replaces any first encountered worse individual
 */
public class Worst  extends Replacement{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1;



	public Worst() 
	{

	}


	public String toString()
	{
		return "Worst Individual Replacement";
	}



	public void layerMigrations(ALPSLayers alps,Population current)
	{
		Population higherPop = null;
		ArrayList<Individual> deleteList = new ArrayList<>();

		//TournamentSelection t = new TournamentSelection();

		//SelectionOperation selectionOperation = new TournamentSelection();

		if (alps.index < (alps.layers.size() - 1)) 
		{
			for(int subpopulation=0;subpopulation<alps.layers.get(alps.index).evolutionState.population.subpops.length;subpopulation++)
			{
				/* try fetching pop size of subpopulation from parameter file
				 * if this fails use population size of subpopulation 0

				int size =  alps.layers.get(alps.index).parameterDatabase.
						getIntWithDefault(new Parameter("pop.subpop."+subpopulation+".size"), null, 
								alps.layers.get(alps.index).parameterDatabase.
								getInt(new Parameter("pop.subpop.0.size"), null));
				 */
				/** total number of populations expected */
				int size = alps.layers.get(alps.index).evolutionState.
						parameters.getInt(new Parameter(Initializer.P_POP).push(Population.P_SUBPOP).push(subpopulation+"").push(POP_SIZE),null);

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
						//fill higher layer with individuals that fall withing its age limit
						//parameters.getIntWithDefault(new Parameter("jobs"), null, 1);
						if (higherPop.subpops[subpopulation].individuals.length < size) 
						{
							/* activate layer if its open to accept individuals */
							alps.layers.get(alps.index + 1).setIsActive(true);
							
							alps.layers.get(alps.index + 1).evolutionState.population.subpops[subpopulation].
							add((Individual) current.subpops[subpopulation].individuals[i].clone());

							deleteList.add(current.subpops[subpopulation].individuals[i]);
						} 
						else if (higherPop.subpops[subpopulation].individuals.length > 0 ) //once higher layer is filled, do selective replacement based on new individuals that have higher age than in the individual in the  higher layer
						{
							/* setup tournament selection
							 * modify to dynamically include  thread
							 */
							worseIndividual = worst(subpopulation,
									alps.layers.get(alps.index + 1).evolutionState, 0);

							if(replaceWeakest)  /* always replace weakest tournament individual with new individual */
								alps.layers.get(alps.index + 1).evolutionState.population.subpops[subpopulation].individuals[worseIndividual] = 
								(Individual) current.subpops[subpopulation].individuals[i].clone();
							else /* only replace weakest tournament individual if its fitness is lower than new individual from lower layer*/
								if(current.subpops[subpopulation].individuals[i].fitness.betterThan(
										alps.layers.get(alps.index + 1).evolutionState.population.subpops[subpopulation].individuals[worseIndividual].fitness))
									alps.layers.get(alps.index + 1).evolutionState.population.subpops[subpopulation].individuals[worseIndividual] = 
									(Individual) current.subpops[subpopulation].individuals[i].clone();

							//alps.layers.get(alps.index + 1).getEvolution().getCurrentPopulation().
							//        set(this.worseIndividual, current.get(i));
							deleteList.add(current.subpops[subpopulation].individuals[i]);
						}
					}
				}
				//remove all individuals older than current layer
				current.subpops[subpopulation].individuals = Operations.emptyPop(current.subpops[subpopulation].individuals,deleteList);

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
