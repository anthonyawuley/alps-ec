
/**
 *@author Anthony Awuley
 *GP Assignment 1
 *File processing
 */

package ec.app.utils.datahouse;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import ec.EvolutionState;


public class DataCruncher  {

	static ArrayList<ArrayList> population = new ArrayList<>();
	static Float[][] SUMPOPULATION;
	static Pattern[] pattern = new Pattern[2];
	static Matcher[] matcher = new Matcher[2];
	
	//public static ArrayList<ArrayList> POPULATION_DATA;


	public static boolean IS_SHUFFLED = false;
	
	public static boolean KFOLD_LOCK_DOWN_SHUFFLE = false;

	/** when true, auto numbering will be attached to the newly generated file */
	static final boolean AUTO_NUMBERING = true;
	/**
	 * when true, files averaged must have equal rows. Else the system processes  maximum rows of
	 * the file with the minimum number of rows.
	 * 
	 * When false, the system processes all file content ignoring rows for which there are no data
	 * for some files. However averaging is done appropriately considering only files with available rows
	 *  */
	static final boolean PROCESS_UPTO_EQUAL_FILE_ROWS = false;
	/**
	 * 1 4 5<br>
	 * 2 6 8<br>
	 * 3 8 3<br>
	 * 4 * *<br>
	 * * * *<br>
	 * this indicates if first row should also be averaged
	 */
	static final boolean AVERAGE_FIRST_ROW = true;


	/**
	 * 
	 * @param img
	 * @param x
	 * @param y
	 * @return
	 */
	@SuppressWarnings("unused")
	private static int[] getPixelData(BufferedImage img, int x, int y) 
	{ 
		int argb = img.getRGB(x, y);

		int rgb[] = new int[] {
				(argb >> 16) & 0xff, //red
				(argb >>  8) & 0xff, //green
				(argb      ) & 0xff  //blue
		};

		return rgb;

	}




	/**
	 * Cleans file using the provided regex
	 * file is not cleaned if a cleanedFile already exits
	 * @param regex regular expression for cleaning a file
	 * @param rawFile orginal file
	 * @param cleanFile write to file
	 * @return
	 */
	public static String cleanFile(String [] regex,String rawFile, String cleanFile)
	{

		String line;
		Reader rd = new Reader();
		pattern[0] = Pattern.compile(regex[0]);
		pattern[1] = Pattern.compile(regex[1]);
		

		if(!(new File(cleanFile).isFile()))
		{ 
			try (PrintWriter printwrite = new PrintWriter(cleanFile,"UTF-8")) 
			{
				BufferedReader f = rd.readText(rawFile);
				int i=0;
				while((line = f.readLine()) != null)
				{
					i=i+1;
					matcher[0] = pattern[0].matcher(line);
					matcher[1] = pattern[1].matcher(line);
					if(!matcher[0].matches() && !matcher[1].matches())
						printwrite.println(line);
				}
			} 
			catch (IOException e) 
			{ 
				System.out.println("File not found " + e.getMessage()); 
				e.printStackTrace(); 
			} 
		}
		return cleanFile;
	}




	/**
	 * 
	 * @param str
	 * @return
	 */
	public boolean isNumeric(String str)
	{
		return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}



	/**
	 * 
	 * @param fvals
	 * @return
	 */
	public static ArrayList<ArrayList> sumArray(ArrayList<ArrayList> fvals)
	{
		for(int i=0;i<fvals.size();i++){
			ArrayList<Double> dataPoint = new ArrayList<>(); 
			for(int j=0;j<fvals.get(i).size();j++)
			{
				if(dataPoint.isEmpty())
				{
					//dataPoint.add(j, (double)(getData(dataPoint,i-1))+getData(fvals.get(i),j));
				}else
				{
					//dataPoint.set(j, (getData(dataPoint,i-1))+getData(fvals.get(i),j));
				}
				//SUMPOPULATION.set(i, fvals.get(i).get(j));
				System.out.print(dataPoint.get(j));
			} System.out.println();
		}
		return population;
	}

