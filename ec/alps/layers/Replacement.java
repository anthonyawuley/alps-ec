package ec.alps.layers;

import java.util.ArrayList;

import ec.Individual;
import ec.Initializer;
import ec.Population;
import ec.alps.Engine;
import ec.EvolutionState;
import ec.select.TournamentSelection;
import ec.util.Parameter;

/**
 * 
 * The ALPS strategy requires regular inter–layer migration for individuals that are older than the 
 * allowed age limit for a layer. An individual’s age is generationally compared to the maximum allowed 
 * age of its layer, and if it is older, an attempt is made to move the individual to a higher layer.
 * 
 * @author Anthony Awuley
 *
 */
public abstract class Replacement extends TournamentSelection {

	/** */
	private static final long serialVersionUID = 1L;
	/** */
	protected int worseIndividual;
	/** */
	protected int nearestIndividual;
	/** */
	protected int bestIndividual;
	/** */
	public final static String ALPS_LAYER_REPLACEMENT = "layer-replacement";
	/** */
	public final static String ALPS_TOURNAMENT_SIZE   = "tournament-size";
	/** */
	public final static String REPLACE_WEAKEST        = "replace-weakest";
	/** */
	public static final String POP_SIZE               = "size";

	/**
	 * This parameter is used to determine the type of replacement for alps
	 * layer movement. When moving an over aged individual into the next
	 * available higher layer, perform tournament selection, this parameter if
	 * true, implies the weakest tournament individual is ALWAYS replaced. Else
	 * the we the weakest tournament is ONLY REPLACED IF AND ONLY IF its fitness
	 * is lower than the fitness of the over aged individual.
	 */
	public static boolean replaceWeakest = false;

	/**
	 * setup Replacement strategy
	 */
	public void setup(final EvolutionState state, final Parameter base) 
	{
		if (!state.parameters.exists(Engine.base().push(ALPS_TOURNAMENT_SIZE),
				null))
			state.output
			.fatal("tournament size parameter not defined for \"alps."
					+ ALPS_TOURNAMENT_SIZE + "\" ");
		if (!state.parameters.exists(defaultBase().push(REPLACE_WEAKEST), null))
			state.output.fatal("replace weakest parameter not defined "
					+ "\"alps." + ALPS_LAYER_REPLACEMENT + "."
					+ REPLACE_WEAKEST + "\" \n"
					+ "A default value of false will be assumed");

		replaceWeakest = state.parameters.getBoolean(
				defaultBase().push(REPLACE_WEAKEST), null, false);

	}

	/**
	 * 
	 * @return
	 *   returns the name of a sub-class
	 */
	@Override
	public abstract String toString();

	/**
	 * return default base for class
	 */
	@Override
	public Parameter defaultBase() 
	{
		return Engine.base().push(ALPS_LAYER_REPLACEMENT);
	}

	/**
	 * loop through highest layer to current layer attempt to move individuals
	 * from current layer that have age values within higher layer.
	 * 
	 * @param layers
	 * @param to
	 * @return
	 */
	public abstract void layerMigrations(ALPSLayers alpsLayers,
			Population current);


	/**
	 * when aged individuals are moved to next higher layer, ECJ breeds maximum
	 * individuals of the total number left in the layer. In order to overcome this
	 * such that at anypoint ECJ breeds a total number of population equal to
	 * the size as contained in parameter file, consolidatePopulation(...) loops through
	 * all subpopulation in a layer and fills in the missing slots
	 * 
	 * @author Anthony
	 * 
	 * @param end
	 *            total size of pop for sub population
	 * @param thread
	 *            thread
	 */
	public void  consolidatePopulation(ALPSLayers alps,final int thread)
	{
	
		for(int sub=0;sub<alps.layers.get(alps.index).evolutionState.population.subpops.length;sub++)
		{
			/** total number of populations expected */
			int size = alps.layers.get(alps.index).evolutionState.
					parameters.getInt(new Parameter(Initializer.P_POP).
							push(Population.P_SUBPOP).push(sub+"").push(POP_SIZE),null);

			
			if(alps.index == (alps.layers.size() - 1))
			{
				alps.layers.get(alps.index).evolutionState.population.subpops[sub].individuals =
						fillPopTournament(alps.layers.get(alps.index).evolutionState.population.subpops[sub].individuals.length,
								size,sub,alps.layers.get(alps.index).evolutionState,thread);
			}
			else 
			{   /* consolidate current and next population */
				for(int index=alps.index;index<alps.index+2;index++)
					alps.layers.get(index).evolutionState.population.subpops[sub].individuals =
					fillPopTournament(alps.layers.get(index).evolutionState.population.subpops[sub].individuals.length,
							size,sub,alps.layers.get(index).evolutionState,thread);
			}
		}
	}



