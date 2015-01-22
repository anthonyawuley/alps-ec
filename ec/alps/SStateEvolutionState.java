package ec.alps;

import java.util.HashMap;

import ec.EvolutionState;
import ec.Individual;
import ec.alps.layers.Layer;
import ec.alps.layers.Replacement;
import ec.alps.util.Operations;
import ec.steadystate.SteadyStateBreeder;
import ec.steadystate.SteadyStateEvaluator;
import ec.steadystate.SteadyStateEvolutionState;
import ec.steadystate.SteadyStateStatisticsForm;
import ec.util.Checkpoint;
import ec.util.Parameter;

public class SStateEvolutionState extends SteadyStateEvolutionState{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1;



	/**
	 * 
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
		
		l.evolutionState.output.message("\n\nSetting up layer :" + l.getId() + 
				                        " Global Generation # " + Engine.completeGenerationalCount);
		l.evolutionState.output.message("Maximum Age: "+l.getMaxAge());
		l.evolutionState.output.message("Maximum Generation: "+l.getGenerations());

		setup(this,null);  // a garbage Parameter

		// POPULATION INITIALIZATION
		//output.message("Initializing Generation 0");
		statistics.preInitializationStatistics(this);

		population = initializer.setupPopulation(this, 0);  // unthreaded.  We're NOT initializing here, just setting up.


		// INITIALIZE VARIABLES
		generationSize = 0;
		generationBoundary = false;
		firstTime = true; //@anthony default is true

		evaluations =  0; 
		whichSubpop = -1; 

		individualHash = new HashMap[population.subpops.length];
		for(int i=0;i<population.subpops.length; i++) individualHash[i] = new HashMap();

		individualCount = new int[population.subpops.length];
		for (int sub=0; sub < population.subpops.length; sub++)  
		{ 
			individualCount[sub]=0;
			generationSize += population.subpops[sub].individuals.length;  // so our sum total 'generationSize' will be the initial total number of individuals
		}

		if (numEvaluations > UNDEFINED && numEvaluations < generationSize)
			output.fatal("Number of evaluations desired is smaller than the initial population of individuals");


		/** Modify numGenerations and numEvaluations for ALPS  */
		Engine.generationSize = generationSize;

		/**
		 * EMPTY INITIAL POPULATION TO LEAVE SINGLE INDIVIDUAL - This is to prevent NullPointer Exception caused during initial inter-layer migrations
		 **/

		population.subpops[0].individuals = 
				Operations.emptyPop(population.subpops[0].individuals);