	/**
	 * 
	 * @param d
	 * @param i
	 * @return
	 */
	private static double getData(double[] d,int i)
	{
		try
		{ return  d[i]; }
		catch(ArrayIndexOutOfBoundsException e)
		{ return 0; }
	}


	/**
	 * 
	 * @param directories
	 * @param base_dir
	 * @param regex
	 * @return
	 */
	protected static String[] dumpConfMatrx(
			ArrayList<String> directories,String base_dir, String regex)
	{
		Pattern p,p2;
		Matcher m,m2;
		String[] storedSequence = new String[directories.size()];
		Reader rd = new Reader();
		//double [][] newAr = new double[60][4];

		try
		{
			//BufferedReader f = new BufferedReader(new FileReader(rawFileDirectory));
			for(int i=0;i<directories.size();i++)
			{ 
				BufferedReader f = rd.readText(base_dir+directories.get(i)); 
				String line;
				p2 = Pattern.compile("-?\\d+");
				p  = Pattern.compile("[-]?[0-9]*\\.?[0-9]+"); //match floats and integers
				String job_numb="";

				m2 = p2.matcher(directories.get(i).toString());
				/* get all integer values in string and use as job number */
				while (m2.find())
					job_numb += m2.group().toString()+".";

				while((line = f.readLine() ) != null)
				{
					String cmt = "";
					m = p.matcher(line.toString());

					if(line.toString().toLowerCase().contains(regex.toLowerCase()))
					{
						while (m.find()) 
							cmt = ("".equals(cmt))?m.group():cmt+ "\t" +m.group();
							storedSequence[i] = cmt;
					}
				}
				storedSequence[i] = job_numb + "\t"+storedSequence[i];
				f.close();
			}
		}
		catch(IOException e)
		{
			System.out.println("Could not open file "+e.getMessage());
		}
		return storedSequence;
	} 


