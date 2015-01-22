package ec.app.utils.datahouse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;


public class Reader {

	static ArrayList<ArrayList> population = new ArrayList<>();
	public static int [] dataCount = new int [2];
	
	/**
	 * 
	 * @param filename
	 * @returna
	 * @throws IOException
	 */
	protected BufferedReader readText(String filename) throws IOException
	{		
		return new BufferedReader(new FileReader(filename));
	}


	/**
	 * 
	 * @param directories
	 * @param base_dir
	 * @param regex
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public ArrayList<ArrayList<ArrayList>> readFileComputeArray(
			ArrayList<String> directories,String base_dir, String regex)
			{
		DataCruncher dc = new DataCruncher();
		
		ArrayList<ArrayList<ArrayList>> megaFiles = new ArrayList<>();
		ArrayList<ArrayList> fileCont;
		ArrayList<String> row = null;

		try
		{
			//BufferedReader f = new BufferedReader(new FileReader(rawFileDirectory));
			for(int i=0;i<directories.size();i++)
			{
				BufferedReader f = readText(base_dir+directories.get(i)); 
				fileCont = new ArrayList<>(); //get number of lines in each file
				String line;
				while((line = f.readLine() ) != null)
				{
					String[] stA = line.toString().split(regex);
					if(dc.isNumeric(stA[0])) //ignore row if it begins with a non numeric character
					{
						row = new ArrayList<>();
						for(int j=0;j<stA.length;j++)
							row.add(stA[j]);
						fileCont.add(row);
					}
				}
				megaFiles.add(fileCont);
				f.close();
			}
		}
		catch(IOException e)
		{ 
			System.out.println("Could not open file "+e.getMessage());
			e.printStackTrace();;
		}

		return megaFiles;
			}



	/**
	 * 
	 * @param directories
	 * @param base_dir
	 * @param regex
	 * @deprecated
	 * @return
	 */
	@Deprecated
	public  double[][] readFileComputeArrayOld(
			ArrayList<String> directories,String base_dir, String regex)
	{

		double [][] newAr = null;

		try
		{
			for(int i=0;i<directories.size();i++)
			{
				BufferedReader f = readText(base_dir+directories.get(i)); //System.out.println(base_dir+directories.get(i));
				//get number of lines in each file
				LineNumberReader  lnr = new LineNumberReader(new FileReader(new File(base_dir+directories.get(i))));
				lnr.skip(Long.MAX_VALUE);
				newAr = new double[100][4];
				int ct=0;
				String line;

				while((line = f.readLine() ) != null)
				{
					String[] stA = line.toString().split(regex);

					for(int j=1;j<stA.length;j++)
						newAr[Integer.parseInt(stA[0])][j]  +=  Double.parseDouble(stA[j]);
					ct++;
				}
				f.close();
			}
		}
		catch(IOException e)
		{
			System.out.println("Could not open file"); System.exit(0);
		}
		return newAr;
	}
	
	
	/**
	 * 
	 * @param rawFileDirectory
	 * @param regex
	 * @return
	 */
	public static ArrayList<ArrayList> readFile(String rawFileDirectory,String regex){

		Reader rd = new Reader();
		try
		{
			//BufferedReader f = new BufferedReader(new FileReader(rawFileDirectory));
			BufferedReader f = rd.readText(rawFileDirectory);
			population.clear();
			String line;
			//initialize counters
			dataCount[0]=0;
			dataCount[1]=0;

			while((line = f.readLine() ) != null)
			{
				ArrayList<Float> dataPoint = new ArrayList<>(); 
				for(int i=0;i < line.toString().split(regex).length;i++)
				{
					dataPoint.add(Float.parseFloat(line.toString().split(regex)[i]));
				} 
				/** count records of number of entries for diabetic & non-diabetic */   
				switch (line.toString().split(regex)
						[line.toString().split(regex).length-1].toString()) 
				{
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
			System.out.println("Could not open file "+ e.getMessage());
			e.printStackTrace();
		}

		return population;

	}
	
	
	
	/**
	 * Reads a file, line by line and replaces every occurence in string with csv
	 * @param rawFileDirectory
	 * @param csv %%class,%%tostring,%%var
	 * @param regex
	 * @return
	 */
	public ArrayList<String> readReplace(String file,String csv,String regex)
	{
		ArrayList<String> pop = new ArrayList<>();
		String [] template = csv.toString().split(regex);
		
		try
		{
			Reader rd = new Reader();
			BufferedReader f = rd.readText(file); 
			if(pop.size()>0) pop.clear();
			String line;
			while((line = f.readLine() ) != null)
			{
				boolean modified = false;
				for(String name: template)
				{
					String [] split = name.toString().split(":"); 
					String newLine = line.replaceAll(split[0], split[1]);
					if(!line.equalsIgnoreCase(newLine))
					{
						//System.out.println(newLine);
					    pop.add(newLine);
					    modified = true;
					}
					
				}
				if (!modified)
					pop.add(line);
					//System.out.println(line);
					
			}
			f.close();
		}
		catch(IOException e)
		{
			System.out.println("Could not open file " + e.getMessage());
			e.printStackTrace();
		}

		return pop;
	}
	
	
	
	
}
