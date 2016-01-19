package ec.alps;

import java.util.LinkedHashMap;
import java.util.Map;

import ec.Breeder;
import ec.Evaluator;
import ec.EvolutionState;
import ec.Exchanger;
import ec.Finisher;
import ec.Initializer;
import ec.Prototype;
import ec.Statistics;
import ec.alps.layers.ALPSLayers;
import ec.alps.layers.Replacement;
import ec.alps.util.Operations;
import ec.util.Parameter;

public class EState extends EvolutionState {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** */
	public Replacement replacement;
	/** */
	public final static String P_REPLACEMENT = "layer-replacement";

	/** */
	public Map<String, Double> nodeCountTerminalSet = new LinkedHashMap<String, Double>();
	/** */
	public Map<String, Double> nodeCountFunctionSet = new LinkedHashMap<String, Double>();

	final static String P_CHECKPOINTPREFIX_OLD = "prefix";

	/** number of chunks available when using k-fold cross validation */
	public int kFoldCrossValidationSize = 1;
	/** */
	public static final String K_FOLD_CROSS_VALIDATION_CHUNCK = "cross-validation-size";
	/** */
	public boolean isKFoldCrossValidation = false;

	/**
	 * Unlike for other setup() methods, ignore the base; it will always be
	 * null.
	 * 
	 * @see Prototype#setup(EvolutionState,Parameter)
	 */
	public void setup(final EvolutionState state, final Parameter base) {
		Parameter p;

		// we ignore the base, it's worthless anyway for EvolutionState

		p = new Parameter(P_CHECKPOINT);
		checkpoint = parameters.getBoolean(p, null, false);

		p = new Parameter(P_CHECKPOINTPREFIX);
		checkpointPrefix = parameters.getString(p, null);
		if (checkpointPrefix == null) {
			// check for the old-style checkpoint prefix parameter
			Parameter p2 = new Parameter(P_CHECKPOINTPREFIX_OLD);
			checkpointPrefix = parameters.getString(p2, null);
			if (checkpointPrefix == null) { // indicate the new style,not old parameter
				output.fatal("No checkpoint prefix specified.", p); 
			} else {
				output.warning(
						"The parameter \"prefix\" is deprecated.  Please use \"checkpoint-prefix\".",
						p2);
			}
		} else {
			// check for the old-style checkpoint prefix parameter as an
			// acciental duplicate
			Parameter p2 = new Parameter(P_CHECKPOINTPREFIX_OLD);
			if (parameters.getString(p2, null) != null) {
				output.warning(
						"You have BOTH the deprecated parameter \"prefix\" and its replacement \"checkpoint-prefix\" defined.  The replacement will be used,  Please remove the \"prefix\" parameter.",
						p2);
			}

		}

		p = new Parameter(P_CHECKPOINTMODULO);
		checkpointModulo = parameters.getInt(p, null, 1);
		if (checkpointModulo == 0)
			output.fatal("The checkpoint modulo must be an integer >0.", p);

		p = new Parameter(P_CHECKPOINTDIRECTORY);
		if (parameters.exists(p, null)) {
			checkpointDirectory = parameters.getFile(p, null);
			if (checkpointDirectory == null)
				output.fatal("The checkpoint directory name is invalid: "
						+ checkpointDirectory, p);
			if (!checkpointDirectory.isDirectory())
				output.fatal(
						"The checkpoint directory location is not a directory: "
								+ checkpointDirectory, p);
		} else
			checkpointDirectory = null;

		// load evaluations, or generations, or both

		p = new Parameter(P_EVALUATIONS);
		if (parameters.exists(p, null)) {
			numEvaluations = parameters.getInt(p, null, 1); // 0 would be
															// UNDEFINED
			if (numEvaluations <= 0)
				output.fatal(
						"If defined, the number of evaluations must be an integer >= 1",
						p, null);
		}

		p = new Parameter(P_GENERATIONS);
		if (parameters.exists(p, null)) {
			numGenerations = parameters.getInt(p, null, 1); // 0 would be
															// UDEFINED

			if (numGenerations <= 0)
				output.fatal(
						"If defined, the number of generations must be an integer >= 1.",
						p, null);

			if (numEvaluations != UNDEFINED) // both defined
			{
				state.output
						.warning("Both generations and evaluations defined: generations will be ignored and computed from the evaluations.");
				numGenerations = UNDEFINED;
			}
		} else if (numEvaluations == UNDEFINED) // uh oh, something must be
												// defined
			output.fatal("Either evaluations or generations must be defined.",
					new Parameter(P_GENERATIONS), new Parameter(P_EVALUATIONS));

		p = new Parameter(P_QUITONRUNCOMPLETE);
		quitOnRunComplete = parameters.getBoolean(p, null, false);

		/*
		 * @anthony
		 */
		p = new Parameter(K_FOLD_CROSS_VALIDATION_CHUNCK);
		if (parameters.exists(p, null)) {
			/** number of chunks available when using k-fold cross validation */
			kFoldCrossValidationSize = parameters.getInt(p, null, 1);
			int numJobs = parameters.getIntWithDefault(new Parameter("jobs"),
					null, 1);

			if (kFoldCrossValidationSize > numJobs)
				output.fatal(K_FOLD_CROSS_VALIDATION_CHUNCK + " "
						+ "cannot be greater than the number of jobs", p, null);
			if (kFoldCrossValidationSize < 2)
				output.fatal("T" + K_FOLD_CROSS_VALIDATION_CHUNCK + " "
						+ "cannot be less 2", p, null);
			if (kFoldCrossValidationSize < numJobs)
				output.warning(
						""
								+ K_FOLD_CROSS_VALIDATION_CHUNCK
								+ " is less than number of jobs \n"
								+ " when a cross validation cycle is exhausted, a new one is started untill all runs are exhausted \n"
								+ "its best when number of runs is a multiple of k-fold-cross-validation size ",
						p, null);
			// deactivate cross validation if the size is less than to 2
			isKFoldCrossValidation = (kFoldCrossValidationSize >= 2) ? true
					: false;
		}

		/* Set up the singletons */
		p = new Parameter(P_INITIALIZER);
		initializer = (Initializer) (parameters.getInstanceForParameter(p,
				null, Initializer.class));
		initializer.setup(this, p);

		p = new Parameter(P_FINISHER);
		finisher = (Finisher) (parameters.getInstanceForParameter(p, null,
				Finisher.class));
		finisher.setup(this, p);

		p = new Parameter(P_BREEDER);
		breeder = (Breeder) (parameters.getInstanceForParameter(p, null,
				Breeder.class));
		breeder.setup(this, p);

		p = new Parameter(P_EVALUATOR);
		evaluator = (Evaluator) (parameters.getInstanceForParameter(p, null,
				Evaluator.class));
		evaluator.setup(this, p);

		p = new Parameter(P_STATISTICS);
		statistics = (Statistics) (parameters.getInstanceForParameterEq(p,
				null, Statistics.class));
		statistics.setup(this, p);

		p = new Parameter(P_EXCHANGER);
		exchanger = (Exchanger) (parameters.getInstanceForParameter(p, null,
				Exchanger.class));
		exchanger.setup(this, p);

		generation = 0;

		/*
		 * replacement strategy p = Engine.base().push(P_REPLACEMENT);
		 * replacement = (Replacement)
		 * (parameters.getInstanceForParameter(p,null,Replacement.class));
		 * replacement.setup(this,p);
		 */
	}


