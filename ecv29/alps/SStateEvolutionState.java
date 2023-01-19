
package ec.alps;

import java.util.HashMap;


import ec.EvolutionState;
import ec.Individual;
import ec.alps.layers.ALPSLayers;
import ec.alps.layers.Layer;
import ec.alps.layers.Replacement;
import ec.alps.util.Operations;
import ec.steadystate.SteadyStateBreeder;
import ec.steadystate.SteadyStateEvaluator;
import ec.steadystate.SteadyStateEvolutionState;
import ec.steadystate.SteadyStateStatisticsForm;
import ec.util.Checkpoint;
import ec.util.Parameter;

/**
 * SteadyState Evolution State for ALPS. extends SteadyStateEvolutionState from ECJ
 * 
 * @author Anthony Awuley and Sean Luke
 */

public class SStateEvolutionState extends SteadyStateEvolutionState{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1;

	private static final String P_REPLACEMENT = "layer-replacement";

	public Replacement replacement;





	/**
	 * used at initialization
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

		individualHash = new HashMap[population.subpops.size()];
		for(int i=0;i<population.subpops.size(); i++) individualHash[i] = new HashMap();

		individualCount = new int[population.subpops.size()];
		for (int sub=0; sub < population.subpops.size(); sub++)  
		{ 
			individualCount[sub]=0;
			generationSize += population.subpops.get(sub).initialSize;
			//generationSize += population.subpops.get(sub).individuals.size();  // so our sum total 'generationSize' will be the initial total number of individuals
		}

		if (numEvaluations > UNDEFINED && numEvaluations < generationSize)
			output.fatal("Number of evaluations desired is smaller than the initial population of individuals");


		/** Modify numGenerations and numEvaluations for ALPS  */
		Engine.generationSize = generationSize;

		/**
		 * EMPTY INITIAL POPULATION TO LEAVE SINGLE INDIVIDUAL - This is to prevent NullPointer Exception caused during initial inter-layer migrations
		 **/

		population.subpops.get(0).individuals = 
				Operations.emptyPop(population.subpops.get(0).individuals);


