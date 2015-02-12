/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/



package ec.app.canonical.breastcancer;
import ec.simple.SimpleProblemForm;
import ec.util.*;
import ec.*;
import ec.app.canonical.breastcancer.DoubleData;
import ec.app.utils.datahouse.DataCruncher;
import ec.app.utils.datahouse.Out;
import ec.app.utils.datahouse.Reader;
import ec.gp.*;
import ec.gp.koza.*;

import java.util.ArrayList;

public class BreastCancer extends GPProblem implements SimpleProblemForm
    {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1;

	/** 
     * 1) ID number 
     * 2) Diagnosis (M = malignant, B = benign) 
     * 3-32) 
     * Ten real-valued features are computed for each cell nucleus: 
     * a) radius (mean of distances from center to points on the perimeter) 
     * b) texture (standard deviation of gray-scale values) 
     * c) perimeter 
     * d) area 
     * e) smoothness (local variation in radius lengths) 
     * f) compactness (perimeter^2 / area - 1.0) 
     * g) concavity (severity of concave portions of the contour) 
     * h) concave points (number of concave portions of the contour) 
     * i) symmetry 
     * j) fractal dimension ("coastline approximation" - 1)
     */
    
    public float  wdbc0,	wdbc1,	wdbc2,	wdbc3,	wdbc4,	wdbc5,	wdbc6,	wdbc7,	wdbc8,	wdbc9,	wdbc10,	
                  wdbc11,	wdbc12,	wdbc13,	wdbc14,	wdbc15,	wdbc16,	wdbc17,	wdbc18,	wdbc19,	wdbc20,	wdbc21,	
                  wdbc22,	wdbc23,	wdbc24,	wdbc25,	wdbc26,	wdbc27,	wdbc28,	wdbc29,	wdbc30,	wdbc31,	wdbc32,	
                  wdbc33;
    
    
  	/** directory to raw unprocessed file */
  	private final String DATA_RAW   = "wdbc-all";
  	/** directory to savel cleaned file */
  	private final String DATA_CLEAN = "wdbc-clean";
  	/** directory to save training file */
  	private final String TRAIN_DATA = "wdbc-train-data";
  	/** directory to save test file */
  	private final String TEST_DATA  = "wdbc-test-data";
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
        BreastCancer newobj = (BreastCancer) (super.clone());
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
            	wdbc0	=	(float) TRAINING_DATA.get(i).get(0); //id
            	expectedResult	=	(float) TRAINING_DATA.get(i).get(1);
            	
            	wdbc2	=	(float) TRAINING_DATA.get(i).get(2);
            	wdbc3	=	(float) TRAINING_DATA.get(i).get(3);
            	wdbc4	=	(float) TRAINING_DATA.get(i).get(4);
            	wdbc5	=	(float) TRAINING_DATA.get(i).get(5);
            	wdbc6	=	(float) TRAINING_DATA.get(i).get(6);
            	wdbc7	=	(float) TRAINING_DATA.get(i).get(7);
            	wdbc8	=	(float) TRAINING_DATA.get(i).get(8);
            	wdbc9	=	(float) TRAINING_DATA.get(i).get(9);
            	wdbc10	=	(float) TRAINING_DATA.get(i).get(10);
            	wdbc11	=	(float) TRAINING_DATA.get(i).get(11);
            	wdbc12	=	(float) TRAINING_DATA.get(i).get(12);
            	wdbc13	=	(float) TRAINING_DATA.get(i).get(13);
            	wdbc14	=	(float) TRAINING_DATA.get(i).get(14);
            	wdbc15	=	(float) TRAINING_DATA.get(i).get(15);
            	wdbc16	=	(float) TRAINING_DATA.get(i).get(16);
            	wdbc17	=	(float) TRAINING_DATA.get(i).get(17);
            	wdbc18	=	(float) TRAINING_DATA.get(i).get(18);
            	wdbc19	=	(float) TRAINING_DATA.get(i).get(19);
            	wdbc20	=	(float) TRAINING_DATA.get(i).get(20);
            	wdbc21	=	(float) TRAINING_DATA.get(i).get(21);
            	wdbc22	=	(float) TRAINING_DATA.get(i).get(22);
            	wdbc23	=	(float) TRAINING_DATA.get(i).get(23);
            	wdbc24	=	(float) TRAINING_DATA.get(i).get(24);
            	wdbc25	=	(float) TRAINING_DATA.get(i).get(25);
            	wdbc26	=	(float) TRAINING_DATA.get(i).get(26);
            	wdbc27	=	(float) TRAINING_DATA.get(i).get(27);
            	wdbc28	=	(float) TRAINING_DATA.get(i).get(28);
            	wdbc29	=	(float) TRAINING_DATA.get(i).get(29);
            	wdbc30	=	(float) TRAINING_DATA.get(i).get(30);
            	wdbc31	=	(float) TRAINING_DATA.get(i).get(31);
            	//wdbc32	=	(float) TRAINING_DATA.get(i).get(32);
            	//wdbc33	=	(float) TRAINING_DATA.get(i).get(33);
            	
                  
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
            
            /*
             * state.output.println("TP: "+confusionMatrix[0][0] + "\tTN: "+confusionMatrix[0][1]+"\n"
                                  + "FP: "+confusionMatrix[1][0] + "\tFN: "+confusionMatrix[0][1],2);
             */
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
            	wdbc0	=	(float) TESTING_DATA.get(i).get(0); //id
            	expectedResult	=	(float) TESTING_DATA.get(i).get(1);
            	
            	wdbc2	=	(float) TESTING_DATA.get(i).get(2);
            	wdbc3	=	(float) TESTING_DATA.get(i).get(3);
            	wdbc4	=	(float) TESTING_DATA.get(i).get(4);
            	wdbc5	=	(float) TESTING_DATA.get(i).get(5);
            	wdbc6	=	(float) TESTING_DATA.get(i).get(6);
            	wdbc7	=	(float) TESTING_DATA.get(i).get(7);
            	wdbc8	=	(float) TESTING_DATA.get(i).get(8);
            	wdbc9	=	(float) TESTING_DATA.get(i).get(9);
            	wdbc10	=	(float) TESTING_DATA.get(i).get(10);
            	wdbc11	=	(float) TESTING_DATA.get(i).get(11);
            	wdbc12	=	(float) TESTING_DATA.get(i).get(12);
            	wdbc13	=	(float) TESTING_DATA.get(i).get(13);
            	wdbc14	=	(float) TESTING_DATA.get(i).get(14);
            	wdbc15	=	(float) TESTING_DATA.get(i).get(15);
            	wdbc16	=	(float) TESTING_DATA.get(i).get(16);
            	wdbc17	=	(float) TESTING_DATA.get(i).get(17);
            	wdbc18	=	(float) TESTING_DATA.get(i).get(18);
            	wdbc19	=	(float) TESTING_DATA.get(i).get(19);
            	wdbc20	=	(float) TESTING_DATA.get(i).get(20);
            	wdbc21	=	(float) TESTING_DATA.get(i).get(21);
            	wdbc22	=	(float) TESTING_DATA.get(i).get(22);
            	wdbc23	=	(float) TESTING_DATA.get(i).get(23);
            	wdbc24	=	(float) TESTING_DATA.get(i).get(24);
            	wdbc25	=	(float) TESTING_DATA.get(i).get(25);
            	wdbc26	=	(float) TESTING_DATA.get(i).get(26);
            	wdbc27	=	(float) TESTING_DATA.get(i).get(27);
            	wdbc28	=	(float) TESTING_DATA.get(i).get(28);
            	wdbc29	=	(float) TESTING_DATA.get(i).get(29);
            	wdbc30	=	(float) TESTING_DATA.get(i).get(30);
            	wdbc31	=	(float) TESTING_DATA.get(i).get(31);
            	//wdbc32	=	(float) POPULATION_DATA.get(i).get(32);
            	//wdbc33	=	(float) POPULATION_DATA.get(i).get(33);
            	
                  
                ((GPIndividual)ind).trees[0].child.eval(
                        state,threadnum,input,stack,((GPIndividual)ind),this);

                 /**
                    if ((input.x >= 199.2 && expectedResult == 1) | (input.x < 199.2 && expectedResult == 0))
                    hits++;
                    * 
                    * Hit&/Confusion matrix conditions
                   */
                
                 if(input.x >= 199.2 && expectedResult == 1)
                      {confusionMatrix[0][0]++;hits++;}
                 if(input.x <  199.2 && expectedResult == 1)
                      {confusionMatrix[1][0]++;}
                 if(input.x <  199.2 && expectedResult == 0)
                      {confusionMatrix[0][1]++;hits++; }
                 if(input.x >= 199.2 && expectedResult == 0)
                      {confusionMatrix[1][1]++;}
                
           
            // the fitness better be KozaFitness!
            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state,1 - (float)hits/TESTING_DATA.size());
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

