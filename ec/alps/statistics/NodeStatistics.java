/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */


package ec.alps.statistics;
import ec.*;
import ec.alps.Engine;
import ec.EvolutionState;
import ec.alps.util.TreeAnalyzer;
import ec.simple.SimpleProblemForm;
import ec.simple.SimpleStatistics;
import ec.steadystate.*;

import java.io.IOException;

import ec.util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

/* 
 * SimpleStatistics.java
 * 
 * Created: Tue Aug 10 21:10:48 1999
 * By: Sean Luke
 */

/**
 * A basic Statistics class suitable for simple problem applications.
 *
 * SimpleStatistics prints out the best individual, per subpopulation,
 * each generation.  At the end of a run, it also prints out the best
 * individual of the run.  SimpleStatistics outputs this data to a log
 * which may either be a provided file or stdout.  Compressed files will
 * be overridden on restart from checkpoint; uncompressed files will be 
 * appended on restart.
 *
 * <p>SimpleStatistics implements a simple version of steady-state statistics:
 * if it quits before a generation boundary,
 * it will include the best individual discovered, even if the individual was discovered
 * after the last boundary.  This is done by using individualsEvaluatedStatistics(...)
 * to update best-individual-of-generation in addition to doing it in
 * postEvaluationStatistics(...).

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>gzip</tt><br>
 <font size=-1>boolean</font></td>
 <td valign=top>(whether or not to compress the file (.gz suffix added)</td></tr>
 <tr><td valign=top><i>base.</i><tt>file</tt><br>
 <font size=-1>String (a filename), or nonexistant (signifies stdout)</font></td>
 <td valign=top>(the log for statistics)</td></tr>
 </table>

 *
 * @author Sean Luke
 * @version 1.0 
 */
/**
 * Modified to include basic ALPS integration
 * This prints node statistics and is used to print node usage/data of GP trees
 * @author Anthony Awuley
 *
 */
