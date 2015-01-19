/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/



package ec.app.canonical.pima;
import ec.simple.SimpleProblemForm;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;

import java.util.ArrayList;

public class MultiValuedRegressionCoscA1B extends GPProblem implements SimpleProblemForm
    {
    /** 
     * Declare variables that will serve as terminal set
     * Variable names are descriptive off their meaining and are as follows
     * 1.Number of times pregnant 
     * 2.Plasma glucose concentration a 2 hours in an oral glucose tolerance test 
     * 3.Diastolic blood pressure (mm Hg) 
     * 4.Triceps skin fold thickness (mm) 
     * 5.2-Hour serum insulin (mu U/ml) 
     * 6.Body mass index (weight in kg/(height in m)^2) 
     * 7.Diabetes pedigree function 
     * 8.Age (years) 
     * 9.Class variable (0 or 1) 
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
    
    private final String DATA_RAW   = "pima-indians-diabetes-data-raw";
    private final String DATA_CLEAN = "pima-indians-diabetes-data-clean";
    private final String TRAIN_DATA = "pima-indians-diabetes-data-train-data";
    private final String TEST_DATA  = "pima-indians-diabetes-data-test-data";
    private final String NUMB_DATA_POINTS = "total-number-of-points";
    private       String trainFile,testFile;
    public  static int ctcmtrx = 0;
    private int numberOfDataPoints;
    
    private ArrayList<ArrayList> POPULATION_DATA;
 
    public DoubleData input;

    public Object clone()
        {
        MultiValuedRegressionCoscA1B newobj = (MultiValuedRegressionCoscA1B) (super.clone());
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
        numberOfDataPoints = state.parameters.getInt(base.push(NUMB_DATA_POINTS), null);
        trainFile = state.parameters.getString(base.push(TRAIN_DATA), null);
        testFile = state.parameters.getString(base.push(TEST_DATA), null);
       
        String [] regex =  {"^[0]?[\\.]?[0]{0,},.*","^.*?[,]+[0]+\\.?[0]*\\,.*"};
        
       /** 
         * 1. Reads raw pima_indians_diabetes_data
         * 2. Filters content using regex above and passes output to dataClean
         * 3. Shuffles content from (2) above
         * 4. Split content into two [trainFile and testFile]
         * 5. Perform shuffle on either files based on experiment (training or testing)
         * NB:Files dont change once they have been created, cleaned and seperated.
         *    This is done by checking existence of files at program call. however,
         *    Files are always shuffled at start of program
         */
        
        
        POPULATION_DATA = DataCruncher.shuffleData(
                                 DataCruncher.readFile(
                                       DataCruncher.cleanFile(regex,dataRaw,dataClean),","));
        
         /** Split formated data and write to training and test file */
         DataCruncher.writeDataToFile(POPULATION_DATA,trainFile,testFile);
        
       
        }

    
    
    public void evaluate(final EvolutionState state, 
        final Individual ind, 
        final int subpopulation,
        final int threadnum)
        {
          
         /**TRAINING FILE */
         POPULATION_DATA = DataCruncher.shuffleData(DataCruncher.readFile(trainFile,"\\s"));
        
        if (!ind.evaluated)  // don't bother reevaluating
            {
            int hits = 0;
            float expectedResult;
            
           
            for(int i=0;i<POPULATION_DATA.size();i++){
		  //decoded Data Point
                  numbPreg                = (float) POPULATION_DATA.get(i).get(0);
                  plasmaGlucoseConc       = (float) POPULATION_DATA.get(i).get(1);
                  diastolicBP             = (float) POPULATION_DATA.get(i).get(2);
                  trcpSknFldThknes        = (float) POPULATION_DATA.get(i).get(3);
                  twoHrSerIns             = (float) POPULATION_DATA.get(i).get(4);
                  bodyMassId              = (float) POPULATION_DATA.get(i).get(5);
                  diabetesPedgFn          = (float) POPULATION_DATA.get(i).get(6);
                  age                     = (float) POPULATION_DATA.get(i).get(7);
                  
                  expectedResult          = (float) POPULATION_DATA.get(i).get(8);
                  
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
            f.setStandardizedFitness(state,1 - (float)hits/POPULATION_DATA.size());
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
        
       
        /**TRAINING FILE */
        POPULATION_DATA = DataCruncher.shuffleData(DataCruncher.readFile(testFile,"\\s"));
        
        int [][] confusionMatrix = new int[2][2];
       
        
            int hits = 0;
            float expectedResult;
            
           
            for(int i=0;i<POPULATION_DATA.size();i++){
		  //decoded Data Point
                  numbPreg                = (float) POPULATION_DATA.get(i).get(0);
                  plasmaGlucoseConc       = (float) POPULATION_DATA.get(i).get(1);
                  diastolicBP             = (float) POPULATION_DATA.get(i).get(2);
                  trcpSknFldThknes        = (float) POPULATION_DATA.get(i).get(3);
                  twoHrSerIns             = (float) POPULATION_DATA.get(i).get(4);
                  bodyMassId              = (float) POPULATION_DATA.get(i).get(5);
                  diabetesPedgFn          = (float) POPULATION_DATA.get(i).get(6);
                  age                     = (float) POPULATION_DATA.get(i).get(7);
                  
                  expectedResult          = (float) POPULATION_DATA.get(i).get(8);
                  
                ((GPIndividual)ind).trees[0].child.eval(
                        state,threadnum,input,stack,((GPIndividual)ind),this);

                 /**
                    if ((input.x >= 199.2 && expectedResult == 1) | (input.x < 199.2 && expectedResult == 0))
                    hits++;
                    * 
                    * Hit&/Confusion matrix conditions
                   */
                
                 if(input.x >= 199.2 && expectedResult == 1){confusionMatrix[0][0]++;hits++;}
                 if(input.x <  199.2 && expectedResult == 1){confusionMatrix[1][0]++;}
                 if(input.x <  199.2 && expectedResult == 0){confusionMatrix[0][1]++;hits++; }
                 if(input.x >= 199.2 && expectedResult == 0){confusionMatrix[1][1]++;}
                
           
            // the fitness better be KozaFitness!
            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state,1 - (float)hits/POPULATION_DATA.size());
            f.hits = hits;
            ind.evaluated = true;
            }
            
           /** CONFUSION MATRIX + DIABETIC CANDIDATE STATUS IN TEST FILE */ 
           state.output.println("TP: "+confusionMatrix[0][0] + "\tTN: "+confusionMatrix[0][1]+"\t"
                              + "FP: "+confusionMatrix[1][0] + "\tFN: "+confusionMatrix[1][1]
                              + "\nTOTAL DIABETIC: "     +DataCruncher.dataCount[1]
                              + "\tTOTAL NON-DIABETIC: " +DataCruncher.dataCount[0],2);
          
        }
     
    
    }