		// INITIALIZE CONTACTS -- done after initialization to allow
		// a hook for the user to do things in Initializer before
		// an attempt is made to connect to island models etc.
		exchanger.initializeContacts(this);
		evaluator.initializeContacts(this);
	}


	/**
	 * 
	 */
	public void startFresh() 
	{
		output.message("\n\nSetting up layer 0" + 
				" Global Generation # " + Engine.completeGenerationalCount);

		/**
		 * only perform this action at the beginning of evolution
		 * NB: statistics collection can't keep file for layer 0 only works for first round age gap generations if this is
		 *     skipped (no if condition)
		 */
		if(Engine.completeGenerationalCount==0) //was 1
			setup(this,null);  // a garbage Parameter

		// POPULATION INITIALIZATION
		//output.message("Initializing Generation 0 " + generation);
		statistics.preInitializationStatistics(this);
		population = initializer.setupPopulation(this, 0);  // unthreaded.  We're NOT initializing here, just setting up.

		// INITIALIZE VARIABLES
		generationSize = 0;
		generationBoundary = false;
		firstTime = true; 
		/*
		 * TODO 
		 * generation parameter not originally present 
		 * @author anthony
		 * responsible for some numbering issues
		 * when initialized, numbering is complete but statistics display for boundary generations are problematic
		 * 
		 */
		generation = 0;

		evaluations=0;  
		whichSubpop=-1; 

		individualHash = new HashMap[population.subpops.length];
		for(int i=0;i<population.subpops.length; i++) individualHash[i] = new HashMap();

		individualCount = new int[population.subpops.length];
		for (int sub=0; sub < population.subpops.length; sub++)  
		{ 
			individualCount[sub]=0;
			generationSize += population.subpops[sub].individuals.length;  // so our sum total 'generationSize' will be the initial total number of individuals
		}

		if (numEvaluations > UNDEFINED && numEvaluations < generationSize)
			output.fatal("Number of evaluations desired is smaller than the initial population of individuals");


		Engine.generationSize = generationSize;

		// INITIALIZE CONTACTS -- done after initialization to allow
		// a hook for the user to do things in Initializer before
		// an attempt is made to connect to island models etc.
		exchanger.initializeContacts(this);
		evaluator.initializeContacts(this);

	}


	boolean justCalledPostEvaluationStatistics = false;




	public int evolve()
	{
		int result = R_NOTDONE;
		
		/* Previous implementation. replaced by Operations.expectedPopulation(...) and howManyToBreed(...)
		 * int popSize = popSizeLayer(Engine.alps.layers.get(Engine.alps.index).evolutionState);
		 */
		
		int popSize = Operations.expectedPopulation(Engine.alps.layers.get(Engine.alps.index).evolutionState);
		howManyToBreed(Engine.alps.layers.get(Engine.alps.index).evolutionState);

		//statistics.prePreBreedingExchangeStatistics(this);
		//population = exchanger.preBreedingExchangePopulation(this);
		//statistics.postPreBreedingExchangeStatistics(this);

		// SHOULD WE QUIT? -- 
		if (Engine.globalEvaluations >= Engine.alpsEvaluations)
			return R_FAILURE;


		//perform steady state evolution for one layer population
		for(int k=0;k<popSize;k++)
			result = steadyEvolve(); 

		statistics.generationBoundaryStatistics(this); 
		statistics.postEvaluationStatistics(this); 

		//PRE-BREED EXCHANGE 
		statistics.postPostBreedingExchangeStatistics(this);

		/* AT REGULAR INTERVALS, CREATE INDIVIDUALS AND ASSIGN CURRENT EVALUATION COUNT */
		if(Engine.alps.layers.get(Engine.alps.index).initializerFlag)
			restartIndEvaluationCount(Engine.alps.layers.get(Engine.alps.index).evolutionState,Engine.completeEvaluationCount);

		/* Calculate age of individuals based on current evaluation count */
		calculateAge(Engine.alps.layers.get(Engine.alps.index).evolutionState,Engine.completeEvaluationCount);

		return  result;
	}




	public int steadyEvolve()
	{
		if (generationBoundary && generation > 0)
		{ 
			output.message("Generation " + generation+ "\t"
					+ "Evaluations " + evaluations + "\t"
					+ "Layer "+ Engine.alps.index + " "
					+ "Global Generation # " + Engine.completeGenerationalCount +"\t"
					+ "Global Evaluation # " + Engine.completeEvaluationCount);
			//output.message("Generation " + generation +"\tEvaluations " + evaluations);
			//statistics.generationBoundaryStatistics(this); 
			//statistics.postEvaluationStatistics(this); 
			justCalledPostEvaluationStatistics = true;
		}
		else
		{
			justCalledPostEvaluationStatistics = false;
		}

		if (firstTime) 
		{ 
			if (statistics instanceof SteadyStateStatisticsForm)
				((SteadyStateStatisticsForm)statistics).enteringInitialPopulationStatistics(this);
			statistics.postInitializationStatistics(this); 
			((SteadyStateBreeder)breeder).prepareToBreed(this, 0); // unthreaded 

			((SteadyStateEvaluator)evaluator).prepareToEvaluate(this, 0); // unthreaded 
			firstTime=false; 
		} 

		whichSubpop = (whichSubpop+1)%population.subpops.length;  // round robin selection

		// is the current subpop full? 
		boolean partiallyFullSubpop = (individualCount[whichSubpop] < population.subpops[whichSubpop].individuals.length);  

		// MAIN EVOLVE LOOP 
		if (((SteadyStateEvaluator) evaluator).canEvaluate())   // are we ready to evaluate? 
		{

			{
				Individual ind=null; 
				int numDuplicateRetries = population.subpops[whichSubpop].numDuplicateRetries; 

				for (int tries=0; tries <= numDuplicateRetries; tries++)  // see Subpopulation
				{ 
					if ( partiallyFullSubpop )   // is population full?
					{   //System.out.println("LAYERSSSSSS "+this.alps.index + " "+ this.alps.layers.get(this.alps.index).evolutionState.population.subpops[0].individuals.length + " tries"+tries);
						ind = population.subpops[whichSubpop].species.newIndividual(this, 0);  // unthreaded 
					}
					else  
					{   
						ind = ((SteadyStateBreeder)breeder).breedIndividual(this, whichSubpop,0); 
						statistics.individualsBredStatistics(this, new Individual[]{ind}); 
					}

					if (numDuplicateRetries >= 1)  
					{ 
						Object o = individualHash[whichSubpop].get(ind); 
						if (o == null) 
						{ 
							individualHash[whichSubpop].put(ind, ind); 
							break; 
						}
					}
				} // tried to cut down the duplicates 
				// evaluate the new individual
				((SteadyStateEvaluator)evaluator).evaluateIndividual(this, ind, whichSubpop);

			}

		}



		Individual ind = ((SteadyStateEvaluator)evaluator).getNextEvaluatedIndividual();
		if (ind != null)   // do we have an evaluated individual? 
		{
			int subpop = ((SteadyStateEvaluator)evaluator).getSubpopulationOfEvaluatedIndividual(); 

			if ( partiallyFullSubpop ) // is subpopulation full? 
			{  //System.out.println("Index"+alps.index+ " Subpopulation::"+ population.subpops[subpop].individuals.length+
				//          "Pop::"+alps.layers.get(alps.index).evolutionState.population.subpops[subpop].individuals.length);
				population.subpops[subpop].individuals[individualCount[subpop]++]=ind; 

				// STATISTICS FOR GENERATION ZERO 
				if ( individualCount[subpop] == population.subpops[subpop].individuals.length ) 
					if (statistics instanceof SteadyStateStatisticsForm)
						((SteadyStateStatisticsForm)statistics).enteringSteadyStateStatistics(subpop, this); 
			}
			else 
			{ 
				// mark individual for death 
				int deadIndividual = ((SteadyStateBreeder)breeder).deselectors[subpop].produce(subpop,this,0);
				Individual deadInd = population.subpops[subpop].individuals[deadIndividual];

				// maybe replace dead individual with new individual
				if (ind.fitness.betterThan(deadInd.fitness) ||         // it's better, we want it
						random[0].nextDouble() < replacementProbability)      // it's not better but maybe we replace it directly anyway
					population.subpops[subpop].individuals[deadIndividual] = ind;

				// update duplicate hash table 
				individualHash[subpop].remove(deadInd); 

				if (statistics instanceof SteadyStateStatisticsForm) 
					((SteadyStateStatisticsForm)statistics).individualsEvaluatedStatistics(this, 
							new Individual[]{ind}, new Individual[]{deadInd}, new int[]{subpop}, new int[]{deadIndividual}); 
			}
			// INCREMENT NUMBER OF COMPLETED EVALUATIONS
			evaluations++;
			// COMPUTE GENERATION BOUNDARY
			generationBoundary = (evaluations % generationSize == 0);
		}
		else
		{
			generationBoundary = false; 
		}



		// SHOULD WE QUIT?
		if (!partiallyFullSubpop && evaluator.runComplete(this) && quitOnRunComplete)
		{ 
			output.message("Found Ideal Individual"); 
			return R_SUCCESS;
		}

		if ((numEvaluations > UNDEFINED && evaluations >= numEvaluations) ||  // using numEvaluations
				(numEvaluations <= UNDEFINED && generationBoundary && generation == numGenerations -1))  // not using numEvaluations
		{
			// we are not exchanging again, but we might wish to increment the generation
			// one last time if we hit a generation boundary
			if (generationBoundary)
				generation++;
			return R_FAILURE;
		}


		// EXCHANGING
		if (generationBoundary)
		{
			// PRE-BREED EXCHANGE 
			statistics.prePreBreedingExchangeStatistics(this);
			population = exchanger.preBreedingExchangePopulation(this);
			statistics.postPreBreedingExchangeStatistics(this);
			String exchangerWantsToShutdown = exchanger.runComplete(this);
			if (exchangerWantsToShutdown!=null)
			{ 
				output.message(exchangerWantsToShutdown); 
				return R_SUCCESS;
			}

			// POST BREED EXCHANGE
			statistics.prePostBreedingExchangeStatistics(this);
			population = exchanger.postBreedingExchangePopulation(this);
			//statistics.postPostBreedingExchangeStatistics(this);

			// INCREMENT GENERATION AND CHECKPOINT
			generation++;
			if (checkpoint && generation%checkpointModulo == 0) 
			{
				output.message("Checkpointing");
				statistics.preCheckpointStatistics(this);
				Checkpoint.setCheckpoint(this);
				statistics.postCheckpointStatistics(this);
			}
		}
		return R_NOTDONE;
	}


	/**
	 * calculate age of individuals for steady state evolution.
	 * @param state EvolutionState
	 * @param evaluations current evaluation count
	 */
	private void calculateAge(EvolutionState state, long evaluations)
	{
		int popSize = popSizeLayer(state);

		for(int x=0;x<state.population.subpops.length;x++)
			for(Individual ind: state.population.subpops[x].individuals)
				ind.age = 1 + (evaluations - ind.evaluation)/popSize;
	}


	/**
	 * at regular intervals determined by the age-gap parameter, new individuals are assigned the current 
	 * evaluation count
	 * @param state EvolutionState
	 * @param evaluations current evaluation count
	 */
	private void restartIndEvaluationCount(EvolutionState state, long evaluations)
	{
		for(int x=0;x<state.population.subpops.length;x++)
			for(Individual ind: state.population.subpops[x].individuals)
				ind.evaluation = evaluations;
	}


	/**
	 * Calculate population size of all sub-populations in a layer
	 * @param state
	 * @return
	 * @deprecated check similar implementation in Operations.expectedPopulation(...)
	 */
	private int popSizeLayer(EvolutionState state)
	{
		int total = 0;

		for(int x=0;x<state.population.subpops.length;x++)
		{
			/* Count population in each subpop
			 * This is very important and must be called in evolve to keep count of
			 * individuals in a layer -- this will determine if new breeding is needed*/
			if(!Engine.alps.layers.get(Engine.alps.index).getIsBottomLayer())
				individualCount[x] = state.population.subpops[x].individuals.length; 

			total +=state.population.subpops[x].individuals.length;
		}
		return total;
	}
	
	
	/**
	 * Calculate how many individuals are available per sub population after inter layer movements
	 * This is used to determine how many extra individuals are to be added to fill up the full population for
	 * each subpopulation
	 * @param state
	 * @return
	 */
	private void howManyToBreed(EvolutionState state)
	{
		for(int x=0;x<state.population.subpops.length;x++)
			/* Count population in each subpop
			 * This is very important and must be called in evolve to keep count of
			 * individuals in a layer -- this will determine if new breeding is needed*/
			if(!Engine.alps.layers.get(Engine.alps.index).getIsBottomLayer())
				individualCount[x] = state.population.subpops[x].individuals.length; 

	}


}