		// INITIALIZE CONTACTS -- done after initialization to allow
		// a hook for the user to do things in Initializer before
		// an attempt is made to connect to island models etc.
		exchanger.initializeContacts(this);
		evaluator.initializeContacts(this);
	}


	/**
	 * After initialization
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

		individualHash = new HashMap[population.subpops.size()];
		for(int i=0;i<population.subpops.size(); i++) individualHash[i] = new HashMap();

		individualCount = new int[population.subpops.size()];
		for (int sub=0; sub < population.subpops.size(); sub++)  
		{ 
			individualCount[sub]=0;
			generationSize += population.subpops.get(sub).initialSize;  // so our sum total 'generationSize' will be the initial total number of individuals
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
			output.message(
					  "L"+ Engine.alps.index + " "
					+ "Generation " + generation+ "\t"
					+ "Evaluations " + evaluations + "\t"
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

		whichSubpop = (whichSubpop+1)%population.subpops.size();  // round robin selection
		

		// is the current subpop full? 
		//boolean partiallyFullSubpop = (individualCount[whichSubpop] < population.subpops.get(whichSubpop).initialSize);  
		// ECJ 27
		boolean partiallyFullSubpop = (population.subpops.get(whichSubpop).individuals.size() < population.subpops.get(whichSubpop).initialSize);
	
		// MAIN EVOLVE LOOP 
		if (((SteadyStateEvaluator) evaluator).canEvaluate())   // are we ready to evaluate? 
		{

			
				Individual ind=null; 
				int numDuplicateRetries = population.subpops.get(whichSubpop).numDuplicateRetries; 

				for (int tries=0; tries <= numDuplicateRetries; tries++)  // see Subpopulation
				{ 
					if ( partiallyFullSubpop )   // is population full?
					{   //System.out.println("LAYERSSSSSS "+this.alps.index + " "+ this.alps.layers.get(this.alps.index).evolutionState.population.subpops[0].individuals.length + " tries"+tries);
						ind = population.subpops.get(whichSubpop).species.newIndividual(this, 0);  // unthreaded 
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



		Individual ind = ((SteadyStateEvaluator)evaluator).getNextEvaluatedIndividual(this);
		/** ECJ 27 */
		int whichIndIndex = -1;
		int whichSubpop = -1;
		if (ind != null)   // do we have an evaluated individual? 
		{

			// ECJ 27
			// COMPUTE GENERATION BOUNDARY
			//generationBoundary = (evaluations % generationSize == 0);

			if (generationBoundary) {
				statistics.preEvaluationStatistics(this);
			}

			int subpop = ((SteadyStateEvaluator)evaluator).getSubpopulationOfEvaluatedIndividual(); 
			whichSubpop = subpop;

			if ( partiallyFullSubpop ) // is subpopulation full? 
			{  //System.out.println("Index"+alps.index+ " Subpopulation::"+ population.subpops.get(subpop).individuals.length+
				//          "Pop::"+alps.layers.get(alps.index).evolutionState.population.subpops.get(subpop).individuals.length);
				//population.subpops.get(subpop).individuals.set(individualCount[subpop]++,ind); 

				// ECJ 27
				population.subpops.get(subpop).individuals.add(ind);

				// STATISTICS FOR GENERATION ZERO 
				if (population.subpops.get(subpop).individuals.size() == population.subpops.get(subpop).initialSize /**individualCount[subpop] == population.subpops.get(subpop).initialSize */) 
					if (statistics instanceof SteadyStateStatisticsForm)
						((SteadyStateStatisticsForm)statistics).enteringSteadyStateStatistics(subpop, this); 
			}
			else 
			{ 
				// mark individual for death 
				int deadIndividual = ((SteadyStateBreeder)breeder).deselectors[subpop].produce(subpop,this,0);
				Individual deadInd = population.subpops.get(subpop).individuals.get(deadIndividual);

				// maybe replace dead individual with new individual
				if (ind.fitness.betterThan(deadInd.fitness) ||         // it's better, we want it
						random[0].nextDouble() < replacementProbability)      // it's not better but maybe we replace it directly anyway
					{
						population.subpops.get(subpop).individuals.set(deadIndividual, ind);
						// ECJ 27
						whichIndIndex = deadIndividual;
					}
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

			/** ECJ 27 */
			if (generationBoundary)
                {
                statistics.postEvaluationStatistics(this);
                }
		}
		else
		{
			generationBoundary = false; 
		}



		// SHOULD WE QUIT?
		if (!partiallyFullSubpop && 
			ind != null && 
			((SteadyStateEvaluator)evaluator).isIdealFitness(this, ind) && 
			/*evaluator.runComplete(this) != null */ quitOnRunComplete)
		{ 
			//output.message("Found Ideal Individual"); 
			output.message("Individual " + whichIndIndex + " of subpopulation " + whichSubpop + " has an ideal fitness."); 
            finishEvaluationStatistics();
			return R_SUCCESS;
		}

		/** ECJ 27 */
		if (((SteadyStateEvaluator)evaluator).runComplete != null)
            {
            output.message(evaluator.runComplete);
            finishEvaluationStatistics();
            return R_SUCCESS; 
            }
        
        if ((generationBoundary && numGenerations != UNDEFINED && generation >= numGenerations - 1) ||
            (numEvaluations != UNDEFINED && evaluations >= numEvaluations))
            {
            finishEvaluationStatistics();
            return R_FAILURE;
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

			// INCREMENT GENERATION AND CHECKPOINT
			generation++;

			// PRE-BREED EXCHANGE 
			statistics.prePreBreedingExchangeStatistics(this);
			population = exchanger.preBreedingExchangePopulation(this);
			statistics.postPreBreedingExchangeStatistics(this);
			String exchangerWantsToShutdown = exchanger.runComplete(this);
			if (exchangerWantsToShutdown!=null)
			{ 
				output.message(exchangerWantsToShutdown); 
				finishEvaluationStatistics();
				return R_SUCCESS;
			}

			// POST BREED EXCHANGE
			statistics.prePostBreedingExchangeStatistics(this);
			population = exchanger.postBreedingExchangePopulation(this);
			statistics.postPostBreedingExchangeStatistics(this);

			// // INCREMENT GENERATION AND CHECKPOINT
			// generation++;



			//CLEAR POPULATION
			// population = population.emptyClone();


		}


			if (checkpoint && generationBoundary && (generation-1)%checkpointModulo == 0) 
			{
				output.message("Checkpointing");
				statistics.preCheckpointStatistics(this);
				Checkpoint.setCheckpoint(this);
				statistics.postCheckpointStatistics(this);
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

		for(int x=0;x<state.population.subpops.size();x++)
			for(Individual ind: state.population.subpops.get(x).individuals)
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
		for(int x=0;x<state.population.subpops.size();x++)
			for(Individual ind: state.population.subpops.get(x).individuals)
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

		for(int x=0;x<state.population.subpops.size();x++)
		{
			/* Count population in each subpop
			 * This is very important and must be called in evolve to keep count of
			 * individuals in a layer -- this will determine if new breeding is needed*/
			if(!Engine.alps.layers.get(Engine.alps.index).getIsBottomLayer())
				individualCount[x] = state.population.subpops.get(x).individuals.size(); 

			total +=state.population.subpops.get(x).individuals.size();
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
		for(int x=0;x<state.population.subpops.size();x++)
			/* Count population in each subpop
			 * This is very important and must be called in evolve to keep count of
			 * individuals in a layer -- this will determine if new breeding is needed*/
			if(!Engine.alps.layers.get(Engine.alps.index).getIsBottomLayer())
				individualCount[x] = state.population.subpops.get(x).individuals.size(); 

	}


}
