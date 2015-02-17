/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */



package ec.app.fsalps.ionosphere;
import ec.simple.SimpleProblemForm;
import ec.app.fsalps.ionosphere.DoubleData;
import ec.app.utils.datahouse.DataCruncher;
import ec.util.*;
import ec.*;
import ec.app.utils.datahouse.Out;
import ec.app.utils.datahouse.Reader;
import ec.gp.*;
import ec.gp.koza.*;

import java.util.ArrayList;

public class Ionosphere extends GPProblem implements SimpleProblemForm
{
	/**  */
	private static final long serialVersionUID = 1;

	/** 
	 * 
	 * This radar data was collected by a system in Goose Bay, Labrador. This system consists of a 
	 * phased array of 16 high-frequency antennas with a total transmitted power on the order of 
	 * 6.4 kilowatts. See the paper for more details. The targets were free electrons in the ionosphere. 
	 * "Good" radar returns are those showing evidence of some type of structure in the ionosphere. 
	 * "Bad" returns are those that do not; their signals pass through the ionosphere. 
	 * 
	 * Received signals were processed using an autocorrelation function whose arguments are the 
	 * time of a pulse and the pulse number. There were 17 pulse numbers for the Goose Bay system. 
	 * Instances in this databse are described by 2 attributes per pulse number, corresponding to 
	 * the complex values returned by the function resulting from the complex electromagnetic signal.
	 * <br><br>
	 * -- All 34 are continuous 
	 * -- The 35th attribute is either "good" or "bad" according to the definition summarized above. 
	 *    This is a binary classification task. 
	 */

	public float   ion0,ion1,ion2,ion3,ion4,ion5,ion6,ion7,ion8,ion9,ion10,ion11,ion12,ion13,ion14,ion15,
	ion16,ion17,ion18,ion19,ion20,ion21,ion22,ion23,ion24,ion25,ion26,ion27,ion28,ion29,ion30,ion31,ion32,
	ion33,ion34;

	/** directory to raw unprocessed file */
	private final String DATA_RAW   = "ionosphere-all";
	/** directory to savel cleaned file */
	private final String DATA_CLEAN = "ionosphere-clean";
	/** directory to save training file */
	private final String TRAIN_DATA = "ionosphere-train-data";
	/** directory to save test file */
	private final String TEST_DATA  = "ionosphere-test-data";
	/** @deprecated */
	private final String NUMB_DATA_POINTS = "total-number-of-points";
	/**
	 * shuffle data when a cycle of kfold cross validation is completed
	 * when not shuffled, this will be like testing the same data sample
	 * n times. where n = jobs/cross-validation-size
	 */
	private final String KFOLD_CYCLE_SHUFFLE = "kfold-cycle-data-shuffle";

	private       String trainFile, testFile;
	public  static int ctcmtrx = 0;
	private ArrayList<ArrayList> POPULATION_DATA;
	private ArrayList<ArrayList> TRAINING_DATA;
	private ArrayList<ArrayList> TESTING_DATA;



	public DoubleData input;

	public Object clone()
	{
		Ionosphere newobj = (Ionosphere) (super.clone());
		newobj.input = (DoubleData)(input.clone());
		return newobj;
	}

