
package ec.alps.vector.breed;

import ec.vector.*;
import ec.vector.breed.VectorCrossoverPipeline;
import ec.*;
import ec.alps.Engine;
import ec.EvolutionState;
import ec.util.*;



/**
 * Extends VectorCrossoverPipeline from ECJ to include age increment of parent and offspring individuals
 * 
 * ALPS: AGE INCREMENT
 * 
 * Each generation in which an individual is used as a parent to create an offspring, its
 * age is increased by 1 since its genetic material has been used in evolution in another generation
 * ---Greg Hornby
 * 
 * Increase age of individuals used as parents :::
 * 
 * For example, the GP CrossoverPipeline asks for one Individual of each of its two children, 
 * which must be genetic programming Individuals, performs subtree crossover on those Individuals, 
 * then hands them to its parent. --  Sean Luke - ECJ Manual
 * 
 * @author Anthony Awuley
 */
public class VectorCrossover extends VectorCrossoverPipeline
{

	/** Temporary holding place for alps parent selection */
	private VectorIndividual alpsParents[];

	private final String ALPS_SELECTION_PRESSURE = "alps.selection-pressure";


	public int produce(final int min, 
			final int max, 
			final int start,
			final int subpopulation,
			final Individual[] inds,
			final EvolutionState state,
			final int thread) 

	{
		double selctionPressure = state.parameters.getDouble(new Parameter(ALPS_SELECTION_PRESSURE), null);

		// how many individuals should we make?
		int n = typicalIndsProduced();
		if (n < min) n = min;
		if (n > max) n = max;

		// should we bother?
		if (!state.random[thread].nextBoolean(likelihood))
			return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

		for(int q=start;q<n+start; /* no increment */)  // keep on going until we're filled up
		{
			// grab two individuals from our sources
			if (sources[0]==sources[1])  // grab from the same source
			{ 
				if(Engine.alps.layers.get(Engine.alps.index).getIsBottomLayer())
				{
					sources[0].produce(2,2,0,subpopulation,parents,state,thread);
				}
				else
				{ 
					for(int u=0;u<=1;u++)
					{
						/**
						 * stores parents of first selection on second loop. 
						 * IMPortant to clone else, will end up with reference of second parents selection in alpsParents
						 * which results in selection from the same source
						 */
						alpsParents = parents.clone(); 

						//perform selection from current population if previous population is empty
						if((state.random[0].nextDouble()<=selctionPressure) || 
								Engine.alps.layers.get(Engine.alps.index-1).evolutionState.population.subpops[0].individuals.length==0)
						{ 
							sources[0].produce(2,2,0,subpopulation,parents,state,thread);
						}
						else
						{ 
							sources[0].produce(2,2,0,subpopulation,parents,
									Engine.alps.layers.get(Engine.alps.index-1).evolutionState,thread);
						}
					}

					/*
					 * the second instance of the loop has new individuals
					 * maintain second individual and replace first with previously selected 
					 * first parent parents[0] = alpsParents[0];
					 * */
					parents[1] = alpsParents[0];

				}


				if (!(sources[0] instanceof BreedingPipeline))  // it's a selection method probably
				{ 
					parents[0] = (VectorIndividual)(parents[0].clone());
					parents[1] = (VectorIndividual)(parents[1].clone());
				}



			}
			else // grab from different sources
			{  

				for(int u=0;u<=1;u++)
				{
					alpsParents = parents.clone(); //stores parents of first selection on second loop

					if(Engine.alps.layers.get(Engine.alps.index).getIsBottomLayer())
					{
						sources[0].produce(1,1,0,subpopulation,parents,state,thread);
						sources[1].produce(1,1,1,subpopulation,parents,state,thread);
					}
					else
					{
						if((state.random[0].nextDouble()<=selctionPressure) || 
								Engine.alps.layers.get(Engine.alps.index-1).evolutionState.population.subpops[0].individuals.length<=0)
						{
							sources[0].produce(1,1,0,subpopulation,parents,state,thread);
							sources[1].produce(1,1,1,subpopulation,parents,state,thread);
						}
						else
						{
							sources[0].produce(1,1,0,subpopulation,parents,
									Engine.alps.layers.get(Engine.alps.index-1).evolutionState,thread);
							sources[1].produce(1,1,1,subpopulation,parents,
									Engine.alps.layers.get(Engine.alps.index-1).evolutionState,thread);
						}
					}
				}

				/** 
				 * the second instance of the loop has new individuals
				 * maintain second individual and replace first with previously selected 
				 * first parent parents[1] = alpsParents[0];
				 * */
				parents[1] = alpsParents[0];


				if (!(sources[0] instanceof BreedingPipeline))  // it's a selection method probably
					parents[0] = (VectorIndividual)(parents[0].clone());
				if (!(sources[1] instanceof BreedingPipeline)) // it's a selection method probably
					parents[1] = (VectorIndividual)(parents[1].clone());




			}

			for(int id=0;id<parents.length;id++)
			{
				if(state.generation != parents[id].generationCount/*!parents[k].parentFlag*/) 
				{
					parents[id].age++;
					parents[id].generationCount = state.generation;
				}
			}



			// at this point, parents[] contains our two selected individuals,
			// AND they're copied so we own them and can make whatever modifications
			// we like on them.

			// so we'll cross them over now.  Since this is the default pipeline,
			// we'll just do it by calling defaultCrossover on the first child

			parents[0].defaultCrossover(state,thread,parents[1]);
			parents[0].evaluated=false;
			parents[1].evaluated=false;

			// add 'em to the population
			inds[q] = parents[0];
			/*
			 * ALPS: AGE INCREMENT
			 * increase age of offsping age by oldest parent
			 * 
			 * Individuals that are created through variation, such as by mutation or recombination, 
			 * take the age value of their oldest parent plus 1 since their genetic material comes from 
			 * their parents and has now been in the population for one more generation than their parents.
			 * --Greg Hornby
			 * 
			 * Since parents age has already been incremented, set offspring age to oldest parent
			 * 
			 * @author anthony
			 */
			/*offspring gets age of oldest parent */
			inds[q].age        = Math.max(parents[0].age, parents[1].age);
			/*Get minimum evaluation for parent. the lowest evaluation count is the oldest parent */
			inds[q].evaluation = Math.min(parents[0].evaluation, parents[1].evaluation);
			q++;
			if (q<n+start && !tossSecondParent)
			{
				inds[q] = parents[1];
				
				/**offspring gets age of oldest parent */
				inds[q].age        = Math.max(parents[0].age, parents[1].age);
				/**Get minimum evaluation for parent. the lowest evaluation count is the oldest parent */
				inds[q].evaluation = Math.min(parents[0].evaluation, parents[1].evaluation);
				q++;
			}
		}
		return n;
	}
}







