/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */



package ec.app.fsalps.sonar;
import ec.simple.SimpleProblemForm;
import ec.util.*;
import ec.*;
import ec.app.utils.datahouse.*;
import ec.gp.*;
import ec.gp.koza.*;

import java.util.ArrayList;

public class Sonar extends GPProblem implements SimpleProblemForm
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;

	/** 
	 * Source : https://archive.ics.uci.edu/ml/datasets/Connectionist+Bench+(Sonar,+Mines+vs.+Rocks)
	 * 
	 * The file "sonar.mines" contains 111 patterns obtained by bouncing sonar signals off a metal 
	 * cylinder at various angles and under various conditions. The file "sonar.rocks" contains 97 
	 * patterns obtained from rocks under similar conditions. The transmitted sonar signal is a 
	 * frequency-modulated chirp, rising in frequency. The data set contains signals obtained from a 
	 * variety of different aspect angles, spanning 90 degrees for the cylinder and 180 degrees for the rock. 
	 *
	 * Each pattern is a set of 60 numbers in the range 0.0 to 1.0. Each number represents the energy 
	 * within a particular frequency band, integrated over a certain period of time. The integration 
	 * aperture for higher frequencies occur later in time, since these frequencies are transmitted later 
	 * during the chirp. 
	 * 
	 * The label associated with each record contains the letter "R" if the object is a rock and "M" 
	 * if it is a mine (metal cylinder). The numbers in the labels are in increasing order of aspect angle, 
	 * but they do not encode the angle directly.
	 * 
	 */
	public float  enfb1,	enfb2,	enfb3,	enfb4,	enfb5,	enfb6,	enfb7,	enfb8,	enfb9,	enfb10,	
	enfb11,	enfb12,	enfb13,	enfb14,	enfb15,	enfb16,	enfb17,	enfb18,	enfb19,	enfb20,	
	enfb21,	enfb22,	enfb23,	enfb24,	enfb25,	enfb26,	enfb27,	enfb28,	enfb29,	enfb30,	
	enfb31,	enfb32,	enfb33,	enfb34,	enfb35,	enfb36,	enfb37,	enfb38,	enfb39,	enfb40,	
	enfb41,	enfb42,	enfb43,	enfb44,	enfb45,	enfb46,	enfb47,	enfb48,	enfb49,	enfb50,	
	enfb51,	enfb52,	enfb53,	enfb54,	enfb55,	enfb56,	enfb57,	enfb58,	enfb59,	enfb60;
	
    /** directory to raw unprocessed file */
	private final String DATA_RAW   = "sonar-all";
	/** directory to savel cleaned file */
	private final String DATA_CLEAN = "sonar-clean";
	/** directory to save training file */
	private final String TRAIN_DATA = "sonar-train-data";
	/** directory to save test file */
	private final String TEST_DATA  = "sonar-test-data";
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
		Sonar newobj = (Sonar) (super.clone());
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
		
		boolean kfoldCycleDataShuffle = state.parameters.
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
					((int)state.job[0]>0) && kfoldCycleDataShuffle)
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
					(int) (POPULATION_DATA.size()/state.kFoldCrossValidationSize), 
					(int) state.job[0]%state.kFoldCrossValidationSize);
			/* fetch testing chunk */
			TESTING_DATA = DataCruncher.selectTestingChunk
					(POPULATION_DATA, (int) (POPULATION_DATA.size()/state.kFoldCrossValidationSize), 
							(int) state.job[0]%state.kFoldCrossValidationSize);
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


			for(int i=0;i<TRAINING_DATA.size();i++)
			{
				//decoded Data Point
				enfb1    =   (float) TRAINING_DATA.get(i).get(0);
				enfb2    =   (float) TRAINING_DATA.get(i).get(1);
				enfb3    =   (float) TRAINING_DATA.get(i).get(2);
				enfb4    =   (float) TRAINING_DATA.get(i).get(3);
				enfb5    =   (float) TRAINING_DATA.get(i).get(4);
				enfb6    =   (float) TRAINING_DATA.get(i).get(5);
				enfb7    =   (float) TRAINING_DATA.get(i).get(6);
				enfb8    =   (float) TRAINING_DATA.get(i).get(7);
				enfb9    =   (float) TRAINING_DATA.get(i).get(8);
				enfb10   =   (float) TRAINING_DATA.get(i).get(9);
				enfb11   =   (float) TRAINING_DATA.get(i).get(10);
				enfb12   =   (float) TRAINING_DATA.get(i).get(11);
				enfb13   =   (float) TRAINING_DATA.get(i).get(12);
				enfb14   =   (float) TRAINING_DATA.get(i).get(13);
				enfb15   =   (float) TRAINING_DATA.get(i).get(14);
				enfb16   =   (float) TRAINING_DATA.get(i).get(15);
				enfb17   =   (float) TRAINING_DATA.get(i).get(16);
				enfb18   =   (float) TRAINING_DATA.get(i).get(17);
				enfb19   =   (float) TRAINING_DATA.get(i).get(18);
				enfb20   =   (float) TRAINING_DATA.get(i).get(19);
				enfb21   =   (float) TRAINING_DATA.get(i).get(20);
				enfb22   =   (float) TRAINING_DATA.get(i).get(21);
				enfb23   =   (float) TRAINING_DATA.get(i).get(22);
				enfb24   =   (float) TRAINING_DATA.get(i).get(23);
				enfb25   =   (float) TRAINING_DATA.get(i).get(24);
				enfb26   =   (float) TRAINING_DATA.get(i).get(25);
				enfb27   =   (float) TRAINING_DATA.get(i).get(26);
				enfb28   =   (float) TRAINING_DATA.get(i).get(27);
				enfb29   =   (float) TRAINING_DATA.get(i).get(28);
				enfb30   =   (float) TRAINING_DATA.get(i).get(29);
				enfb31   =   (float) TRAINING_DATA.get(i).get(30);
				enfb32   =   (float) TRAINING_DATA.get(i).get(31);
				enfb33   =   (float) TRAINING_DATA.get(i).get(32);
				enfb34   =   (float) TRAINING_DATA.get(i).get(33);
				enfb35   =   (float) TRAINING_DATA.get(i).get(34);
				enfb36   =   (float) TRAINING_DATA.get(i).get(35);
				enfb37   =   (float) TRAINING_DATA.get(i).get(36);
				enfb38   =   (float) TRAINING_DATA.get(i).get(37);
				enfb39   =   (float) TRAINING_DATA.get(i).get(38);
				enfb40   =   (float) TRAINING_DATA.get(i).get(39);
				enfb41   =   (float) TRAINING_DATA.get(i).get(40);
				enfb42   =   (float) TRAINING_DATA.get(i).get(41);
				enfb43   =   (float) TRAINING_DATA.get(i).get(42);
				enfb44   =   (float) TRAINING_DATA.get(i).get(43);
				enfb45   =   (float) TRAINING_DATA.get(i).get(44);
				enfb46   =   (float) TRAINING_DATA.get(i).get(45);
				enfb47   =   (float) TRAINING_DATA.get(i).get(46);
				enfb48   =   (float) TRAINING_DATA.get(i).get(47);
				enfb49   =   (float) TRAINING_DATA.get(i).get(48);
				enfb50   =   (float) TRAINING_DATA.get(i).get(49);
				enfb51   =   (float) TRAINING_DATA.get(i).get(50);
				enfb52   =   (float) TRAINING_DATA.get(i).get(51);
				enfb53   =   (float) TRAINING_DATA.get(i).get(52);
				enfb54   =   (float) TRAINING_DATA.get(i).get(53);
				enfb55   =   (float) TRAINING_DATA.get(i).get(54);
				enfb56   =   (float) TRAINING_DATA.get(i).get(55);
				enfb57   =   (float) TRAINING_DATA.get(i).get(56);
				enfb58   =   (float) TRAINING_DATA.get(i).get(57);
				enfb59   =   (float) TRAINING_DATA.get(i).get(58);
				enfb60   =   (float) TRAINING_DATA.get(i).get(59);

				expectedResult  = (float) TRAINING_DATA.get(i).get(60);


				((GPIndividual)ind).trees[0].child.eval(
						state,threadnum,input,stack,((GPIndividual)ind),this);


				if ((input.x >= 199.2 && expectedResult == 1.0) | (input.x < 199.2 && expectedResult == 0.0))
					hits++;
			}

			// the fitness better be KozaFitness!
			KozaFitness f = ((KozaFitness)ind.fitness);
			f.setStandardizedFitness(state,1 - (float)hits/TRAINING_DATA.size());
			f.hits = hits;
			ind.evaluated = true;

		}

	}

	/* PERFORM TESTING */
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


		for(int i=0;i<TESTING_DATA.size();i++)
		{
			//decoded Data Point
			enfb1    =   (float) TESTING_DATA.get(i).get(0);
			enfb2    =   (float) TESTING_DATA.get(i).get(1);
			enfb3    =   (float) TESTING_DATA.get(i).get(2);
			enfb4    =   (float) TESTING_DATA.get(i).get(3);
			enfb5    =   (float) TESTING_DATA.get(i).get(4);
			enfb6    =   (float) TESTING_DATA.get(i).get(5);
			enfb7    =   (float) TESTING_DATA.get(i).get(6);
			enfb8    =   (float) TESTING_DATA.get(i).get(7);
			enfb9    =   (float) TESTING_DATA.get(i).get(8);
			enfb10   =   (float) TESTING_DATA.get(i).get(9);
			enfb11   =   (float) TESTING_DATA.get(i).get(10);
			enfb12   =   (float) TESTING_DATA.get(i).get(11);
			enfb13   =   (float) TESTING_DATA.get(i).get(12);
			enfb14   =   (float) TESTING_DATA.get(i).get(13);
			enfb15   =   (float) TESTING_DATA.get(i).get(14);
			enfb16   =   (float) TESTING_DATA.get(i).get(15);
			enfb17   =   (float) TESTING_DATA.get(i).get(16);
			enfb18   =   (float) TESTING_DATA.get(i).get(17);
			enfb19   =   (float) TESTING_DATA.get(i).get(18);
			enfb20   =   (float) TESTING_DATA.get(i).get(19);
			enfb21   =   (float) TESTING_DATA.get(i).get(20);
			enfb22   =   (float) TESTING_DATA.get(i).get(21);
			enfb23   =   (float) TESTING_DATA.get(i).get(22);
			enfb24   =   (float) TESTING_DATA.get(i).get(23);
			enfb25   =   (float) TESTING_DATA.get(i).get(24);
			enfb26   =   (float) TESTING_DATA.get(i).get(25);
			enfb27   =   (float) TESTING_DATA.get(i).get(26);
			enfb28   =   (float) TESTING_DATA.get(i).get(27);
			enfb29   =   (float) TESTING_DATA.get(i).get(28);
			enfb30   =   (float) TESTING_DATA.get(i).get(29);
			enfb31   =   (float) TESTING_DATA.get(i).get(30);
			enfb32   =   (float) TESTING_DATA.get(i).get(31);
			enfb33   =   (float) TESTING_DATA.get(i).get(32);
			enfb34   =   (float) TESTING_DATA.get(i).get(33);
			enfb35   =   (float) TESTING_DATA.get(i).get(34);
			enfb36   =   (float) TESTING_DATA.get(i).get(35);
			enfb37   =   (float) TESTING_DATA.get(i).get(36);
			enfb38   =   (float) TESTING_DATA.get(i).get(37);
			enfb39   =   (float) TESTING_DATA.get(i).get(38);
			enfb40   =   (float) TESTING_DATA.get(i).get(39);
			enfb41   =   (float) TESTING_DATA.get(i).get(40);
			enfb42   =   (float) TESTING_DATA.get(i).get(41);
			enfb43   =   (float) TESTING_DATA.get(i).get(42);
			enfb44   =   (float) TESTING_DATA.get(i).get(43);
			enfb45   =   (float) TESTING_DATA.get(i).get(44);
			enfb46   =   (float) TESTING_DATA.get(i).get(45);
			enfb47   =   (float) TESTING_DATA.get(i).get(46);
			enfb48   =   (float) TESTING_DATA.get(i).get(47);
			enfb49   =   (float) TESTING_DATA.get(i).get(48);
			enfb50   =   (float) TESTING_DATA.get(i).get(49);
			enfb51   =   (float) TESTING_DATA.get(i).get(50);
			enfb52   =   (float) TESTING_DATA.get(i).get(51);
			enfb53   =   (float) TESTING_DATA.get(i).get(52);
			enfb54   =   (float) TESTING_DATA.get(i).get(53);
			enfb55   =   (float) TESTING_DATA.get(i).get(54);
			enfb56   =   (float) TESTING_DATA.get(i).get(55);
			enfb57   =   (float) TESTING_DATA.get(i).get(56);
			enfb58   =   (float) TESTING_DATA.get(i).get(57);
			enfb59   =   (float) TESTING_DATA.get(i).get(58);
			enfb60   =   (float) TESTING_DATA.get(i).get(59);  

			expectedResult  = (float) TESTING_DATA.get(i).get(60);

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


			// the fitness better be KozaFitness!
			KozaFitness f = ((KozaFitness)ind.fitness);
			f.setStandardizedFitness(state,1 - (float) hits/TESTING_DATA.size());
			f.hits = hits;
			ind.evaluated = true;
		}

		/** CONFUSION MATRIX + DIABETIC CANDIDATE STATUS IN TEST FILE */ 
		state.output.println("TP: "+confusionMatrix[0][0] + "\tTN: "+confusionMatrix[0][1]+"\t"
				+ "FP: "+confusionMatrix[1][0] + "\tFN: "+confusionMatrix[1][1]
						+ "\nTOTAL DIABETIC: "     +Reader.dataCount[1]
								+ "\tTOTAL NON-DIABETIC: " +Reader.dataCount[0],log);

	}


}