	/** setup is called once per run */
	public void setup(final EvolutionState state,
			final Parameter base)
	{
		// very important, remember this
		super.setup(state,base);

		// set up our input -- don't want to use the default base, it's unsafe here
		input = (DoubleData) state.parameters.getInstanceForParameterEq(
				base.push(P_DATA), null, DoubleData.class);
		input.setup(state,base.push(P_DATA));

		String dataRaw = state.parameters.getString(base.push(DATA_RAW), null);
		String dataClean = state.parameters.getString(base.push(DATA_CLEAN), null);
		//state.parameters.getInt(base.push(NUMB_DATA_POINTS), null);
		trainFile = state.parameters.getString(base.push(TRAIN_DATA), null);
		testFile = state.parameters.getString(base.push(TEST_DATA), null);

		boolean kFoldCycleDataShuffle = state.parameters.
				getBoolean(base.push(KFOLD_CYCLE_SHUFFLE),null,false);

		//String [] regex =  {"^[0]?[\\.]?[0]{0,},.*","^.*?[,]+[0]+\\.?[0]*\\,.*"};
		String [] regex =  {"",""};

		/** 
		 * 1. Reads raw data
		 * 2. Filters content using regex above and passes output to dataClean
		 * 3. Shuffles content from (2) above
		 * 4. Split content into two [trainFile and testFile]
		 * 5. Perform shuffle on either files based on experiment (training or testing)
		 * NB:Files don't change once they have been created, cleaned and separated.
		 *    This is done by checking existence of files at program call. however,
		 *    Files are always shuffled at start of program
		 */
		if(state.isKFoldCrossValidation)
		{ 
			if(!DataCruncher.LOCK_DOWN_SHUFFLE) 
			{  
				/* it is assumed that data is already shuffled, this is to ensure that the same 
				 * data block is always selected for training and testing for all strategies
				 * i.e. canonical, alps and fsalps 
				 * */
				POPULATION_DATA = DataCruncher.shuffleData(state,
						Reader.readFile(DataCruncher.cleanFile(regex,dataRaw,dataClean+".clean"),","),true);
				/* write if file does not exist */
				Out.writeDataToFile(POPULATION_DATA,dataClean,true); 
				/* read from file */
				POPULATION_DATA = Reader.readFile(DataCruncher.cleanFile(regex,dataRaw,dataClean),"\\s"); 
			}
			/*
			 * when one k-fold cross validation cycle is exhausted, force data shuffle and force
			 * write operation to dataClean. This begins a new k-fold cross validation process
			 * with a newly shuffled data set
			 */
			else if(((int)state.job[0]%state.kFoldCrossValidationSize)==0 && 
					((int)state.job[0]>0) && kFoldCycleDataShuffle)
			{   /* read cleaned data */
				POPULATION_DATA = Reader.readFile(
						DataCruncher.cleanFile(regex,dataRaw,dataClean),"\\s"); 
				/* turn off shuffle lock  and FORCE shuffle */
				DataCruncher.LOCK_DOWN_SHUFFLE = false; 
				POPULATION_DATA = DataCruncher.shuffleData(state,POPULATION_DATA,true);
				/* forced rewrite of cleaned data) */
				Out.writeDataToFile(POPULATION_DATA,dataClean,true); 

			}
			else 
			{   /* this is necessary to prevent NullPinterException when using ALPS */
				POPULATION_DATA = Reader.readFile(DataCruncher.cleanFile(regex,dataRaw,dataClean),"\\s"); 
			}
			/* fetch training chunk */
			TRAINING_DATA = DataCruncher.selectTrainingChunk(POPULATION_DATA,
					state.kFoldCrossValidationSize, 
					(int) state.job[0] % state.kFoldCrossValidationSize);
			/* fetch testing chunk */
			TESTING_DATA = DataCruncher.selectTestingChunk(POPULATION_DATA,
					state.kFoldCrossValidationSize, 
					(int) state.job[0] % state.kFoldCrossValidationSize);

			/* shuffle training and testing data */
			TRAINING_DATA = DataCruncher.shuffleData (state,TRAINING_DATA,true);
			TESTING_DATA  = DataCruncher.shuffleData (state,TESTING_DATA,true);
		}
		else 
		{   
			/* when splitting data into test and train */
			POPULATION_DATA = DataCruncher.shuffleData(state,Reader.readFile(
					DataCruncher.cleanFile(regex,dataRaw,dataClean),"\\s"),false);
			/* Split formated data and write to training and test file */
			Out.writeDataToFile(POPULATION_DATA,trainFile,testFile);

			/* shuffle training and testing data */
			TRAINING_DATA = DataCruncher.shuffleData(state,Reader.readFile(trainFile,","),true);
			TESTING_DATA  = DataCruncher.shuffleData(state,Reader.readFile(testFile,","),true);
		}

	}



