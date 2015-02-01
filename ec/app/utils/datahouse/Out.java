
package ec.app.utils.datahouse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class Out {


	protected static String createData(String filename, int numb_data, int seed)
	{
		DecimalFormat df = new DecimalFormat("#");
		if(!(new File(filename).isFile()))
		{ 
			try (PrintWriter createfile = new PrintWriter(filename, "UTF-8")) 
			{
				int i = 0;
				while(i<numb_data)
				{    
					createfile.println(df.format((float)(Math.random() * seed) + 1) + " " + df.format((float)(Math.random() * seed) + 1));
					//createfile.println(df.format((float)(state.random[0].nextDouble()+(Math.random() * seed))) + " " + df.format((float)(state.random[0].nextDouble()+(Math.random() * seed))));
					i++;
				}
			}
			catch(FileNotFoundException | UnsupportedEncodingException e)
			{
				System.out.println("Could not write data file "+ e.getMessage()); 
				e.printStackTrace();
				System.exit(0);
			}
		}
		return filename;

	}


	/**
	 * 
	 * @param dataSet
	 * @param dataFile
	 * @param forceRewrite used to force a write operation
	 *        even if the file already exists
	 */
	public static void writeDataToFile(ArrayList<ArrayList> dataSet,String dataFile, boolean foreceRewrite){

		if (foreceRewrite) //file will be overwritten
			writeDataToFile(dataSet,dataFile);
		else if(!(new File(dataFile).isFile())) //perform write only when file already exists
			writeDataToFile(dataSet,dataFile);
		  
	}



	/**
	 * 
	 * @param dataSet
	 * @param dataFile
	 */
	public static void writeDataToFile(ArrayList<ArrayList> dataSet,String dataFile)
	{

		try 
		{
			PrintWriter printData = new PrintWriter(dataFile,"UTF-8");

			for (int i=0;i<dataSet.size();i++)
			{
				String ct = "";
				for(int j=0;j<dataSet.get(i).size();j++)
					ct += dataSet.get(i).get(j)+" ";
				printData.println(ct);
			}
			printData.flush();
			printData.close();
		}
		catch (IOException e) 
		{
			System.out.println("File could not be created " +e.getMessage()); 
			e.printStackTrace();
		} 

	}



	/**
	 * 
	 * @param dataSet
	 * @param trainFile
	 * @param testFile
	 */
	public static void writeDataToFile(ArrayList<ArrayList> dataSet,String trainFile,String testFile){
		PrintWriter file;

		if(!(new File(trainFile).isFile()) &&  !(new File(testFile).isFile()))
		{   //return already cleaned file if it exists
			try 
			{
				PrintWriter printTrain = new PrintWriter(trainFile,"UTF-8");
				PrintWriter printTest = new PrintWriter(testFile,"UTF-8"); 

				for (int i=0;i<dataSet.size();i++)
				{
					file = i<(dataSet.size()/2)?printTrain:printTest;
					String ct = "";
					for(int j=0;j<dataSet.get(i).size();j++)
						ct += dataSet.get(i).get(j)+" ";
					file.println(ct);
				}
				printTrain.flush();printTrain.close();
				printTest.flush();printTest.close();
			}
			catch (IOException e) 
			{
				System.out.println("File could not be created " +e.getMessage()); 
				e.printStackTrace();
				System.exit(1);
			} 

		}  
	}

	/**
	 * print output to a file
	 * @param dataSet
	 * @param output
	 */
	protected static void writeDataToFileSimple(ArrayList<String> dataSet,String dir, String output,String extension){
		PrintWriter file;

		if(!(new File(dir+output).isFile()))
		{  
			try 
			{   
				file = new PrintWriter(dir+output+"."+extension,"UTF-8"); 
				for (String ct: dataSet)
					file.println(ct);
				file.flush(); file.close();
			}
			catch (IOException e) 
			{
				System.out.println("File could not be created " +e.getMessage()); 
				e.printStackTrace();
				System.exit(1);
			} 
			System.out.println(dir+output+"."+extension + " successfully generated ");

		}  
	}






}
