/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.texture;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MultiValuedRegression extends GPProblem implements SimpleProblemForm
    {
    private static final long serialVersionUID = 1;
    private static final String TRAINING_DATASET = "training-file";
    private String training;
    public double avg3x3, avg7x7, avg9x9, SD3x3, SD7x7, SD9x9;
    public double currentX, currentY;
    
    
    public void setup(final EvolutionState state,
        final Parameter base)
        {
        super.setup(state, base);
        training = state.parameters.getString(base.push(TRAINING_DATASET), null);  
        
        // verify our input is the right class (or subclasses from it)
        if (!(input instanceof DoubleData))
            state.output.fatal("GPData class must subclass from " + DoubleData.class,
                base.push(P_DATA), null);
        }
        
    public void evaluate(final EvolutionState state, 
        final Individual ind, 
        final int subpopulation,
        final int threadnum)
        {
        if (!ind.evaluated)  // don't bother reevaluating
            {
            DoubleData input = (DoubleData)(this.input);
        
            int hits = 0;
            double sum = 0.0;
            double expectedResult;
            double result;
            //fileReader();
            
            
            @SuppressWarnings("deprecation")
            File file = new File(training);
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            DataInputStream dis = null; 
            try {
              fis = new FileInputStream(file);

              // Here BufferedInputStream is added for fast reading.
              bis = new BufferedInputStream(fis);
              dis = new DataInputStream(bis);

              // dis.available() returns 0 if the file does not have more lines.
              while (dis.available() != 0) {

              // this statement reads the line from the file and print it to
                // the console.
            	String [] splitted = dis.readLine().split(",");
            	String first = splitted[0];
            	String second = splitted[1];
            	
            	currentX  = Double.parseDouble(first);
            	currentY =  Double.parseDouble(second);
            	
            	expectedResult = currentX*currentX*currentY + currentX*currentY + currentY;
              ((GPIndividual)ind).trees[0].child.eval(state,threadnum,input,stack,((GPIndividual)ind),this);
              result = Math.abs(expectedResult - input.x);
              if (result <= 0.01) hits++;
              sum += result; 
       //         System.out.println(first);
         //       System.out.println(second);
              }

              // dispose all the resources after using them.
              fis.close();
              bis.close();
              dis.close();

            } catch (FileNotFoundException e) {
              e.printStackTrace();
            } catch (IOException e) {
              e.printStackTrace();
            }
            
            
            
            
//            for (int y=0;y<10;y++)
//                {
//                currentX = state.random[threadnum].nextDouble();
//            	currentY = state.random[threadnum].nextDouble();
//                expectedResult = currentX*currentX*currentY + currentX*currentY + currentY;
//                ((GPIndividual)ind).trees[0].child.eval(
//                    state,threadnum,input,stack,((GPIndividual)ind),this);
//
//                result = Math.abs(expectedResult - input.x);
//                if (result <= 0.01) hits++;
//                sum += result;                  
//                }

            // the fitness better be KozaFitness!
            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state, sum);
            f.hits = hits;
            ind.evaluated = true;
            }
        }
//    @SuppressWarnings("deprecation")
//      public void fileReader(){
//        File file = new File("E:\\drive h\\eclipse\\ecj22\\ec\\app\\tutorial4\\elhamtrainingset.txt");
//        FileInputStream fis = null;
//        BufferedInputStream bis = null;
//        DataInputStream dis = null; 
//        try {
//          fis = new FileInputStream(file);
//
//          // Here BufferedInputStream is added for fast reading.
//          bis = new BufferedInputStream(fis);
//          dis = new DataInputStream(bis);
//
//          // dis.available() returns 0 if the file does not have more lines.
//          while (dis.available() != 0) {
//
//          // this statement reads the line from the file and print it to
//            // the console.
//        	String [] splitted = dis.readLine().split(",");
//        	String first = splitted[0];
//        	String second = splitted[1];
//        	double firstNum = Double.parseDouble(first);
//        	double secondNum =  Double.parseDouble(second);
//            System.out.println(first);
//            System.out.println(second);
//          }
//
//          // dispose all the resources after using them.
//          fis.close();
//          bis.close();
//          dis.close();
//
//        } catch (FileNotFoundException e) {
//          e.printStackTrace();
//        } catch (IOException e) {
//          e.printStackTrace();
//        }
//      }
    }