	public void evaluate(final EvolutionState state, 
			final Individual ind, 
			final int subpopulation,
			final int threadnum)
	{

		if (!ind.evaluated)  // don't bother reevaluating
		{
			int hits = 0;
			float expectedResult;


			for(int i=0;i<TRAINING_DATA.size();i++){
				//decoded Data Point
				ion0	= (float) TRAINING_DATA.get(i).get(0);
				ion1	= (float) TRAINING_DATA.get(i).get(1);
				ion2	= (float) TRAINING_DATA.get(i).get(2);
				ion3	= (float) TRAINING_DATA.get(i).get(3);
				ion4	= (float) TRAINING_DATA.get(i).get(4);
				ion5	= (float) TRAINING_DATA.get(i).get(5);
				ion6	= (float) TRAINING_DATA.get(i).get(6);
				ion7	= (float) TRAINING_DATA.get(i).get(7);
				ion8	= (float) TRAINING_DATA.get(i).get(8);
				ion9	= (float) TRAINING_DATA.get(i).get(9);
				ion10	= (float) TRAINING_DATA.get(i).get(10);
				ion11	= (float) TRAINING_DATA.get(i).get(11);
				ion12	= (float) TRAINING_DATA.get(i).get(12);
				ion13	= (float) TRAINING_DATA.get(i).get(13);
				ion14	= (float) TRAINING_DATA.get(i).get(14);
				ion15	= (float) TRAINING_DATA.get(i).get(15);
				ion16	= (float) TRAINING_DATA.get(i).get(16);
				ion17	= (float) TRAINING_DATA.get(i).get(17);
				ion18	= (float) TRAINING_DATA.get(i).get(18);
				ion19	= (float) TRAINING_DATA.get(i).get(19);
				ion20	= (float) TRAINING_DATA.get(i).get(20);
				ion21	= (float) TRAINING_DATA.get(i).get(21);
				ion22	= (float) TRAINING_DATA.get(i).get(22);
				ion23	= (float) TRAINING_DATA.get(i).get(23);
				ion24	= (float) TRAINING_DATA.get(i).get(24);
				ion25	= (float) TRAINING_DATA.get(i).get(25);
				ion26	= (float) TRAINING_DATA.get(i).get(26);
				ion27	= (float) TRAINING_DATA.get(i).get(27);
				ion28	= (float) TRAINING_DATA.get(i).get(28);
				ion29	= (float) TRAINING_DATA.get(i).get(29);
				ion30	= (float) TRAINING_DATA.get(i).get(30);
				ion31	= (float) TRAINING_DATA.get(i).get(31);
				ion32	= (float) TRAINING_DATA.get(i).get(32);
				ion33	= (float) TRAINING_DATA.get(i).get(33);
				expectedResult	= (float) TRAINING_DATA.get(i).get(34);

				((GPIndividual)ind).trees[0].child.eval(
						state,threadnum,input,stack,((GPIndividual)ind),this);

				if ((input.x >= 199.2 && expectedResult == 1) | (input.x < 199.2 && expectedResult == 0))
					hits++;
			}

			// the fitness better be KozaFitness!
			KozaFitness f = ((KozaFitness)ind.fitness);
			f.setStandardizedFitness(state,1 - (float)(hits/TRAINING_DATA.size()));
			f.hits = hits;
			ind.evaluated = true;


		}

	}

