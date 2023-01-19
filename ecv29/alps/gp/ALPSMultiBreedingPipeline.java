/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */


package ec.alps.gp;
import ec.*;
import ec.alps.Engine;
import ec.EvolutionState;
import ec.breed.MultiBreedingPipeline;
import ec.util.*;

/**
 * Extends MultiBreedingPipeline from ECJ to load selection pressure from parameter file for ALPS evolution
 * The selection pressure value is used to determine inter layer individual selection.
 * E.g. a value of 0.8 means 80% of the time, individuals are selected from Layer N and 20% of the time individuals
 * are selected from layer N-1
 * 
 * @author Anthony Awuley
 *
 */
public class ALPSMultiBreedingPipeline extends MultiBreedingPipeline
{
	private final String SELECTION_PRESSURE = "selection-pressure";
	

	public void setup(final EvolutionState state, final Parameter base)
	{
		super.setup(state,base);

		
		if (!state.parameters.exists(Engine.base().push(SELECTION_PRESSURE), null))
			state.output.fatal("set selection pressure for alps inter-layer individual selection:  \"alps."+SELECTION_PRESSURE+"\" ");
		
		
	}


}
