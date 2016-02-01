package ec.alps.gp.breed;

import ec.EvolutionState;
import ec.Individual;
import ec.SelectionMethod;
import ec.alps.Engine;
import ec.breed.ReproductionPipeline;

/**
 * Extends ReproductionPipeline from ECJ to include ageing property of parents and offsprings
 * In ALPS. In ALPS, a parents age is incremented by 1 and an offspring takes the age of the oldest
 * parent + 1
 * 
 * @author Anthony Awuley
 *
 */
public class Reproduction extends ReproductionPipeline{

	
    /** */
	private static final long serialVersionUID = 1;
	
	private final String SELECTION_PRESSURE = "selection-pressure";

	public int produce(
            final int min, 
            final int max, 
            final int start,
            final int subpopulation,
            final Individual[] inds,
            final EvolutionState state,
            final int thread) 
            {
    	
		    double selectionPressure = state.parameters.getDouble(Engine.base().push(SELECTION_PRESSURE), null);

    	    int n;
    	    
    	    if(Engine.alps.layers.get(Engine.alps.index).getIsBottomLayer() || 
        			(Engine.alps.index>0 && ((state.random[0].nextDouble()<=selectionPressure) || 
        					Engine.alps.layers.get(Engine.alps.index-1).evolutionState.population.subpops[subpopulation].individuals.length<=0)))
        	{
               // grab individuals from our source and stick 'em right into inds.
               // we'll modify them from there
               n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);
        	}
    	    else /* select individual from lower layer */
    	    {
    	       // grab individuals from our source and stick 'em right into inds.
               // we'll modify them from there
    	       n = sources[0].produce(min,max,start,subpopulation,inds,
    	    		   Engine.alps.layers.get(Engine.alps.index-1).evolutionState,thread);
    	       
    	       /* When this flag is enabled, prevent increasing age for idividuals selected from lower layer for breeding  */
				if(Engine.alps_age_only_current_layer)
					for(int q=start; q < n+start; q++)
						inds[q].generationCount       = state.generation;
    	    }
    	    
    	    
            if (mustClone || sources[0] instanceof SelectionMethod)
                for(int q=start; q < n+start; q++)
                {
                	
                	if(state.generation != inds[q].generationCount/*!parents[0].parentFlag*/) 
         			{   //System.out.println("ANTHONY WAS HERE1! BEFORE:"+inds[q].age+"AFTER:"); System.exit(0);
                		inds[q].age++;
                		inds[q].generationCount = state.generation;
         			} 
        			
                    inds[q] = (Individual)(inds[q].clone());
                }
            return n;
            }

}
