/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */



package ec.app.alps.pima;
import ec.simple.SimpleProblemForm;
import ec.util.*;
import ec.*;
import ec.app.alps.pima.DoubleData;
import ec.app.utils.datahouse.DataCruncher;
import ec.app.utils.datahouse.Out;
import ec.app.utils.datahouse.Reader;
import ec.gp.*;
import ec.gp.koza.*;

import java.util.ArrayList;

public class Pima extends GPProblem implements SimpleProblemForm
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;

	/** 
	 * Declare variables that will serve as terminal set
	 * Variable names are descriptive off their meaining and are as follows<br>
	 * 1.Number of times pregnant <br>
	 * 2.Plasma glucose concentration a 2 hours in an oral glucose tolerance test <br>
	 * 3.Diastolic blood pressure (mm Hg) <br>
	 * 4.Triceps skin fold thickness (mm) <br>
	 * 5.2-Hour serum insulin (mu U/ml) <br>
	 * 6.Body mass index (weight in kg/(height in m)^2) <br>
	 * 7.Diabetes pedigree function <br>
	 * 8.Age (years) <br>
	 * 9.Class variable (0 or 1) <br>
	 */

	public float  numbPreg,
	plasmaGlucoseConc,
	diastolicBP,
	trcpSknFldThknes,
	twoHrSerIns,
	bodyMassId,
	diabetesPedgFn,
	age,
	classVar;


	/** directory to raw unprocessed file */
	private final String DATA_RAW   = "pima-indians-diabetes-data-raw";
	/** directory to savel cleaned file */
	private final String DATA_CLEAN = "pima-indians-diabetes-data-clean";
	/** directory to save training file */
	private final String TRAIN_DATA = "pima-indians-diabetes-data-train-data";
	/** directory to save test file */
	private final String TEST_DATA  = "pima-indians-diabetes-data-test-data";
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
		Pima newobj = (Pima) (super.clone());
		newobj.input = (DoubleData)(input.clone());
		return newobj;
	}

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

		String [] regex =  {"^[0]?[\\.]?[0]{0,},.*","^.*?[,]+[0]+\\.?[0]*\\,.*"};

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
			if(!DataCruncher.KFOLD_LOCK_DOWN_SHUFFLE) 
			{ 
				POPULATION_DATA = DataCruncher.shuffleData(state,
						Reader.readFile(DataCruncher.cleanFile(regex,dataRaw,dataClean+".clean"),","),false);
				Out.writeDataToFile(POPULATION_DATA,dataClean,false); //write if file does not exist
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
				/* turn off shuffle lock  and force shuffle */
				DataCruncher.KFOLD_LOCK_DOWN_SHUFFLE = false; 
				POPULATION_DATA = DataCruncher.shuffleData(state,POPULATION_DATA,false);
				/* forced rewrite of cleaned data) */
				Out.writeDataToFile(POPULATION_DATA,dataClean,true); 
			}
			else //read from file
			{  
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
		}
		else
		{
			POPULATION_DATA = DataCruncher.shuffleData(state,Reader.readFile(
					DataCruncher.cleanFile(regex,dataRaw,dataClean),"\\s"),false);
			/* Split formated data and write to training and test file */
			Out.writeDataToFile(POPULATION_DATA,trainFile,testFile);
		}

		/* TRAINING FILE */
		if(!DataCruncher.IS_SHUFFLED && state.isKFoldCrossValidation )
			TRAINING_DATA = DataCruncher.shuffleData(state,TRAINING_DATA,true);
		else if(!DataCruncher.IS_SHUFFLED) //reading from training file
			TRAINING_DATA = DataCruncher.shuffleData(state,Reader.readFile(trainFile,","),true);
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
				numbPreg                = (float) TRAINING_DATA.get(i).get(0);
				plasmaGlucoseConc       = (float) TRAINING_DATA.get(i).get(1);
				diastolicBP             = (float) TRAINING_DATA.get(i).get(2);
				trcpSknFldThknes        = (float) TRAINING_DATA.get(i).get(3);
				twoHrSerIns             = (float) TRAINING_DATA.get(i).get(4);
				bodyMassId              = (float) TRAINING_DATA.get(i).get(5);
				diabetesPedgFn          = (float) TRAINING_DATA.get(i).get(6);
				age                     = (float) TRAINING_DATA.get(i).get(7);

				expectedResult          = (float) TRAINING_DATA.get(i).get(8);

				((GPIndividual)ind).trees[0].child.eval(
						state,threadnum,input,stack,((GPIndividual)ind),this);
				/*
                   //hits=input.x >= 0 && expectedResult == 1?hits++:hits;
                   //hits=input.x <  0 && expectedResult == 0?hits++:hits;
                   //varied 0,5,10, 20, 55,56,59:140  60,600:141 65:141  70:139 200:144
                   //199.2:145 : just run ok in run 1
				 */

				if ((input.x >= 199.2 && expectedResult == 1) | (input.x < 199.2 && expectedResult == 0))
					hits++;
			}

			// the fitness better be KozaFitness!
			KozaFitness f = ((KozaFitness)ind.fitness);
			f.setStandardizedFitness(state,1 - (float)hits/TRAINING_DATA.size());
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


		/* READ TEST FILE */
		DataCruncher.IS_SHUFFLED = false;
		if(!DataCruncher.IS_SHUFFLED && state.isKFoldCrossValidation )
			TESTING_DATA = DataCruncher.shuffleData(state,TESTING_DATA,true);
		else if(!DataCruncher.IS_SHUFFLED )
			TESTING_DATA = DataCruncher.shuffleData(state,Reader.readFile(testFile,","),true);

		int [][] confusionMatrix = new int[2][2];


		int hits = 0;
		float expectedResult;


		for(int i=0;i<TESTING_DATA.size();i++){
			//decoded Data Point
			numbPreg                = (float) TESTING_DATA.get(i).get(0);
			plasmaGlucoseConc       = (float) TESTING_DATA.get(i).get(1);
			diastolicBP             = (float) TESTING_DATA.get(i).get(2);
			trcpSknFldThknes        = (float) TESTING_DATA.get(i).get(3);
			twoHrSerIns             = (float) TESTING_DATA.get(i).get(4);
			bodyMassId              = (float) TESTING_DATA.get(i).get(5);
			diabetesPedgFn          = (float) TESTING_DATA.get(i).get(6);
			age                     = (float) TESTING_DATA.get(i).get(7);

			expectedResult          = (float) TESTING_DATA.get(i).get(8);

			((GPIndividual)ind).trees[0].child.eval(
					state,threadnum,input,stack,((GPIndividual)ind),this);


			if(input.x >= 199.2 && expectedResult == 1){confusionMatrix[0][0]++;hits++;}
			if(input.x <  199.2 && expectedResult == 1){confusionMatrix[1][0]++;}
			if(input.x <  199.2 && expectedResult == 0){confusionMatrix[0][1]++;hits++; }
			if(input.x >= 199.2 && expectedResult == 0){confusionMatrix[1][1]++;}


			// the fitness better be KozaFitness!
			KozaFitness f = ((KozaFitness)ind.fitness);
			f.setStandardizedFitness(state,1 - (float) hits/TESTING_DATA.size());
			f.hits = hits;
			ind.evaluated = true;
		}

		/** CONFUSION MATRIX + DIABETIC CANDIDATE STATUS IN TEST FILE */ 
		state.output.println("TP: "+confusionMatrix[0][0] + "\tTN: "+confusionMatrix[0][1]+"\t"
				+ "FP: "+confusionMatrix[1][0] + "\tFN: "+confusionMatrix[1][1]
						+ "\nTOTAL CASEA: "     +Reader.dataCount[1]
						+ "\tTOTAL CASEB: " +Reader.dataCount[0],log);

	}


}