	/**
	 * @author anthony
	 * @param arr
	 * @param remIndex
	 */
	public static void removeElt(int[] arr, int remIndex) 
	{
		int numElts = arr.length - (remIndex + 1);
		System.arraycopy(arr, remIndex + 1, arr, remIndex, numElts);
	}

	/**
	 * @author anthony
	 * @param Individuals
	 * @return
	 */
	public ArrayList<Individual> listArrayAdaptor(Individual[] individuals) 
	{
		ArrayList<Individual> list = new ArrayList<>();
		for (Individual i : individuals)
			list.add(i);

		return list;
	}

	/**
	 * @author anthony
	 * @param individuals
	 * @return
	 */
	public Individual[] arrayListAdaptor(ArrayList<Individual> individuals) 
	{
		Individual[] list = new Individual[individuals.size()];
		for (int i = 0; i < individuals.size(); i++)
			list[i] = individuals.get(i);

		return list;
	}

	/**
	 * reverse tournament implementation for ALPS GP : selection of worst
	 * individual
	 * 
	 * @author anthony
	 * @param subpopulation
	 * @param state
	 * @param thread
	 * @return
	 */
	public int worst(final int subpopulation, final EvolutionState state,
			final int thread) 
	{
		// pick size random individuals, then pick the best.
		Individual[] oldinds = state.population.subpops[subpopulation].individuals;
		int best = getRandomIndividual(0, subpopulation, state, thread);
		// int s = getTournamentSizeToUse(state.random[thread]);
		for (int x = 1; x < state.population.subpops[subpopulation].individuals.length; x++) {
			int j = getRandomIndividual(x, subpopulation, state, thread);
			if (!betterThan(oldinds[j], oldinds[best], subpopulation, state,thread)) // j is at least as bad as best
				best = j;
		}

		return best;
	}

	/**
	 * when aged individuals are moved to next higher layer, ECJ breeds maximum
	 * individuals of the total left in the layer. In order to overcome this
	 * such that at anypoint ECJ breeds a total number of population to the size
	 * as contained in parameter file, fillPopRandom(...) randomly selects
	 * existing individuals in the same layer to fill in the number that was
	 * moved up the layer. This selection process is completely random and its
	 * only to ensure that the maximum capacity is available to enable maximum
	 * breed of individuals in a population
	 * 
	 * @author anthony
	 * 
	 * @param start
	 *            begin random copy of existing individuals
	 * @param end
	 *            total size of pop for sub population
	 * @param sub
	 *            supbpopulation
	 * @param state
	 *            EvolutionState
	 * @param thread
	 *            thread
	 * @return population with filled in individuals to size of end
	 */
	public Individual[] fillPopRandom(final int start, final int end,
			final int sub, final EvolutionState state, final int thread) 
	{
		Individual[] inds = new Individual[end - start];
		Individual[] newInds = new Individual[end];
		int count = 0;

		if (start == 0) // this condition implies the current population is empty.
			return state.population.subpops[sub].individuals;

		/*
		 * randomly select individuals from existing population to populate the
		 * "shot off" number selects random individual from population
		 */
		for (int x = 0; x < (end - start); x++)
			inds[x] = (Individual) state.population.subpops[sub].individuals[getRandomIndividual(
					x + 1, sub, state, thread)].clone(); // +1 just to avoid IllegalArgumentException: n must be positive
		/* 
		 * newInds[0...currentPop] = currentPop individuals
		 * newInds[currentPop...end] == inds
		 */
		for (int i = 0; i < end; i++)
			if (i < start)
				newInds[i] = state.population.subpops[sub].individuals[i];
			else
				newInds[i] = inds[count++];

		return newInds;
	}

	/**
	 * when aged individuals are moved to next higher layer, ECJ breeds maximum
	 * individuals of the total left in the layer. In order to overcome this
	 * such that at anypoint ECJ breeds a total number of population equal to
	 * the size as contained in parameter file, fillPopTournament(...) randomly
	 * selects n individuals in the same layer and picks the best to fill in the
	 * number that was moved up the layer.
	 * 
	 * @author Anthony
	 * 
	 * @param end
	 *            total size of pop for sub population
	 * @param sub
	 *            supbpopulation
	 * @param thread
	 *            thread
	 * @return population with filled in individuals to size of end
	 */
	public Individual[] fillPopTournament(final int start, final int end,
			final int sub, final EvolutionState state, final int thread) 
	{
		Individual[] inds = new Individual[end - start];
		Individual[] newInds = new Individual[end];
		int count = 0;

		/* this condition implies the current population is empty. */
		if (start == 0) 
			return state.population.subpops[sub].individuals;

		/*
		 * performs tournament selection and picks best individual to fill an
		 * empty slot
		 */
		for (int x = 0; x < (end - start); x++)
			inds[x] = (Individual) state.population.subpops[sub].
			individuals[bestTournament(sub, state, thread)].clone();

		/*
		 * newInds[0...currentPop] = currentPop individuals
		 * newInds[currentPop...end] == inds
		 */
		for (int i = 0; i < end; i++)
			if (i < start)
				newInds[i] = state.population.subpops[sub].individuals[i];
			else
				newInds[i] = inds[count++];

		return newInds;
	}

