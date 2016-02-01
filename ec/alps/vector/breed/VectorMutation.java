/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */


package ec.alps.vector.breed;

import ec.vector.*;
import ec.vector.breed.VectorMutationPipeline;
import ec.*;
import ec.alps.Engine;
import ec.EvolutionState;
import ec.util.*;

/**
 * Extends VectorMutationPipeline from ECJ to include age increment of parent and offspring individuals
 * 
 * ALPS: AGE INCREMENT
 * increase age of parent 
 * 
 * Each generation in which an individual is used as a parent to create an offspring its age is 
 * increases by 1 since its genetic material has been used in evolution in another generation. 
 * Even if an individual is selected to reproduce multiple times in one generation its age is 
 * still only increased by 1 so that good individuals that reproduce a lot are not penalized for 
 * being more fit than similarly aged individuals.  --- GREG Hornby
 * 
 * There's no need modifying evaluation count for individuals because its still the same
 * 
 * @author Anthony Awuley
 */
public class VectorMutation extends VectorMutationPipeline
{

	private final String ALPS_SELECTION_PRESSURE = "alps.selection-pressure";


	public int produce(final int min, 
			final int max, 
			final int start,
			final int subpopulation,
			final Individual[] inds,
			final EvolutionState state,
			final int thread) 
	{



		double slectionPressure = state.parameters.getDouble(new Parameter(ALPS_SELECTION_PRESSURE), null);
		int n;


		if(Engine.alps.layers.get(Engine.alps.index).getIsBottomLayer() || 
				state.random[0].nextDouble()<=slectionPressure || 
						Engine.alps.layers.get(Engine.alps.index-1).evolutionState.population.subpops[0].individuals.length==0)
		{
			// grab individuals from our source and stick 'em right into inds.
			// we'll modify them from there
			n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);

			// should we bother?
			if (!state.random[thread].nextBoolean(likelihood))
				return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did

		}
		else//select individual from lower layer
		{ 
			// grab individuals from our source and stick 'em right into inds.
			// we'll modify them from there
			n = sources[0].produce(min,max,start,subpopulation,inds,
					Engine.alps.layers.get(Engine.alps.index-1).evolutionState,thread);

			// should we bother?
			if (!state.random[thread].nextBoolean(likelihood))
				return reproduce(n, start, subpopulation, inds, 
						Engine.alps.layers.get(Engine.alps.index-1).evolutionState, thread, false);  // DON'T produce children from source -- we already did

		}




		// clone the individuals if necessary
		if (!(sources[0] instanceof BreedingPipeline))
			for(int q=start;q<n+start;q++)
				inds[q] = (Individual)(inds[q].clone());

		// mutate 'em
		for(int q=start;q<n+start;q++)
		{

		
			if(state.generation != inds[q].generationCount/*!parents[0].parentFlag*/) 
			{
				inds[q].age++;
				inds[q].generationCount = state.generation;
			}



			((VectorIndividual)inds[q]).defaultMutate(state,thread);
			((VectorIndividual)inds[q]).evaluated=false;
		}

		return n;
	}

}


