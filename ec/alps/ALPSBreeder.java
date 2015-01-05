/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */


package ec.alps;
import ec.Initializer;
import ec.Individual;
import ec.BreedingPipeline;
import ec.Breeder;
import ec.EvolutionState;
import ec.Population;
import ec.simple.SimpleBreeder;
import ec.util.*;

/* 
 * SimpleBreeder.java
 * 
 * Created: Tue Aug 10 21:00:11 1999
 * By: Sean Luke
 */

/**
 * Breeds each subpopulation separately, with no inter-population exchange,
 * and using a generational approach.  A SimpleBreeder may have multiple
 * threads; it divvys up a subpopulation into chunks and hands one chunk
 * to each thread to populate.  One array of BreedingPipelines is obtained
 * from a population's Species for each operating breeding thread.
 *
 * <p>Prior to breeding a subpopulation, a SimpleBreeder may first fill part of the new
 * subpopulation up with the best <i>n</i> individuals from the old subpopulation.
 * By default, <i>n</i> is 0 for each subpopulation (that is, this "elitism"
 * is not done).  The elitist step is performed by a single thread.
 *
 * <p>If the <i>sequential</i> parameter below is true, then breeding is done specially:
 * instead of breeding all Subpopulations each generation, we only breed one each generation.
 * The subpopulation index to breed is determined by taking the generation number, modulo the
 * total number of subpopulations.  Use of this parameter outside of a coevolutionary context
 * (see ec.coevolve.MultiPopCoevolutionaryEvaluator) is very rare indeed.
 *
 * <p>SimpleBreeder adheres to the default-subpop parameter in Population: if either an 'elite'
 * or 'reevaluate-elites' parameter is missing, it will use the default subpopulation's value
 * and signal a warning.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><tt><i>base</i>.elite.<i>i</i></tt><br>
 <font size=-1>int >= 0 (default=0)</font></td>
 <td valign=top>(the number of elitist individuals for subpopulation <i>i</i>)</td></tr>
 <tr><td valign=top><tt><i>base</i>.reevaluate-elites.<i>i</i></tt><br>
 <font size=-1>boolean (default = false)</font></td>
 <td valign=top>(should we reevaluate the elites of subpopulation <i>i</i> each generation?)</td></tr>
 <tr><td valign=top><tt><i>base</i>.sequential</tt><br>
 <font size=-1>boolean (default = false)</font></td>
 <td valign=top>(should we breed just one subpopulation each generation (as opposed to all of them)?)</td></tr>
 </table>
 *
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class ALPSBreeder extends SimpleBreeder
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1;
	public static final String POP_SIZE = "size";
	public static final String SUB_POP  = "subpop";

	public int [] subpopSize;



	public void setup(final EvolutionState state, final Parameter base) 
	{
		Parameter p = new Parameter(Initializer.P_POP).push(Population.P_SIZE);
		int size = state.parameters.getInt(p,null,1);  // if size is wrong, we'll let Population complain about it -- for us, we'll just make 0-sized arrays and drop out.

		/*
		 * @author anthony
		 */
		int mysubpops = state.parameters.getInt(new Parameter(Initializer.P_POP).push(Population.P_SIZE), null);
		subpopSize = new int[mysubpops];


		eliteFrac = new double[size];
		elite = new int[size];
		for(int i = 0; i < size; i++) 
			eliteFrac[i] = elite[i] = NOT_SET;
		reevaluateElites = new boolean[size];

		sequentialBreeding = state.parameters.getBoolean(base.push(P_SEQUENTIAL_BREEDING), null, false);
		if (sequentialBreeding && (size == 1)) // uh oh, this can't be right
			state.output.fatal("The Breeder is breeding sequentially, but you have only one population.", base.push(P_SEQUENTIAL_BREEDING));

		clonePipelineAndPopulation =state.parameters.getBoolean(base.push(P_CLONE_PIPELINE_AND_POPULATION), null, true);
		if (!clonePipelineAndPopulation && (state.breedthreads > 1)) // uh oh, this can't be right
			state.output.fatal("The Breeder is not cloning its pipeline and population, but you have more than one thread.", base.push(P_CLONE_PIPELINE_AND_POPULATION));

		int defaultSubpop = state.parameters.getInt(new Parameter(Initializer.P_POP).push(Population.P_DEFAULT_SUBPOP), null, 0); 
		for(int x=0;x<size;x++)
		{
			/*
			 * @author anthony
			 */
			subpopSize[x] = state.parameters.getInt(new Parameter(Initializer.P_POP).push(Population.P_SUBPOP).push(x+"").push(POP_SIZE), null, 0);
			//System.out.println(subpopSize[x]); System.exit(0);

			// get elites
			if (state.parameters.exists(base.push(P_ELITE).push(""+x),null))
			{
				if (state.parameters.exists(base.push(P_ELITE_FRAC).push(""+x),null))
					state.output.error("Both elite and elite-frac specified for subpouplation " + x + ".", base.push(P_ELITE_FRAC).push(""+x), base.push(P_ELITE_FRAC).push(""+x));
				else 
				{
					elite[x] = state.parameters.getIntWithDefault(base.push(P_ELITE).push(""+x),null,0);
					if (elite[x] < 0)
						state.output.error("Elites for subpopulation " + x + " must be an integer >= 0", base.push(P_ELITE).push(""+x));
				}
			}
			else if (state.parameters.exists(base.push(P_ELITE_FRAC).push(""+x),null))
			{
				eliteFrac[x] = state.parameters.getDoubleWithMax(base.push(P_ELITE_FRAC).push(""+x),null,0.0, 1.0);
				if (eliteFrac[x] < 0.0)
					state.output.error("Elite Fraction of subpopulation " + x + " must be a real value between 0.0 and 1.0 inclusive", base.push(P_ELITE_FRAC).push(""+x));
			}
			else if (defaultSubpop >= 0)
			{
				if (state.parameters.exists(base.push(P_ELITE).push(""+defaultSubpop),null))
				{
					elite[x] = state.parameters.getIntWithDefault(base.push(P_ELITE).push(""+defaultSubpop),null,0);
					if (elite[x] < 0)
						state.output.warning("Invalid default subpopulation elite value.");  // we'll fail later
				}
				else if (state.parameters.exists(base.push(P_ELITE_FRAC).push(""+defaultSubpop),null))
				{
					eliteFrac[x] = state.parameters.getDoubleWithMax(base.push(P_ELITE_FRAC).push(""+defaultSubpop),null,0.0, 1.0);
					if (eliteFrac[x] < 0.0)
						state.output.warning("Invalid default subpopulation elite-frac value.");  // we'll fail later
				}
				else  // elitism is 0
				{
					elite[x] = 0;
				}
			}
			else // elitism is 0
			{
				elite[x] = 0;
			}

			// get reevaluation
			if (defaultSubpop >= 0 && !state.parameters.exists(base.push(P_REEVALUATE_ELITES).push(""+x),null))
			{
				reevaluateElites[x] = state.parameters.getBoolean(base.push(P_REEVALUATE_ELITES).push(""+defaultSubpop), null, false);
				if (reevaluateElites[x])
					state.output.warning("Elite reevaluation not specified for subpopulation " + x + ".  Using values for default subpopulation " + defaultSubpop + ": " + reevaluateElites[x]);
			}
			else
			{
				reevaluateElites[x] = state.parameters.getBoolean(base.push(P_REEVALUATE_ELITES).push(""+x), null, false);
			}
		}

		state.output.exitIfErrors();
	}



   /** 
	* Elites are often stored in the top part of the subpopulation; this function returns what part 
	* of the subpopulation contains individuals to replace with newly-bred 
	* ones(up to but not including the elites). 
    *
    * @author anthony
    * This was modified to use the number of specified population size in parameter file instead of total
    * size of individuals available in current population as a limit.
    * 
    * ArrayIndexOutOfBoundsException: Uncomment when this is fixed in 
    *    -SelectionMethod.java:76
    *    this occurs when breeding is attempted on an empty population or a population with lesser size
    *    than specified parameter population
    * 
    
	public int computeSubpopulationLength(EvolutionState state, Population newpop, int subpopulation, int threadnum)
	{ 
		if (!shouldBreedSubpop(state, subpopulation, threadnum))
			return subpopSize[subpopulation];  // we're not breeding the population, just copy over the whole thing
		return subpopSize[subpopulation] - numElites(state, subpopulation); // we're breeding population, so elitism may have happened 
	}
    */


	/** A private helper function for breedPopulation which loads elites into
        a subpopulation. */

	protected void loadElites(EvolutionState state, Population newpop)
	{
		// are our elites small enough?
		for(int x=0;x<state.population.subpops.length;x++)
		{

			/*
			 * ALPS modification
			 * This is to ensure that a layer with population less than number of elites runs successfully
			 * This is achieved by scaling the number of elites in that subpopulation to the number of population
			 * 
			 * @author anthony
			 */
			if(state.population.subpops[x].individuals.length < numElites(state, x))
			{
				elite[x] = state.population.subpops[x].individuals.length;

			}
			else
			{ //original condition

				if (numElites(state, x)>state.population.subpops[x].individuals.length)
					state.output.error("The number of elites for subpopulation " + x + " exceeds the actual size of the subpopulation", 
							new Parameter(EvolutionState.P_BREEDER).push(P_ELITE).push(""+x));
				if (numElites(state, x)==state.population.subpops[x].individuals.length)
					state.output.warning("The number of elites for subpopulation " + x + " is the actual size of the subpopulation", 
							new Parameter(EvolutionState.P_BREEDER).push(P_ELITE).push(""+x));
			}

		}
		state.output.exitIfErrors();

		// we assume that we're only grabbing a small number (say <10%), so
		// it's not being done multithreaded
		for(int sub=0;sub<state.population.subpops.length;sub++) 
		{
			if (!shouldBreedSubpop(state, sub, 0))  // don't load the elites for this one, we're not doing breeding of it
			{
				continue;
			}

			// if the number of elites is 1, then we handle this by just finding the best one.
			if (numElites(state, sub)==1)
			{
				int best = 0;
				Individual[] oldinds = state.population.subpops[sub].individuals;
				for(int x=1;x<oldinds.length;x++)
					if (oldinds[x].fitness.betterThan(oldinds[best].fitness))
						best = x;
				Individual[] inds = newpop.subpops[sub].individuals;

				inds[inds.length-1] = (Individual)(oldinds[best].clone());

				/**
				 * ALPS: ELITE CONTROL
				 * increase age of the elite individuals
				 * @author anthony
				 */
				if(state.generation != inds[inds.length-1].generationCount/*!oldinds[best].parentFlag*/) 
				{ 
					inds[inds.length-1].age++;
					inds[inds.length-1].generationCount = state.generation;
				} 

			}
			else if (numElites(state, sub)>0)  // we'll need to sort
			{
				int[] orderedPop = new int[state.population.subpops[sub].individuals.length];
				for(int x=0;x<state.population.subpops[sub].individuals.length;x++) 
					orderedPop[x] = x;

				// sort the best so far where "<" means "not as fit as"
				QuickSort.qsort(orderedPop, new EliteComparator(state.population.subpops[sub].individuals));
				// load the top N individuals

				Individual[] inds = newpop.subpops[sub].individuals;
				Individual[] oldinds = state.population.subpops[sub].individuals;
				for(int x=inds.length-numElites(state, sub);x<inds.length;x++)
				{
					inds[x] = (Individual)(oldinds[orderedPop[x]].clone());
                   
					/**
					 * ALPS: ELITE CONTROL
					 * increase age of elite individuals
					 * @author anthony
					 */
					if(state.generation != inds[x].generationCount/*!oldinds[orderedPop[x]].parentFlag*/) 
					{ 
						inds[x].age++;
						inds[x].generationCount = state.generation;
					} 
				}
			}
		}

		// optionally force reevaluation
		unmarkElitesEvaluated(state, newpop);
	}
	
		
	
}