	/**
	 * reverse tournament implementation for ALPS GP : perform tournament
	 * selection and peak the worst
	 * 
	 * @author anthony
	 * @param subpopulation
	 * @param state
	 * @param thread
	 * @return
	 */
	public int reverseTournament(final int subpopulation,
			final EvolutionState state, final int thread) 
	{
		// pick size random individuals, then pick the best.
		Individual[] oldinds = state.population.subpops[subpopulation].individuals;
		int worst = getRandomIndividual(0, subpopulation, state, thread);

		// int s = getTournamentSizeToUse(state.random[thread]); //was not picking system set tournament selection
		int s = state.parameters.getInt(Engine.base().push(ALPS_TOURNAMENT_SIZE),null);

		for (int x = 1; x < s; x++) {
			int j = getRandomIndividual(x, subpopulation, state, thread);
			if (!betterThan(oldinds[j], oldinds[worst], subpopulation, state,thread)) // j is at least as bad as best
				worst = j;
		}

		return worst;
	}

	/**
	 * reverse tournament implementation for ALPS GP : perform tournament
	 * selection and peak the worst
	 * 
	 * @author anthony
	 * @param subpopulation
	 * @param state
	 * @param thread
	 * @return
	 */
	public int bestTournament(final int subpopulation,
			final EvolutionState state, final int thread) 
	{
		// pick size random individuals, then pick the best.
		Individual[] oldinds = state.population.subpops[subpopulation].individuals;
		int best = getRandomIndividual(0, subpopulation, state, thread);

		// int s = getTournamentSizeToUse(state.random[thread]); //was not picking system set tournament selection
		int s = state.parameters.getInt(Engine.base().push(ALPS_TOURNAMENT_SIZE),null);

		for (int x = 1; x < s; x++) {
			int j = getRandomIndividual(x, subpopulation, state, thread);
			if (betterThan(oldinds[j], oldinds[best], subpopulation, state,
					thread)) // j is at least as bad as best
				best = j;
		}

		return best;
	}

	/**
	 * perform tournament selection and select individual with nearest fitness
	 * to newIndividual
	 * 
	 * @param subpopulation
	 * @param state
	 * @param thread
	 * @param newIndividual
	 * @return the nearest individual in population
	 */
	protected int nearestTournamentIndividualFitness(final int subpopulation,
			final EvolutionState state, final int thread,
			final Individual newIndividual) 
	{
		int nearest = 0;
		Individual[] oldinds = state.population.subpops[subpopulation].individuals;
		double fitness = newIndividual.fitness.fitness();

		int s = state.parameters.getInt(Engine.base().push(ALPS_TOURNAMENT_SIZE),
				null);

		for (int x = 1; x < s; x++) {
			int j = getRandomIndividual(x, subpopulation, state, thread);
			if (Math.abs(oldinds[nearest].fitness.fitness() - fitness) > Math
					.abs(oldinds[j].fitness.fitness() - fitness))
				nearest = j;
		}
		// System.out.println("\nThis is the nearest "+pop.get(tournament.get(nearest)).getAge()+" \n");
		return nearest;
	}

	/**
	 * perform tournament selection and select individual with nearst fitness to
	 * individuals in the population
	 * 
	 * @param subpopulation
	 * @param state
	 * @param thread
	 * @param newIndividual
	 * @return
	 */
	public int nearestPopulationIndividualFitness(final int subpopulation,
			final EvolutionState state, final int thread,
			final Individual newIndividual) 
	{
		int nearest = 0;
		Individual[] oldinds = state.population.subpops[subpopulation].individuals;
		double fitness = newIndividual.fitness.fitness();

		// int s = getTournamentSizeToUse(state.random[thread]);
		for (int x = 1; x < state.population.subpops[subpopulation].individuals.length; x++) {
			int j = getRandomIndividual(x, subpopulation, state, thread);
			if (Math.abs(oldinds[nearest].fitness.fitness() - fitness) > Math
					.abs(oldinds[j].fitness.fitness() - fitness))
				nearest = j;
		}

		return nearest;
	}
}
