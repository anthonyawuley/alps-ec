
package ec.alps;
import ec.*;
import ec.alps.layers.Layer;
import ec.alps.layers.Replacement;
import ec.util.Checkpoint;
import ec.util.Parameter;



/**
 * Generational Evolution State for ALPS. extends EvolutionState from ECJ
 * @author Anthony Awuley and Sean Luke
 */
public class GenEvolutionState extends EvolutionState
{

	/** */
	private static final long serialVersionUID = 1;

	/**
	 * At setup, startFresh(Layer) is called to setup a new layer
	 */
	public void startFresh(Layer l) 
	{
		/*
		 * consider putting this in a better location
		 * transfered here because whilst in EvolutionState, canonical GP
		 * always flags an erroor because replacement parameters are not set in
		 * the parameter file
		 */
		Parameter p = Engine.base().push(P_REPLACEMENT);
		replacement = (Replacement)
				(parameters.getInstanceForParameter(p,null,Replacement.class));
		replacement.setup(this,p);
		
		l.evolutionState.output.message("\n\nSetting up layer :" + l.getId()/* + " Global Generation # " + Engine.completeGenerationalCount*/);
		l.evolutionState.output.message("Maximum Age: "+l.getMaxAge());
		l.evolutionState.output.message("Maximum Generation: "+l.getGenerations());

		setup(this,null);  // a garbage Parameter

		// POPULATION INITIALIZATION
		//output.message("Initializing Generation 0");
		statistics.preInitializationStatistics(this);
		population = initializer.initialPopulation(this,0); // unthreaded
		statistics.postInitializationStatistics(this);

		// Compute generations from evaluations if necessary
		if (numEvaluations > UNDEFINED)
		{   
			// compute a generation's number of individuals
			int generationSize = 0;
			for (int sub=0; sub < population.subpops.length; sub++)  
			{ 
				generationSize += population.subpops[sub].individuals.length;  // so our sum total 'generationSize' will be the initial total number of individuals
			}

			if (numEvaluations < generationSize)
			{
				numEvaluations = generationSize;
				numGenerations = 1;
				output.warning("Using evaluations, but evaluations is less than the initial total population size (" + generationSize + ").  Setting to the populatiion size.");
			}
			else 
			{
				if (numEvaluations % generationSize != 0)
					output.warning("Using evaluations, but initial total population size does not divide evenly into it.  Modifying evaluations to a smaller value ("
							+ ((numEvaluations / generationSize) * generationSize) +") which divides evenly.");  // note integer division
				numGenerations = (int) (numEvaluations / generationSize);  // note integer division
				numEvaluations = numGenerations * generationSize;
			} 
			output.message("Generations will be " + numGenerations);
		}

		// compute population size per layer
		int popSize = 0;
		for (int sub=0; sub < population.subpops.length; sub++) 
			popSize += population.subpops[sub].individuals.length; 
		//This is the same as population size
		Engine.generationSize = popSize;
		
		/** TODO Modify numGenerations and numEvaluations for ALPS  */
		l.numGenerations = numGenerations;

		//begin initial setup with empty population population
		Individual [] ind = new Individual[0];
		this.population.subpops[0].individuals = ind;

		// INITIALIZE CONTACTS -- done after initialization to allow
		// a hook for the user to do things in Initializer before
		// an attempt is made to connect to island models etc.
		exchanger.initializeContacts(this);
		evaluator.initializeContacts(this);

	}



	/**
	 * After setup,
	 */
	public void startFresh() 
	{
		output.message("\n\nSetting up layer " + Engine.alps.index + 
				" Global Generation # " + Engine.completeGenerationalCount);

		/*
		 * only perform this action at the beginning of evolution
		 * NB: statistics collection can't keep file for layer 0 only works for first round age gap generations if this is
		 *     skipped (no if condition)
		 */
		if(Engine.completeGenerationalCount==0) //was 1
		{
			setup(this,null);  // a garbage Parameter
		}

		// POPULATION INITIALIZATION
		output.message("Initializing Generation 0");
		statistics.preInitializationStatistics(this);
		population = initializer.initialPopulation(this, 0); // unthreaded
		statistics.postInitializationStatistics(this);

		// Compute generations from evaluations if necessary
		/* Modify numGenerations and numEvaluations for ALPS  */
		//numGenerations = alps.layers.get(0).evaluation;
		//alps.layers.get(0).numGenerations = numGenerations;



		// INITIALIZE CONTACTS -- done after initialization to allow
		// a hook for the user to do things in Initializer before
		// an attempt is made to connect to island models etc.
		exchanger.initializeContacts(this);
		evaluator.initializeContacts(this);
	}



	public int evolve()
	{
		if (generation > 0) 
			output.message(
					"L"+ Engine.alps.index + " "
					+ "Gen: " + generation + " "
					+ "Global Gen: # " + Engine.completeGenerationalCount);

		//population.subpops[0].individuals[0]

		// EVALUATION
		statistics.preEvaluationStatistics(this);
		evaluator.evaluatePopulation(this);
		statistics.postEvaluationStatistics(this);

		// SHOULD WE QUIT?
		if (evaluator.runComplete(this) && quitOnRunComplete)
		{
			output.message("Found Ideal Individual");
			return R_SUCCESS;
		}

		// SHOULD WE QUIT? -- 
		/*
		if (generation == numGenerations-1)
		{
			return R_FAILURE;
		}*/

		// SHOULD WE QUIT? -- 
		if (Engine.globalEvaluations >= Engine.alpsEvaluations)
			return R_FAILURE;
	

		// PRE-BREEDING EXCHANGING
		statistics.prePreBreedingExchangeStatistics(this);
		population = exchanger.preBreedingExchangePopulation(this);
		statistics.postPreBreedingExchangeStatistics(this);

		String exchangerWantsToShutdown = exchanger.runComplete(this);
		if (exchangerWantsToShutdown!=null)
		{ 
			output.message(exchangerWantsToShutdown);
			/*
			 * Don't really know what to return here.  The only place I could
			 * find where runComplete ever returns non-null is 
			 * IslandExchange.  However, that can return non-null whether or
			 * not the ideal individual was found (for example, if there was
			 * a communication error with the server).
			 * 
			 * Since the original version of this code didn't care, and the
			 * result was initialized to R_SUCCESS before the while loop, I'm 
			 * just going to return R_SUCCESS here. 
			 */

			return R_SUCCESS;
		}

		// BREEDING
		statistics.preBreedingStatistics(this);

		population = breeder.breedPopulation(this);

		// POST-BREEDING EXCHANGING
		statistics.postBreedingStatistics(this);

		// POST-BREEDING EXCHANGING
		statistics.prePostBreedingExchangeStatistics(this);
		population = exchanger.postBreedingExchangePopulation(this);
		statistics.postPostBreedingExchangeStatistics(this);

		// INCREMENT GENERATION AND CHECKPOINT
		generation++;
		if (checkpoint && generation%checkpointModulo == 0) 
		{
			output.message("Checkpointing");
			statistics.preCheckpointStatistics(this);
			Checkpoint.setCheckpoint(this);
			statistics.postCheckpointStatistics(this);
		}
		
		return R_NOTDONE;
	}

	/**
	 * @param result
	 */
	public void finish(int result) 
	{
		//Output.message("Finishing");
		/* finish up -- we completed. */
		statistics.finalStatistics(this,result);
		finisher.finishPopulation(this,result);
		exchanger.closeContacts(this,result);
		evaluator.closeContacts(this,result);
	}



}