	/**
	 * Starts the run. <i>condition</i> indicates whether or not the run was
	 * restarted from a checkpoint (C_STARTED_FRESH vs
	 * C_STARTED_FROM_CHECKPOINT). At the point that run(...) has been called,
	 * the parameter database has already been set up, as have the random number
	 * generators, the number of threads, and the Output facility. This method
	 * should call this.setup(...) to set up the EvolutionState object if
	 * condition equals C_STARTED_FRESH.
	 * 
	 * This was overloaded to run ALPS GP
	 * 
	 * @author anthony
	 */
	public void run(ALPSLayers alps, int condition) {
		/* all running instances of Evolution state have access to ALPS Layers */
		// this.alps = alpsLayers;
		// int result = R_NOTDONE;

		/*
		 * NOTE: Node count is performed for every generation in which there are
		 * individuals in the last layer use largest layer node terminal count
		 * This is performed only when Engine.use_only_default_node_pr is FALSE
		 * and the generational frequency count flag for the last layer is TRUE
		 * 
		 * NB: A layer can be active when no statistics data [NodeStatistics has
		 * not been called] has been generated for the first time in such cases,
		 * the frequency data is the same as what is already been used.
		 */
		if (!Engine.fsalps_use_only_default_node_pr
				&& Engine.fsalps_gen_freq_count
				&& alps.layers.get(alps.layers.size() - 1).getIsActive()
				&& Engine.fsalps_active
		 /* (Operations.popSize(alps.layers.get(alps.layers.size()-1).evolutionState ) == Engine.generationSize)*/){
			Engine.roulette.calculateNodeProbabilities(alps,alps.layers.get(alps.layers.size() - 1).evolutionState);
	      }
		// Engine.roulette = new  Roulette(alps,alps.layers.get(alps.layers.size()-1).evolutionState);

		if (alps.layers.get(alps.index).getIsBottomLayer()
				&& alps.layers.get(alps.index).initializerFlag) {
			/*
			 * NOTE: Node count is performed during initialization of new
			 * individuals in layer 0 This condition is performed only if
			 * Engine.use_only_default_node_pr is Boolean.FALSE and
			 * Engine.fsalps_last_layer_gen_freq_count is Boolean.FALSE :: This
			 * last check avoids multiple count process in the even that that
			 * flag is Boolean.TRUE.
			 * 
			 * NB: A layer can be active when no statistics data [NodeStatistics
			 * has not been called] has been generated for the first time in
			 * such cases, the frequency data is the same as what is already
			 * been used.
			 */
			if (!Engine.fsalps_use_only_default_node_pr
					&& !Engine.fsalps_gen_freq_count
					&& alps.layers.get(alps.layers.size() - 1).getIsActive()
					&& Engine.fsalps_active
			/* (Operations.popSize(alps.layers.get(alps.layers.size()-1).evolutionState) == Engine.generationSize)*/)
			{
				Engine.roulette.calculateNodeProbabilities(alps,
						alps.layers.get(alps.layers.size() - 1).evolutionState);
		    }
			// Engine.roulette = new Roulette( alps,alps.layers.get(alps.layers.size()-1).evolutionState);

			if ((condition == C_STARTED_FRESH)) {
				startFresh();
				alps.layers.get(alps.index).result = evolve();
				alps.layers.get(alps.index).initializerFlag = false;
			} else // condition == C_STARTED_FROM_CHECKPOINT
			{
				startFromCheckpoint();
			}
		} else {
			alps.layers.get(alps.index).result = evolve();
		}

		/*
		 * Attempt inter-layer individual migration replacement =
		 * (ALPSReplacement) (parameters.getInstanceForParameter(new
		 * Parameter(P_REPLACEMENT),null,ALPSReplacement.class));
		 */
		replacement.layerMigrations(alps,
				alps.layers.get(alps.index).evolutionState.population);

		/* count only when evolve is performed */
		alps.layers.get(alps.index).layerGenerationalCount++;
		/* count evaluations in a layer */
		alps.layers.get(alps.index).layerEvaluationCount += Operations
				.activePopulaton(alps.layers.get(alps.index).evolutionState);
		/* count evaluations */
		Engine.globalEvaluations += Operations.activePopulaton(alps.layers
				.get(alps.index).evolutionState);

		/* perform describe(...) for last layer */
		if ((alps.layers.get(alps.layers.size() - 1).result != R_NOTDONE)
				&& (alps.index == (alps.layers.size() - 1)))
			finish(alps.layers.get(alps.layers.size() - 1).result);

		/* DUMPING INDIVIDUAL AGE TO CONSOLE */
		// alps.printAge();
		// alps.printPopSize();
	}

}