public class NodeStatistics extends Statistics implements SteadyStateStatisticsForm //, ec.eval.ProvidesBestSoFar
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;

	public Individual[] getBestSoFar() { return best_of_run; }

	/** log file parameter */
	public static final String P_STATISTICS_FILE = "file";

	/** compress? */
	public static final String P_COMPRESS = "gzip";

	public static final String P_DO_FINAL = "do-final";
	public static final String P_DO_GENERATION = "do-generation";
	public static final String P_DO_MESSAGE = "do-message";
	public static final String P_DO_DESCRIPTION = "do-description";
	public static final String P_DO_PER_GENERATION_DESCRIPTION = "do-per-generation-description";

	/** The Statistics' log */
	public int statisticslog = 0;  // stdout

	/** The best individual we've found so far */
	public Individual[] best_of_run = null;

	/** Should we compress the file? */
	public boolean compress;
	public boolean doFinal;
	public boolean doGeneration;
	public boolean doMessage;
	public boolean doDescription;
	public boolean doPerGenerationDescription;

	public static boolean  isALPSEA      = true;

	public void setup(final EvolutionState state, final Parameter base)
	{
		super.setup(state,base);

		compress = state.parameters.getBoolean(base.push(P_COMPRESS),null,false);

		File statisticsFile = state.parameters.getFile(
				base.push(P_STATISTICS_FILE),null);

		doFinal = state.parameters.getBoolean(base.push(P_DO_FINAL),null,true);
		doGeneration = state.parameters.getBoolean(base.push(P_DO_GENERATION),null,true);
		doMessage = state.parameters.getBoolean(base.push(P_DO_MESSAGE),null,true);
		doDescription = state.parameters.getBoolean(base.push(P_DO_DESCRIPTION),null,true);
		doPerGenerationDescription = state.parameters.getBoolean(base.push(P_DO_PER_GENERATION_DESCRIPTION),null,false);

		if (silentFile)
		{
			statisticslog = Output.NO_LOGS;
		}
		else if (statisticsFile!=null)
		{
			try
			{
				statisticslog = state.output.addLog(statisticsFile, !compress, compress);
			}
			catch (IOException i)
			{
				state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
			}
		}
		else state.output.warning("No statistics file specified, printing to stdout at end.", base.push(P_STATISTICS_FILE));
	}




	public void postInitializationStatistics(final EvolutionState state)
	{
		super.postInitializationStatistics(state);

		// set up our best_of_run array -- can't do this in setup, because
		// we don't know if the number of subpopulations has been determined yet
		best_of_run = new Individual[state.population.subpops.length];
	}

	/** Logs the best individual of the generation. */
	public void postEvaluationStatistics(final EvolutionState state)
	{
		super.postEvaluationStatistics(state);

		// for now we just print the best fitness per subpopulation.

		/*
		 * At the beginning of evolution, UNSET FREQUENCY count of all upper layers excluding layer 0
		 * Layer 0 might begin with initial or default frequency settings specified in the parameter file
		 * 
		 * DONT care if Engine.use_only_default_node_pr is true
		 */
		try
		{ //this avoid NullPinterException error when using canonical EA, in which case Layers are not defined
			if(Engine.alps.index==0 && Engine.completeGenerationalCount==0 && !Engine.fsalps_use_only_default_node_pr)
				for(int l=1;l<Engine.alps.layers.size();l++)
					//Engine.alps.layers.get(l).evolutionState.nodeCountTerminalSet =
					TreeAnalyzer.unsetNodeCount(
							Engine.alps.layers.get(l).evolutionState,
							Engine.alps.layers.get(l).evolutionState.nodeCountTerminalSet);
		}
		catch (NullPointerException e)
		{
			isALPSEA = false;
		}


		/* 
		 * unset values of this.nodeCountTerminalSet at the beginning of every generation 
		 * this is to ensure that a gnerational count is maintained without accumulating all counts
		 * from all layers
		 * if(!Engine.use_only_default_node_pr)
		 */
		//state.nodeCountTerminalSet = 
		TreeAnalyzer.unsetNodeCount(state, state.nodeCountTerminalSet);

		Individual[] best_i = new Individual[state.population.subpops.length];  // quiets compiler complaints
		for(int x=0;x<state.population.subpops.length;x++)
		{
			best_i[x] = state.population.subpops[x].individuals[0];
			/* for efficiency, its best to start from y=1 to test for best individual. 
			 * started from 0 because node statistics data is generated in the same loop
			 */
			for(int y=0;y<state.population.subpops[x].individuals.length;y++) 
			{
				/* gather statistics of node usage in entire population 
				 * state.nodeCountTerminalSet is updated
				 */
				state.population.subpops[x].individuals[y].gatherIndividualNodeStats(state,state.nodeCountTerminalSet);

				//find best individual
				if (state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness))
					best_i[x] = state.population.subpops[x].individuals[y];
			}

			// now test to see if it's the new best_of_run
			if (best_of_run[x]==null || best_i[x].fitness.betterThan(best_of_run[x].fitness))
				best_of_run[x] = (Individual)(best_i[x].clone());
		}
		//print the best-of-generation individual 

		//if (doGeneration) state.output.print("" + Engine.completeGenerationalCount,statisticslog);
		if (doGeneration && isALPSEA) 
			state.output.print(Engine.globalEvaluations+"\t",statisticslog);
		else //when using canonical EA
			state.output.print(state.generation+"\t",statisticslog);

		//if (doGeneration) state.output.println("Best Individual:",statisticslog);
		for(int x=0;x<state.population.subpops.length;x++)
		{
			//if (doGeneration) state.output.println("Subpopulation " + x + ":",statisticslog);

			/* use this to gather statistics of best individual in population */
			//if (doGeneration) best_i[x].gatherIndividualNodeStats(state,statisticslog,false);


			/* PRINT all nodes with related usage frequency per layer */
			if (doGeneration)
				for (Entry<String, Double> entry : state.nodeCountTerminalSet.entrySet()) 
					state.output.print(entry.getValue()+"\t",statisticslog);


		}
		state.output.println(" ",statisticslog);
	}

	/** Allows MultiObjectiveStatistics etc. to call super.super.finalStatistics(...) without
        calling super.finalStatistics(...) */
	protected void bypassFinalStatistics(EvolutionState state, int result)
	{ super.finalStatistics(state, result); }

	/** Logs the best individual of the run. */
	public void finalStatistics(final EvolutionState state, final int result)
	{
		super.finalStatistics(state,result);

		// for now we just print the best fitness 

		/* this is to get the terminals*/
		Map<String, Double>  bestIndividualTerminalSet  = state.nodeCountTerminalSet;
		/* now clear any default values*/
		TreeAnalyzer.unsetNodeCount(state, bestIndividualTerminalSet);


		if (doFinal) 
			state.output.println("\nBest Individual of Run:",statisticslog);
		for(int x=0;x<state.population.subpops.length;x++ )
		{
			if (doFinal) state.output.println("Subpopulation " + x + ":",statisticslog);
			/* tree depth */
			if (doFinal) best_of_run[x].individualTreeDepth(state,statisticslog);
			/*get node count for best individual of generation*/
			if (doFinal) best_of_run[x].printTerminalCount(state,bestIndividualTerminalSet,statisticslog);

			/* print tree size*/
			if (doFinal) state.output.println("Tree Size " + best_of_run[x].size(),statisticslog);

			if (doFinal) best_of_run[x].printIndividualForHumans(state,statisticslog);

			if (doMessage && !silentPrint) state.output.message("Subpop " + x + " best fitness of run: " + best_of_run[x].fitness.fitnessToStringForHumans());

			// finally describe the winner if there is a description
			//if (doFinal && doDescription) 
				//if (state.evaluator.p_problem instanceof SimpleProblemForm)
					//((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(state, best_of_run[x], x, 0, statisticslog);      
		}

		// we're done! @anthony
		//if (doFinal) state.output.println("", statisticslog);

	}
}