	/** PERFORM TESTING */
	@Override
	public void describe(final EvolutionState state, 
			final Individual ind, 
			final int subpopulation,
			final int threadnum,
			final int log){

		/* READ TEST FILE
		DataCruncher.LOCK_DOWN_SHUFFLE = false;
		if(!DataCruncher.LOCK_DOWN_SHUFFLE && state.isKFoldCrossValidation )
			TESTING_DATA = DataCruncher.shuffleData(state,TESTING_DATA,true);
		else if(!DataCruncher.LOCK_DOWN_SHUFFLE )
			TESTING_DATA = DataCruncher.shuffleData(state,Reader.readFile(testFile,","),true);
		 */

		int [][] confusionMatrix = new int[2][2];

		int hits = 0;
		float expectedResult;


		for(int i=0;i<TESTING_DATA.size();i++){
			//decoded Data Point
			ion0	= (float) TESTING_DATA.get(i).get(0);
			ion1	= (float) TESTING_DATA.get(i).get(1);
			ion2	= (float) TESTING_DATA.get(i).get(2);
			ion3	= (float) TESTING_DATA.get(i).get(3);
			ion4	= (float) TESTING_DATA.get(i).get(4);
			ion5	= (float) TESTING_DATA.get(i).get(5);
			ion6	= (float) TESTING_DATA.get(i).get(6);
			ion7	= (float) TESTING_DATA.get(i).get(7);
			ion8	= (float) TESTING_DATA.get(i).get(8);
			ion9	= (float) TESTING_DATA.get(i).get(9);
			ion10	= (float) TESTING_DATA.get(i).get(10);
			ion11	= (float) TESTING_DATA.get(i).get(11);
			ion12	= (float) TESTING_DATA.get(i).get(12);
			ion13	= (float) TESTING_DATA.get(i).get(13);
			ion14	= (float) TESTING_DATA.get(i).get(14);
			ion15	= (float) TESTING_DATA.get(i).get(15);
			ion16	= (float) TESTING_DATA.get(i).get(16);
			ion17	= (float) TESTING_DATA.get(i).get(17);
			ion18	= (float) TESTING_DATA.get(i).get(18);
			ion19	= (float) TESTING_DATA.get(i).get(19);
			ion20	= (float) TESTING_DATA.get(i).get(20);
			ion21	= (float) TESTING_DATA.get(i).get(21);
			ion22	= (float) TESTING_DATA.get(i).get(22);
			ion23	= (float) TESTING_DATA.get(i).get(23);
			ion24	= (float) TESTING_DATA.get(i).get(24);
			ion25	= (float) TESTING_DATA.get(i).get(25);
			ion26	= (float) TESTING_DATA.get(i).get(26);
			ion27	= (float) TESTING_DATA.get(i).get(27);
			ion28	= (float) TESTING_DATA.get(i).get(28);
			ion29	= (float) TESTING_DATA.get(i).get(29);
			ion30	= (float) TESTING_DATA.get(i).get(30);
			ion31	= (float) TESTING_DATA.get(i).get(31);
			ion32	= (float) TESTING_DATA.get(i).get(32);
			ion33	= (float) TESTING_DATA.get(i).get(33);
			expectedResult	= (float) TESTING_DATA.get(i).get(34);

			((GPIndividual)ind).trees[0].child.eval(
					state,threadnum,input,stack,((GPIndividual)ind),this);


			if(input.x >= 199.2 && expectedResult == 1.0)
			{confusionMatrix[0][0]++;hits++;}
			if(input.x <  199.2 && expectedResult == 1.0)
			{confusionMatrix[1][0]++;}
			if(input.x <  199.2 && expectedResult == 0.0)
			{confusionMatrix[0][1]++;hits++; }
			if(input.x >= 199.2 && expectedResult == 0.0)
			{confusionMatrix[1][1]++;}
			//System.out.println(expectedResult);

			// the fitness better be KozaFitness!
			KozaFitness f = ((KozaFitness)ind.fitness);
			f.setStandardizedFitness(state,1 - (float) (hits/TESTING_DATA.size()));
			f.hits = hits;
			ind.evaluated = true;
		}
		//System.exit(0);
		/** CONFUSION MATRIX + DIABETIC CANDIDATE STATUS IN TEST FILE */ 
		state.output.println("TP: "+confusionMatrix[0][0] + "\tTN: "+confusionMatrix[0][1]+"\t"
				+ "FP: "+confusionMatrix[1][0] + "\tFN: "+confusionMatrix[1][1]
						+ "\nTOTAL CASEA: "     +Reader.dataCount[1]
								+ "\tTOTAL CASEB: " +Reader.dataCount[0],log);

	}


}