	/**
	 * 
	 * @param filename
	 * @param data
	 * @param header
	 */
	protected static void createConfMatrix(String filename, String [] data,String header)
	{
		//DecimalFormat df = new DecimalFormat("#0.000000");
		try (PrintWriter createfile = new PrintWriter(filename, "UTF-8")) 
		{
			//create header
			createfile.println(header);
			for(int i=0;i<data.length;i++)
				createfile.println(data[i]);
			System.out.println("File successfully created and saved as: "+filename);
		}
		catch(FileNotFoundException | UnsupportedEncodingException e)
		{
			System.out.println("Could not write data file"+ e.getMessage()); 
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param filename
	 * @param files
	 * @param numb_files
	 * @param delimiter
	 */
	@SuppressWarnings("rawtypes")
	protected static void createData(String filename, 
			ArrayList<ArrayList<ArrayList>> files,int numb_files,String delimiter)
	{
		DecimalFormat df = new DecimalFormat("#0.00000000"); //8 decimal places
		ArrayList<ArrayList> data;
		ArrayList<String> columns;

		try (PrintWriter createfile = new PrintWriter(filename, "UTF-8")) 
		{
			data = files.get(0); //get any existing file to start processing
			for(int j=0;j<data.size();j++)
			{
				columns = data.get(j);
				String l1 = AUTO_NUMBERING?j+"":""; //decide if auto numbering must be added
				int start = AVERAGE_FIRST_ROW?0:1;
				//k=0 refers to line number: include line numbering
				for(int k=start;k<columns.size();k++) 
				{
					columns.get(k);
					String l2 = "";
					double sum = 0;
					int rowsAdded = 0;

					if(PROCESS_UPTO_EQUAL_FILE_ROWS)
					{
						for(int i=0;i<files.size();i++)
							sum += Double.parseDouble((String)files.get(i).get(j).get(k));
						l2 += delimiter.toString()+ df.format(sum/files.size());
					}
					else
					{
						for(int i=0;i<files.size();i++)
							try
						{ 
								sum += Double.parseDouble((String)files.get(i).get(j).get(k));
								rowsAdded++;
						}
						catch(IndexOutOfBoundsException e)
						{ 
							System.out.println("A File contains fewer rows than expected for the maximum number of rows"
									+ "NB: Such columns will be ignored and other available columns averaged appropriately"
									+ ": Possible IndexOutOfBoundsException"  + "raised at "  + e.getMessage());
							break; 
						}
						l2 += delimiter.toString()+ df.format(sum/rowsAdded);
					}
					l1 += l2;
				}
				createfile.println(l1);
			}
			System.out.println("File successfully created and saved as: "+filename);
		}
		catch(FileNotFoundException | UnsupportedEncodingException e)
		{
			System.out.println("Could not write data file" + e.getMessage());
			e.printStackTrace();
		}
		catch(IndexOutOfBoundsException e)
		{ 
			System.out.println("Processing haulted due to varying number of rows in the selected files \n"
					+ "File has been processed up to the number of rows for which all files have content.\n"
					+ "set the constant \"PROCESS_UPTO_EQUAL_FILE_ROWS\" to true if you'll want to average all rows\n"
					+ "and ignore files with missing rows. \n"
					+ "Error raised: IndexOutOfBoundsException "  + e.getMessage());
		}

	}



	/**
	 * @deprecated
	 * @param filename
	 * @param data
	 * @param numb_files
	 * @param det
	 */
	@Deprecated
	protected static void createDataOld(String filename, double[][] data,int numb_files,String det)
	{
		//DecimalFormat df = new DecimalFormat("#0.00000000");

		try (PrintWriter createfile = new PrintWriter(filename, "UTF-8")) 
		{
			for(int i=0;i<data.length;i++)
				createfile.println((int)data[i][0] + convertToString(data[i],numb_files,det));

			System.out.println("File successfully created and saved as: "+filename);
		}
		catch(FileNotFoundException | UnsupportedEncodingException e)
		{
			System.out.println("Could not write data file "+e.getMessage()); 
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param data
	 * @param size
	 * @param det
	 * @return
	 */
	private static String convertToString(double [] data, int size,String det)
	{
		DecimalFormat df = new DecimalFormat("#0.00000000");
		String a = "";

		for(int i=1;i<data.length; i++)
		{
			switch(det)
			{
			case "raw": // no averaging
				a += ","+data[i];
				break;
			case "avg":
			default: //perform averaging
				a += ","+df.format(data[i]/size);
				break;
			}
		}
		return a;
	}


	/**
	 * 
	 * @param dataSet
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static ArrayList<ArrayList> shuffleData
	(EvolutionState state, ArrayList<ArrayList> dataSet, boolean shuffled)
	{
		//this is to avoid continious shuffling for every individual
		//if(IS_SHUFFLED) return population;
        
		for (int i=0;i<dataSet.size();i++)
		{
			//ignore possible max count for ArrayList to ensure capacity constraint
			int rand = (int)(Math.random() * dataSet.size());
			//int rand = (int)state.random[0].nextDouble() * dataSet.size();
			//swap both values randomly
			ArrayList swap = dataSet.get(i);
			dataSet.set(i,dataSet.get(rand));
			dataSet.set(rand,swap);
		}
		IS_SHUFFLED = shuffled;
		
		KFOLD_LOCK_DOWN_SHUFFLE = true;

		return dataSet;

	}

	/**
	 * 
	 * @param dataSet
	 * @param chunksize
	 * @param start
	 * @return chunk block
	 */
	public static  ArrayList<ArrayList> 
	selectTestingChunk(ArrayList<ArrayList> dataSet, int chunkSize, int chunckNumber )
	{
		int start = chunckNumber * chunkSize;

		ArrayList<ArrayList> chunck = new ArrayList<>();
		/* extract chunk size if its larger than available block 
		if(chunkSize >(dataSet.size()-start))
			for(int i=start;i<dataSet.size();i++)
				chunck.add(dataSet.get(i));
		 */
		/*
		 * When splitting data, always add the last few that doesnt make up the total chunk to 
		 * the last chnuk sample. This way, the chunkNumber is maintained. Otherwise unevn dataSet size
		 * will always result in chunckNumber + 1
		 */
		if(chunkSize >(dataSet.size()-(start+chunkSize)))
			chunkSize += (dataSet.size()-(start+chunkSize));

		for(int i=start;i<start+chunkSize;i++)
			chunck.add(dataSet.get(i));

		return chunck;
	}


	/**
	 * 
	 * @param dataSet
	 * @param chunksize
	 * @param start
	 * @return dataSet without the chunck block
	 */
	public static  ArrayList<ArrayList> 
	selectTrainingChunk(ArrayList<ArrayList> dataSet, int chunkSize, int chunckNumber )
	{   
		int start = chunckNumber * chunkSize;
		ArrayList<ArrayList> chunck = new ArrayList<>();

		if(chunkSize >(dataSet.size()-(start+chunkSize)))
			chunkSize += (dataSet.size()-(start+chunkSize));

		for(int i=0;i<dataSet.size();i++)
			if(i<start || i>(start+chunkSize-1) )
				chunck.add(dataSet.get(i));

		return chunck;
	}


	/**
	 * Quickly read the last "n" lines of a text file
	 * http://stackoverflow.com/questions/686231/quickly-read-the-last-line-of-a-text-file
	 * 
	 * @param file
	 * @param lines
	 * @return 
	 */
	public static String tail2( File file, int lines) 
	{

		java.io.RandomAccessFile fileHandler = null;

		try 
		{
			fileHandler =  new java.io.RandomAccessFile( file, "r" );
			long fileLength = fileHandler.length() - 1;
			StringBuilder sb = new StringBuilder();
			int line = 0;

			for(long filePointer = fileLength; filePointer != -1; filePointer--){
				fileHandler.seek( filePointer );
				int readByte = fileHandler.readByte();

				if( readByte == 0xA ) 
				{
					line = line + 1;
					if (line == lines) {
						if (filePointer == fileLength) 
							continue;
						else 
							break;
					}
				} 
				else if( readByte == 0xD ) 
				{
					line = line + 1;
					if (line == lines) 
					{
						if (filePointer == fileLength - 1) 
							continue;
						else 
							break;
					}
				}
				sb.append( ( char ) readByte );
			}

			String lastLine = sb.reverse().toString();
			return lastLine;
		} 
		catch( java.io.FileNotFoundException e ) 
		{
			e.printStackTrace();
			return null;
		} 
		catch( java.io.IOException e ) 
		{
			e.printStackTrace();
			return null;
		}
		finally 
		{
			if (fileHandler != null )
				try 
			{
					fileHandler.close();
			} 
			catch (IOException e) 
			{ e.printStackTrace(); }
		}
	}


	/**
	 * 
	 * @param colourMatrix
	 * @param height
	 * @param width
	 * @param formatName
	 * @param filename
	 * @param save_type
	 */
	public static void createImage(int [][] colourMatrix,
			int height, int width,String formatName,String filename,int save_type)
	{
		Color clc;
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();

		filename = save_type==0?filename+"."+formatName:
			filename+"-"+dateFormat.format(date)+"."+formatName;

		try 
		{
			BufferedImage img = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {

					switch(colourMatrix[i][j])
					{ 
					case 1://TP:Green(0,139,69)
						clc = new Color(0,139,69); 
						break;
					case 2://TN:Black(0,0,0)
						clc = new Color(0,0,0); 
						break;
					case 3://FP:Red(238,44,44)
						clc = new Color(238,44,44); 
						break;
					case 4://FT:Yellow(238,238,0)
						clc = new Color(238,238,0); 
						break;
					default://Treat as case 4
						clc = new Color(255,255,255); 
						break;
					}
					int rgb = clc.getRGB();
					img.setRGB(j,i, rgb); 
				}
			}
			// retrieve image
			ImageIO.write(img,formatName,new File(filename));

		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

	}


}
