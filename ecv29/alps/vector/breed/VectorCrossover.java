
package ec.alps.vector.breed;

import ec.vector.*;
import ec.vector.breed.VectorCrossoverPipeline;

import java.util.ArrayList;
import java.util.HashMap;

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
			final ArrayList<Individual> inds,
			final EvolutionState state,
			final int thread,
			final HashMap<String,Object> misc) 

	{
		double selctionPressure = state.parameters.getDouble(new Parameter(ALPS_SELECTION_PRESSURE), null);

		// how many individuals should we make?
		int n = typicalIndsProduced();
		if (n < min) n = min;
		if (n > max) n = max;

		/** Added for ECJ 27 integration */
		IntBag[] parentparents = null;
		IntBag[] preserveParents = null;

		/** Added for ECJ 27 */
		if (misc!=null && misc.containsKey(KEY_PARENTS))
            {
            preserveParents = (IntBag[])misc.get(KEY_PARENTS);
            parentparents = new IntBag[2];
            misc.put(KEY_PARENTS, parentparents);
            }

		// // should we bother?
		// if (!state.random[thread].nextBoolean(likelihood))
		// 	return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

		/** ECJ 27 */
		// should we use them straight?
        if (!state.random[thread].nextBoolean(likelihood))
            {
            // just load from source 0 and clone 'em
            sources[0].produce(n,n,subpopulation,inds, state,thread,misc);
            return n;
            }

		for(int q=start;q<n+start; /* no increment */)  // keep on going until we're filled up
		{
			parents.clear(); // ECJ 27

			// grab two individuals from our sources
			if (sources[0]==sources[1])  // grab from the same source
			{ 
				if(Engine.alps.layers.get(Engine.alps.index).getIsBottomLayer())
				{
					sources[0].produce(2,2,subpopulation,parents,state,thread,misc);
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
						alpsParents = (VectorIndividual[]) parents.clone(); 

						//perform selection from current population if previous population is empty
						if((state.random[0].nextDouble()<=selctionPressure) || 
								Engine.alps.layers.get(Engine.alps.index-1).evolutionState.population.subpops.get(0).individuals.size()==0)
						{ 
							sources[0].produce(2,2,subpopulation,parents,state,thread,misc);
						}
						else
						{ 
							sources[0].produce(2,2,subpopulation,parents,
									Engine.alps.layers.get(Engine.alps.index-1).evolutionState,thread,misc);
						}
					}

					/*
					 * the second instance of the loop has new individuals
					 * maintain second individual and replace first with previously selected 
					 * first parent parents[0] = alpsParents[0];
					 * */
					parents.set(1, alpsParents[0]);

				}


				if (!(sources[0] instanceof BreedingPipeline))  // it's a selection method probably
				{ 
					parents.set(0, (VectorIndividual)(parents.get(0).clone()));
					parents.set(1, (VectorIndividual)(parents.get(1).clone()));
				}



			}
			else // grab from different sources
			{  

				for(int u=0;u<=1;u++)
				{
					alpsParents = (VectorIndividual[]) parents.clone(); //stores parents of first selection on second loop

					if(Engine.alps.layers.get(Engine.alps.index).getIsBottomLayer())
					{
						sources[0].produce(1,1,subpopulation,parents,state,thread,misc);
						sources[1].produce(1,1,subpopulation,parents,state,thread,misc);
					}
					else
					{
						if((state.random[0].nextDouble()<=selctionPressure) || 
								Engine.alps.layers.get(Engine.alps.index-1).evolutionState.population.subpops.get(0).individuals.size()<=0)
						{
							sources[0].produce(1,1,subpopulation,parents,state,thread,misc);
							sources[1].produce(1,1,subpopulation,parents,state,thread,misc);
						}
						else
						{
							sources[0].produce(1,1,subpopulation,parents,
									Engine.alps.layers.get(Engine.alps.index-1).evolutionState,thread,misc);
							sources[1].produce(1,1,subpopulation,parents,
									Engine.alps.layers.get(Engine.alps.index-1).evolutionState,thread,misc);
						}
					}
				}

				/** 
				 * the second instance of the loop has new individuals
				 * maintain second individual and replace first with previously selected 
				 * first parent parents.get(1) = alpsParents[0];
				 * */
				parents.set(1, alpsParents[0]);


				if (!(sources[0] instanceof BreedingPipeline))  // it's a selection method probably
					parents.set(0, (VectorIndividual)(parents.get(0).clone()));
				if (!(sources[1] instanceof BreedingPipeline)) // it's a selection method probably
					parents.set(1, (VectorIndividual)(parents.get(1).clone()));




			}

			for(int id=0;id<parents.size();id++)
			{
				if(state.generation != parents.get(id).generationCount/*!parents[k].parentFlag*/) 
				{
					parents.get(id).age++;
					parents.get(id).generationCount = state.generation;
				}
			}



			// at this point, parents[] contains our two selected individuals,
			// AND they're copied so we own them and can make whatever modifications
			// we like on them.

			// so we'll cross them over now.  Since this is the default pipeline,
			// we'll just do it by calling defaultCrossover on the first child

			//parents.get(0).defaultCrossover(state,thread,parents.get(1));
			// ECJ 27
			((VectorIndividual)(parents.get(0))).defaultCrossover(state,thread,((VectorIndividual)(parents.get(1))));
			parents.get(0).evaluated=false;
			parents.get(1).evaluated=false;

			// add 'em to the population
			//inds[q] = parents.get(0);
			//ECJ 27
			// by Ermo. this should use add instead of set, because the inds is empty, so will throw index out of bounds
            // okay -- Sean
			inds.add(parents.get(0));
			if (preserveParents != null)
                {
                parentparents[0].addAll(parentparents[1]);
                preserveParents[q] = parentparents[0];
                }

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
			inds.get(q).age        = Math.max(parents.get(0).age, parents.get(1).age);
			/*Get minimum evaluation for parent. the lowest evaluation count is the oldest parent */
			inds.get(q).evaluation = Math.min(parents.get(0).evaluation, parents.get(1).evaluation);
			q++;
			if (q<n+start && !tossSecondParent)
			{
				/** ECJ 27 */
				inds.add(parents.get(1));
				if (preserveParents != null)
                    {
                    preserveParents[q] = new IntBag(parentparents[0]);
                    }
				
				/**offspring gets age of oldest parent */
				inds.get(q).age        = Math.max(parents.get(0).age, parents.get(1).age);
				/**Get minimum evaluation for parent. the lowest evaluation count is the oldest parent */
				inds.get(q).evaluation = Math.min(parents.get(0).evaluation, parents.get(1).evaluation);
				q++;
			}
		}
		return n;
	}
}







