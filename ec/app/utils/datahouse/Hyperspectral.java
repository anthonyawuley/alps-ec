package ec.app.utils.datahouse;

import java.io.File;
import java.util.ArrayList;

import data.DataCruncher;
import data.Directory;
import data.GrayScale;
import data.Reader;

public class Hyperspectral {

	/**  minimum value of array */
	public static int minimum = 0;
	/**  maximum value of array */
	public static int maximum = 0;
	
	public static int height = 0;
	public static int width  = 0;
	
	public Hyperspectral() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 * @param directory
	 * @param regex
	 * @param filter
	 * @param filename
	 * @param delimiter
	 */
	public static ArrayList<ArrayList>  channels(String directory, String regex, String filter) {

		final File folder = new File(directory);
		Reader rd  = new Reader();
		
		ArrayList<ArrayList> dataPoints = new ArrayList<>();
		ArrayList<String> listDir;
		ArrayList<ArrayList<ArrayList>> db;
		
		listDir = Directory.listFilesForFolder(folder, filter);
		db = rd.readFileComputeArray(listDir, directory,regex);
		
		height = db.get(0).size(); //this assumes that all rows are equal
		width =  db.get(0).get(0).size(); //this assumes that all columns are equal
		
		for(int i=0;i<db.size();i++)
		{
			ArrayList<Float> row = new ArrayList<>();
			for(int j=0;j<db.get(i).size();j++)
			{
				for(int k=0;k<db.get(i).get(j).size();k++)
				{
					float pixel = Float.parseFloat((String) db.get(i).get(j).get(k));
					row.add(pixel);
					minimum = (int) Math.min(minimum, pixel);
					maximum = (int) Math.max(maximum, pixel);
				}
			}
			dataPoints.add(row);
		}
		
		//System.out.println(normalized.get(0).size() + "maximum: "+maximum + "  minimum: "+ minimum);
		//System.exit(0);
		
		return dataPoints;
	}
	
	
	/**
	 * 
	 * @param directory
	 * @param regex
	 * @param filter
	 * @return  x,y cordinates pairs
	 */
	public static ArrayList<ArrayList>  trainingPoints(String directory, String regex, String filter) {

		Reader rd  = new Reader();
		ArrayList<ArrayList> dataPoints = new ArrayList<>();
		
		ArrayList<ArrayList<ArrayList>> db = 
				rd.readFileComputeArray(Directory.listFilesForFolder(new File(directory), filter), directory,regex);
		
		for(int i=0;i<db.size();i++)
		{
			for(int j=0;j<db.get(i).size();j++)
			{
				ArrayList<Integer> row = new ArrayList<>(); //each row has x,y cordinates
				for(int k=0;k<db.get(i).get(j).size();k++)
				{
					Float pixel = new Float((float) db.get(i).get(j).get(k));
					//int pixel = (int) db.get(i).get(j).get(k);
					row.add(pixel.intValue());
				}
				dataPoints.add(row);
			}
		}
		
		return dataPoints;
	}
	
	
	@SuppressWarnings("unchecked")
	public static ArrayList<ArrayList<ArrayList>>  bands(String directory, String regex, String filter) {
		final File folder = new File(directory);
		Reader rd  = new Reader();
		
		@SuppressWarnings("rawtypes")
		ArrayList<ArrayList<ArrayList>> db =
				rd.readFileComputeArray(Directory.listFilesForFolder(folder, filter), directory,regex);;
		
        minimum = Reader.minimum;
        maximum = Reader.maximum;
        
        height = db.get(0).size(); //this assumes that all rows are equal
		width =  db.get(0).get(0).size(); //this assumes that all columns are equal
		
		for(int i=0;i<db.size();i++)
			for(int j=0;j<db.get(i).size();j++)
				for(int k=0;k<db.get(i).get(j).size();k++)
					db.get(i).get(j).set(k,normalize((float) db.get(i).get(j).get(k),Reader.minimum,Reader.maximum));
		
        return db;
		
	}
	
	/**
	 * 
	 * @param x
	 * @param min
	 * @param max
	 * @return
	 */
	public static float normalize(float x, int min,int max)
	{
		return (float)(x-min)/(max-min);
	}

	
	
	
	
	
	
	

}
