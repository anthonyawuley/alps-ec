/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/



package ec.app.alps.sonar;
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
    
    private final String DATA_RAW   = "sonar-all";
    private final String DATA_CLEAN = "sonar-clean";
    private final String TRAIN_DATA = "sonar-train-data";
    private final String TEST_DATA  = "sonar-test-data";
    private final String NUMB_DATA_POINTS = "total-number-of-points";
    private       String trainFile, testFile;
    public  static int ctcmtrx = 0;
    private ArrayList<ArrayList> POPULATION_DATA;
 
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
        state.parameters.getInt(base.push(NUMB_DATA_POINTS), null);
        trainFile = state.parameters.getString(base.push(TRAIN_DATA), null);
        testFile = state.parameters.getString(base.push(TEST_DATA), null);
       
        String [] regex =  {"^[0]?[\\.]?[0]{0,},.*","^.*?[,]+[0]+\\.?[0]*\\,.*"};
        
       /** 
         * 1. Reads raw pima_indians_diabetes_data
         * 2. Filters content using regex above and passes output to dataClean
         * 3. Shuffles content from (2) above
         * 4. Split content into two [trainFile and testFile]
         * 5. Perform shuffle on either files based on experiment (training or testing)
         * NB:Files don't change once they have been created, cleaned and separated.
         *    This is done by checking existence of files at program call. however,
         *    Files are always shuffled at start of program
         */
        
        POPULATION_DATA = DataCruncher.shuffleData(
        		                Reader.readFile(
                                		 DataCruncher.cleanFile(regex,dataRaw,dataClean),","),false);
        
         /** Split formated data and write to training and test file */
         Out.writeDataToFile(POPULATION_DATA,trainFile,testFile);
        
      }

    
    
    public void evaluate(final EvolutionState state, 
        final Individual ind, 
        final int subpopulation,
        final int threadnum)
        {
          

        /**TRAINING FILE */
    	if(!DataCruncher.IS_SHUFFLED)
            POPULATION_DATA = DataCruncher.shuffleData(Reader.readFile(trainFile,","),true);
    	//else POPULATION_DATA = DataCruncher.shuffleData(POPULATION_DATA,true);
        
        if (!ind.evaluated)  // don't bother reevaluating
            {
            int hits = 0;
            float expectedResult;
         
           
            for(int i=0;i<POPULATION_DATA.size();i++)
            {
		        //decoded Data Point
            	enfb1   =   (float) POPULATION_DATA.get(i).get(0);
            	enfb2   =   (float) POPULATION_DATA.get(i).get(1);
            	enfb3   =   (float) POPULATION_DATA.get(i).get(2);
            	enfb4   =   (float) POPULATION_DATA.get(i).get(3);
            	enfb5   =   (float) POPULATION_DATA.get(i).get(4);
            	enfb6   =   (float) POPULATION_DATA.get(i).get(5);
            	enfb7   =   (float) POPULATION_DATA.get(i).get(6);
            	enfb8   =   (float) POPULATION_DATA.get(i).get(7);
            	enfb9   =   (float) POPULATION_DATA.get(i).get(8);
            	enfb10   =   (float) POPULATION_DATA.get(i).get(9);
            	enfb11   =   (float) POPULATION_DATA.get(i).get(10);
            	enfb12   =   (float) POPULATION_DATA.get(i).get(11);
            	enfb13   =   (float) POPULATION_DATA.get(i).get(12);
            	enfb14   =   (float) POPULATION_DATA.get(i).get(13);
            	enfb15   =   (float) POPULATION_DATA.get(i).get(14);
            	enfb16   =   (float) POPULATION_DATA.get(i).get(15);
            	enfb17   =   (float) POPULATION_DATA.get(i).get(16);
            	enfb18   =   (float) POPULATION_DATA.get(i).get(17);
            	enfb19   =   (float) POPULATION_DATA.get(i).get(18);
            	enfb20   =   (float) POPULATION_DATA.get(i).get(19);
            	enfb21   =   (float) POPULATION_DATA.get(i).get(20);
            	enfb22   =   (float) POPULATION_DATA.get(i).get(21);
            	enfb23   =   (float) POPULATION_DATA.get(i).get(22);
            	enfb24   =   (float) POPULATION_DATA.get(i).get(23);
            	enfb25   =   (float) POPULATION_DATA.get(i).get(24);
            	enfb26   =   (float) POPULATION_DATA.get(i).get(25);
            	enfb27   =   (float) POPULATION_DATA.get(i).get(26);
            	enfb28   =   (float) POPULATION_DATA.get(i).get(27);
            	enfb29   =   (float) POPULATION_DATA.get(i).get(28);
            	enfb30   =   (float) POPULATION_DATA.get(i).get(29);
            	enfb31   =   (float) POPULATION_DATA.get(i).get(30);
            	enfb32   =   (float) POPULATION_DATA.get(i).get(31);
            	enfb33   =   (float) POPULATION_DATA.get(i).get(32);
            	enfb34   =   (float) POPULATION_DATA.get(i).get(33);
            	enfb35   =   (float) POPULATION_DATA.get(i).get(34);
            	enfb36   =   (float) POPULATION_DATA.get(i).get(35);
            	enfb37   =   (float) POPULATION_DATA.get(i).get(36);
            	enfb38   =   (float) POPULATION_DATA.get(i).get(37);
            	enfb39   =   (float) POPULATION_DATA.get(i).get(38);
            	enfb40   =   (float) POPULATION_DATA.get(i).get(39);
            	enfb41   =   (float) POPULATION_DATA.get(i).get(40);
            	enfb42   =   (float) POPULATION_DATA.get(i).get(41);
            	enfb43   =   (float) POPULATION_DATA.get(i).get(42);
            	enfb44   =   (float) POPULATION_DATA.get(i).get(43);
            	enfb45   =   (float) POPULATION_DATA.get(i).get(44);
            	enfb46   =   (float) POPULATION_DATA.get(i).get(45);
            	enfb47   =   (float) POPULATION_DATA.get(i).get(46);
            	enfb48   =   (float) POPULATION_DATA.get(i).get(47);
            	enfb49   =   (float) POPULATION_DATA.get(i).get(48);
            	enfb50   =   (float) POPULATION_DATA.get(i).get(49);
            	enfb51   =   (float) POPULATION_DATA.get(i).get(50);
            	enfb52   =   (float) POPULATION_DATA.get(i).get(51);
            	enfb53   =   (float) POPULATION_DATA.get(i).get(52);
            	enfb54   =   (float) POPULATION_DATA.get(i).get(53);
            	enfb55   =   (float) POPULATION_DATA.get(i).get(54);
            	enfb56   =   (float) POPULATION_DATA.get(i).get(55);
            	enfb57   =   (float) POPULATION_DATA.get(i).get(56);
            	enfb58   =   (float) POPULATION_DATA.get(i).get(57);
            	enfb59   =   (float) POPULATION_DATA.get(i).get(58);
            	enfb60   =   (float) POPULATION_DATA.get(i).get(59);
                  
                expectedResult  = (float) POPULATION_DATA.get(i).get(60);
                
                ((GPIndividual)ind).trees[0].child.eval(
                        state,threadnum,input,stack,((GPIndividual)ind),this);
            
                 
                  if ((input.x >= 199.2 && expectedResult == 1.0) | (input.x < 199.2 && expectedResult == 0.0))
                         hits++;
	     }
            
            // the fitness better be KozaFitness!
            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state,1 - (float)hits/POPULATION_DATA.size());
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
        
       
        /**TRAINING FILE */
    	DataCruncher.IS_SHUFFLED = false;
    	if(!DataCruncher.IS_SHUFFLED)
            POPULATION_DATA = DataCruncher.shuffleData(Reader.readFile(testFile,","),true);
        
        int [][] confusionMatrix = new int[2][2];
        int hits = 0;
        float expectedResult;
          
           
            for(int i=0;i<POPULATION_DATA.size();i++)
            {
		        //decoded Data Point
            	enfb1   =   (float) POPULATION_DATA.get(i).get(0);
            	enfb2   =   (float) POPULATION_DATA.get(i).get(1);
            	enfb3   =   (float) POPULATION_DATA.get(i).get(2);
            	enfb4   =   (float) POPULATION_DATA.get(i).get(3);
            	enfb5   =   (float) POPULATION_DATA.get(i).get(4);
            	enfb6   =   (float) POPULATION_DATA.get(i).get(5);
            	enfb7   =   (float) POPULATION_DATA.get(i).get(6);
            	enfb8   =   (float) POPULATION_DATA.get(i).get(7);
            	enfb9   =   (float) POPULATION_DATA.get(i).get(8);
            	enfb10   =   (float) POPULATION_DATA.get(i).get(9);
            	enfb11   =   (float) POPULATION_DATA.get(i).get(10);
            	enfb12   =   (float) POPULATION_DATA.get(i).get(11);
            	enfb13   =   (float) POPULATION_DATA.get(i).get(12);
            	enfb14   =   (float) POPULATION_DATA.get(i).get(13);
            	enfb15   =   (float) POPULATION_DATA.get(i).get(14);
            	enfb16   =   (float) POPULATION_DATA.get(i).get(15);
            	enfb17   =   (float) POPULATION_DATA.get(i).get(16);
            	enfb18   =   (float) POPULATION_DATA.get(i).get(17);
            	enfb19   =   (float) POPULATION_DATA.get(i).get(18);
            	enfb20   =   (float) POPULATION_DATA.get(i).get(19);
            	enfb21   =   (float) POPULATION_DATA.get(i).get(20);
            	enfb22   =   (float) POPULATION_DATA.get(i).get(21);
            	enfb23   =   (float) POPULATION_DATA.get(i).get(22);
            	enfb24   =   (float) POPULATION_DATA.get(i).get(23);
            	enfb25   =   (float) POPULATION_DATA.get(i).get(24);
            	enfb26   =   (float) POPULATION_DATA.get(i).get(25);
            	enfb27   =   (float) POPULATION_DATA.get(i).get(26);
            	enfb28   =   (float) POPULATION_DATA.get(i).get(27);
            	enfb29   =   (float) POPULATION_DATA.get(i).get(28);
            	enfb30   =   (float) POPULATION_DATA.get(i).get(29);
            	enfb31   =   (float) POPULATION_DATA.get(i).get(30);
            	enfb32   =   (float) POPULATION_DATA.get(i).get(31);
            	enfb33   =   (float) POPULATION_DATA.get(i).get(32);
            	enfb34   =   (float) POPULATION_DATA.get(i).get(33);
            	enfb35   =   (float) POPULATION_DATA.get(i).get(34);
            	enfb36   =   (float) POPULATION_DATA.get(i).get(35);
            	enfb37   =   (float) POPULATION_DATA.get(i).get(36);
            	enfb38   =   (float) POPULATION_DATA.get(i).get(37);
            	enfb39   =   (float) POPULATION_DATA.get(i).get(38);
            	enfb40   =   (float) POPULATION_DATA.get(i).get(39);
            	enfb41   =   (float) POPULATION_DATA.get(i).get(40);
            	enfb42   =   (float) POPULATION_DATA.get(i).get(41);
            	enfb43   =   (float) POPULATION_DATA.get(i).get(42);
            	enfb44   =   (float) POPULATION_DATA.get(i).get(43);
            	enfb45   =   (float) POPULATION_DATA.get(i).get(44);
            	enfb46   =   (float) POPULATION_DATA.get(i).get(45);
            	enfb47   =   (float) POPULATION_DATA.get(i).get(46);
            	enfb48   =   (float) POPULATION_DATA.get(i).get(47);
            	enfb49   =   (float) POPULATION_DATA.get(i).get(48);
            	enfb50   =   (float) POPULATION_DATA.get(i).get(49);
            	enfb51   =   (float) POPULATION_DATA.get(i).get(50);
            	enfb52   =   (float) POPULATION_DATA.get(i).get(51);
            	enfb53   =   (float) POPULATION_DATA.get(i).get(52);
            	enfb54   =   (float) POPULATION_DATA.get(i).get(53);
            	enfb55   =   (float) POPULATION_DATA.get(i).get(54);
            	enfb56   =   (float) POPULATION_DATA.get(i).get(55);
            	enfb57   =   (float) POPULATION_DATA.get(i).get(56);
            	enfb58   =   (float) POPULATION_DATA.get(i).get(57);
            	enfb59   =   (float) POPULATION_DATA.get(i).get(58);
            	enfb60   =   (float) POPULATION_DATA.get(i).get(59);  
                  
                expectedResult  = (float) POPULATION_DATA.get(i).get(60);
                
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
            f.setStandardizedFitness(state,1 - (float) hits/POPULATION_DATA.size());
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

