/**
 *@author Anthony Awuley
 *GP Assignment 1
 *File processing
*/



package ec.app.fsalps.ionosphere;
import ec.EvolutionState;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.DecimalFormat;


public class DataCruncher  {
    
	static ArrayList<ArrayList> population = new ArrayList<>();
        static Pattern[] pattern = new Pattern[2];
	static Matcher[] matcher = new Matcher[2];
        public static int [] dataCount = new int [2];
        
	protected static String cleanFile(String [] regex,String rawFile, String cleanFile){
		
                String line;
		pattern[0] = Pattern.compile(regex[0]);
		pattern[1] = Pattern.compile(regex[1]);
                
          if(!(new File(cleanFile).isFile())){   //return already cleaned file if it exists
              
            try (PrintWriter printwrite = new PrintWriter(cleanFile,"UTF-8")) {
                BufferedReader f = readText(rawFile);
                int i=0;
                while((line = f.readLine()) != null)
                {
                        i=i+1;
                        matcher[0] = pattern[0].matcher(line);
                        matcher[1] = pattern[1].matcher(line);
                        if(!matcher[0].matches() && !matcher[1].matches())
                        {
                               printwrite.println(line); 
                        }
                }
                printwrite.close();
                printwrite.flush();
                f.close();
            } catch (IOException e) {
                System.out.println("File not found");
                System.exit(1);
            }
        }
            
      return cleanFile;
  } 
	
	
	private static BufferedReader readText(String filename) throws IOException{		
		return new BufferedReader(new FileReader(filename));
	}
        
        
	public static ArrayList<ArrayList> readFile(String rawFileDirectory,String regex){
		
		try
		{
			//BufferedReader f = new BufferedReader(new FileReader(rawFileDirectory));
                        BufferedReader f = readText(rawFileDirectory);
                        population.clear();
			String line;
                        //initialize counters
                        dataCount[0]=0;dataCount[1]=0;
                        
			while((line = f.readLine() ) != null)
                        {
				ArrayList<Float> dataPoint = new ArrayList<>(); 
				for(int i=0;i < line.toString().split(regex).length;i++)
				{
                                    dataPoint.add(Float.parseFloat(line.toString().split(regex)[i]));
                                } 
                              /** count records of number of entries for diabetic & non-diabetic */   
                               switch (line.toString().split(regex)[8].toString()) {
                                  case "0.0":
                                     dataCount[0]++;
                                      break;
                                  case "1.0":
                                    dataCount[1]++;
                                    break;
                               }
			    population.add(dataPoint);
			}
			f.close();
		}
		catch(IOException e)
		{
			System.out.println("Could not open file");
		}
		
		return population;
		
	}
        
        /*
	protected static ArrayList readFile(String filename){
		int i = 0;
		try
		{
                    try (BufferedReader f = readText(filename)) {
                        String line;
                        while((line = f.readLine() ) != null)
                        {
                           i+=1;
                           try
                           {	
                               xy.add(Double.parseDouble(line.split("\\s")[0]));
                               xy.add(Double.parseDouble(line.split("\\s")[1]));
                           }
                           catch(NumberFormatException e)
                           { 
                               System.out.println("Unclean data found on line: " + i + "\nCheck: " + filename);
                               System.exit(0);
                           }  
                         }
                    }
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Could not open file: "+ filename); System.exit(0);
		}
		catch(IOException e)
		{
			System.out.println("IO Exception encounted"); System.exit(0);
		}
	   return xy;
       }
       */ 
        
       protected static String createData(String filename, int numb_data, int seed,final EvolutionState state)
       {
         DecimalFormat df = new DecimalFormat("#");
         if(!(new File(filename).isFile()))
         { 
              try (PrintWriter createfile = new PrintWriter(filename, "UTF-8")) {
                    int i = 0;
                   
                     while(i<numb_data)
                     {    
                        //createfile.println(df.format((float)(Math.random() * seed) + 1) + " " + df.format((float)(Math.random() * seed) + 1));
                        createfile.println(df.format((float)(state.random[0].nextDouble()+(Math.random() * seed))) + " " + df.format((float)(state.random[0].nextDouble()+(Math.random() * seed))));
                        i++;
                     }
              }
              catch(FileNotFoundException | UnsupportedEncodingException e)
              {System.out.println("Could not write data file"); System.exit(0);}
         }
        return filename;
            
      }
       
       
       protected static ArrayList<ArrayList> shuffleData(ArrayList<ArrayList> dataSet)
       {
           
           for (int i=0;i<dataSet.size();i++)
           {
               int rand = (int)(Math.random() * dataSet.size()); //ignore possible max count for ArrayList to ensure capacity constraint
               //swap both values randomly
               ArrayList swap = dataSet.get(i);
               dataSet.set(i,dataSet.get(rand));
               dataSet.set(rand,swap);
              
           }
           return dataSet;
       }
       
       
       
       protected static void writeDataToFile(ArrayList<ArrayList> dataSet,String trainFile,String testFile){
           PrintWriter file;
           
           if(!(new File(trainFile).isFile()) &&  !(new File(testFile).isFile())){   //return already cleaned file if it exists
            try {
                
                 PrintWriter printTrain = new PrintWriter(trainFile,"UTF-8");
                 PrintWriter printTest = new PrintWriter(testFile,"UTF-8"); 
               
                  for (int i=0;i<dataSet.size();i++)
                   {
                       file = i<(dataSet.size()/2)?printTrain:printTest;
                       
                       String ct = "";
                       for(int j=0;j<dataSet.get(i).size();j++)
                       {
                           ct += dataSet.get(i).get(j)+" ";
                       }
                       file.println(ct);
                  }
                  printTrain.flush();printTrain.close();
                  printTest.flush();printTest.close();
                  
              }
               catch (IOException e) {
                 System.out.println("File could not be created"); System.exit(1);
            } 
            
           }
            
     }
       
   
       
        
}
