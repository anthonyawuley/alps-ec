
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ec.app.utils.datahouse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 *
 * @author anthony
 */
public class Directory {
	/**
	 * 
	 */
	private static ArrayList<String> dataPoint = new ArrayList<>(); 

	/**
	 * 
	 * @param folder
	 * @param search
	 * @return
	 */
	public static ArrayList<String> listFilesForFolder(final File folder, String search) 
	{
		dataPoint.clear();
		try
		{
			for (final File fileEntry : folder.listFiles()) 
			{
				if (fileEntry.isDirectory())
					listFilesForFolder(fileEntry,search);
				else if(fileEntry.getName().toLowerCase().contains(search.toLowerCase()))
					dataPoint.add(fileEntry.getName());
			}
		}
		catch(NullPointerException e)
		{
			System.out.println("Problem reading directory. Please confirm validity of supplied directory ");
			e.printStackTrace();
		}

		return dataPoint;
	}




	/**
	 * extract n lines from below from a directory (EC)
	 * 
	 * @param filename
	 * @param files
	 * @param numbLines 
	 */
	protected static void createReadLastNLines(String dir,String filename, ArrayList<String> files,int numbLines)
	{

		try (PrintWriter createfile = new PrintWriter(dir+filename, "UTF-8")) 
		{
			for(int j=0;j<files.size();j++) 
				createfile.println(DataCruncher.tail2(new File(dir+files.get(j)), numbLines));
			System.out.println("File successfully created and saved as: "+dir+filename);
		}
		catch(FileNotFoundException | UnsupportedEncodingException e)
		{
			System.out.println("Could not write data file "+ e.getMessage());
			e.printStackTrace();
		}

	}


	public  static void fileConcatenator()
	{

	}


}
