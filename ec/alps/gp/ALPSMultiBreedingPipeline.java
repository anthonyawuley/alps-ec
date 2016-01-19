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

/* 
 * MultiBreedingPipeline.java
 * 
 * Created: December 28, 1999
 * By: Sean Luke
 */

/**
 * MultiBreedingPipeline is a BreedingPipeline stores some <i>n</i> child sources; 
 * each time it must produce an individual or two, 
 * it picks one of these sources at random and has it do the production.

 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 If by <i>base</i>.<tt>generate-max</tt> is <tt>true</tt>, then always the maximum
 number of the typical numbers of any child source.  If <tt>false</itt>, then varies
 depending on the child source picked.

 <p><b>Number of Sources</b><br>
 Dynamic.  As many as the user specifies.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>generate-max</tt><br>
 <font size=-1> bool = <tt>true</tt> (default) or <tt>false</tt></font></td>
 <td valign=top>(Each time produce(...) is called, should the MultiBreedingPipeline
 force all its sources to produce exactly the same number of individuals as the largest
 typical number of individuals produced by any source in the group?)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 breed.multibreed

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class ALPSMultiBreedingPipeline extends MultiBreedingPipeline
{
	private final String SELECTION_PRESSURE = "selection-pressure";
	

	public void setup(final EvolutionState state, final Parameter base)
	{
		super.setup(state,base);

		/* 
		 * @author anthony
		 * enforce setting of parameter : alps.selection-pressure 
		 */
		if (!state.parameters.exists(Engine.base().push(SELECTION_PRESSURE), null))
			state.output.fatal("set selection pressure for alps inter-layer individual selection:  \"alps."+SELECTION_PRESSURE+"\" ");
		
		
	}


}
