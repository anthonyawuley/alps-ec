/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.app.canonical.ionosphere;
import ec.*;
import ec.simple.SimpleProblemForm;
import ec.steadystate.*;
import java.io.IOException;
import ec.util.*;
import java.io.File;


public class SymbolicRegressionSimpleStatistics extends Statistics implements SteadyStateStatisticsForm //, ec.eval.ProvidesBestSoFar
    {
    /**
     *
     */
    public Individual[] getBestSoFar() { return best_of_run; }

    /** log file parameter */
    public static final String P_STATISTICS_FILE = "file";

    /** compress? */
    public static final String P_COMPRESS = "gzip";

    /** The Statistics' log */
    public int statisticslog;

    /** The best individual we've found so far */
    public Individual[] best_of_run;

    /** Should we compress the file? */
    public boolean compress;


    public SymbolicRegressionSimpleStatistics() { best_of_run = null; statisticslog = 0; /* stdout */ }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        compress = state.parameters.getBoolean(base.push(P_COMPRESS),null,false);

        File statisticsFile = state.parameters.getFile(
            base.push(P_STATISTICS_FILE),null);

        if (statisticsFile!=null)
            try
                {
                statisticslog = state.output.addLog(statisticsFile, !compress, compress);
                }
            catch (IOException i)
                {
                state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
                }
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
        // print the best-of-generation individual
        state.output.print(state.generation + ",",statisticslog);
        // for now we just print the best fitness per subpopulation.
        Individual[] best_i = new Individual[state.population.subpops.length];  // quiets compiler complaints
        for(int x=0;x<state.population.subpops.length;x++)
            {
            best_i[x] = state.population.subpops[x].individuals[0];
            for(int y=1;y<state.population.subpops[x].individuals.length;y++)
                if (state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness))
                    best_i[x] = state.population.subpops[x].individuals[y];

            // find average for each generation
            int total = 0;
            for (int y = 1;y<state.population.subpops[x].individuals.length; y++) {
            	String temp = state.population.subpops[x].individuals[y].fitness.fitnessToStringForHumans();
            	int start = temp.indexOf('=');
            	int end = temp.indexOf('.');
            	int misses = Integer.parseInt(temp.substring(start+1, end));
            	total += misses;
            }

            // now test to see if it's the new best_of_run
            if (best_of_run[x]==null || best_i[x].fitness.betterThan(best_of_run[x].fitness))
                best_of_run[x] = (Individual)(best_i[x].clone());

            state.output.print((total/state.population.subpops[x].individuals.length) + ",", statisticslog);
            String temp = best_of_run[x].fitness.fitnessToStringForHumans();
        	int start = temp.indexOf('=');
        	int end = temp.indexOf('.');
        	int bestHits = Integer.parseInt(temp.substring(start+1, end));
            state.output.println(bestHits + "", statisticslog);
            }

        }

    /** Logs the best individual of the run. */
    public void finalStatistics(final EvolutionState state, final int result)
        {
        super.finalStatistics(state,result);

        // for now we just print the best fitness

        state.output.println("\nBest Individual of Run:",statisticslog);
        for(int x=0;x<state.population.subpops.length;x++ )
            {
            state.output.println("Subpopulation " + x + ":",statisticslog);
            best_of_run[x].printIndividualForHumans(state,statisticslog);
            state.output.message("Subpop " + x + " best fitness of run: " + best_of_run[x].fitness.fitnessToStringForHumans());

            // finally describe the winner if there is a description
            if (state.evaluator.p_problem instanceof SimpleProblemForm)
                ((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(state, best_of_run[x], x, 0, statisticslog);
            }
    }}
