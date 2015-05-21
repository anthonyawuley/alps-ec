/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */



package ec.app.canonical.cuprite;
import java.io.File;
import java.util.ArrayList;

import data.Directory;
import data.Reader;
import ec.simple.SimpleProblemForm;
import ec.util.*;
import ec.*;
import ec.app.canonical.cuprite.DoubleData;
import ec.app.utils.datahouse.DataCruncher;
import ec.app.utils.datahouse.Hyperspectral;
import ec.gp.*;
import ec.gp.koza.*;
import vision.IOInit;

public class Cuprite extends GPProblem implements SimpleProblemForm
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;

	/** 
	 * Declare variables that will serve as terminal set
	 * Variable names are description are as follows<br>
	 * Each terminal is selected from a specific band i.e. b1 is from band 1 etc <br>
	 */

	public float  
	b1,b2,b3,b4,b5,b6,b7,b8,b9,b10,b11,b12,b13,b14,b15,b16,b17,b18,b19,b20,
	b21,b22,b23,b24,b25,b26,b27,b28,b29,b30,b31,b32,b33,b34,b35,b36,b37,b38,b39,b40,
	b41,b42,b43,b44,b45,b46,b47,b48,b49,b50,b51,b52,b53,b54,b55,b56,b57,b58,b59,b60,
	b61,b62,b63,b64,b65,b66,b67,b68,b69,b70,b71,b72,b73,b74,b75,b76,b77,b78,b79,b80,
	b81,b82,b83,b84,b85,b86,b87,b88,b89,b90,b91,b92,b93,b94,b95,b96,b97,b98,b99,b100,
	b101,b102,b103,b104,b105,b106,b107,b108,b109,b110,b111,b112,b113,b114,b115,b116,b117,b118,b119,b120,
	b121,b122,b123,b124,b125,b126,b127,b128,b129,b130,b131,b132,b133,b134,b135,b136,b137,b138,b139,b140,
	b141,b142,b143,b144,b145,b146,b147,b148,b149,b150,b151,b152,b153,b154,b155,b156,b157,b158,b159,b160,
	b161,b162,b163,b164,b165,b166,b167,b168,b169,b170,b171,b172,b173,b174,b175,b176,b177,b178,b179,b180,
	b181,b182,b183,b184,b185,b186,b187,b188,b189,b190,b191,b192,b193,b194,b195,b196,b197,b198,b199,b200,
	b201,b202,b203,b204,b205,b206,b207,b208,b209,b210,b211,b212,b213,b214,b215,b216,b217,b218,b219,b220;



	/** directory to raw unprocessed file */
	private final String DATA_RAW     = "cuprite-raw";
	/** fetch training directory */
	private final String TRAIN_DATA   = "cuprite-training";
	
	/** directory to savel cleaned file */
	private final String DATA_GTI   = "cuprite-gti";
	private final String DATA_OUT   = "cuprite-out";

	public  static int ctcmtrx = 0;
	
	private static final int START_ROW = 20;
	private static final int START_COL = 25;
	private static final int WIDTH     = 35;
	private static final int HEIGHT    = 20;
	
	private String dataRaw, trainData;
	

	public DoubleData input;

	public Object clone()
	{
		Cuprite newobj = (Cuprite) (super.clone());
		newobj.input = (DoubleData)(input.clone());
		return newobj;
	}

	/** setup is called once per run */
	public void setup(final EvolutionState state,
			final Parameter base)
	{
		// very important, remember this
		super.setup(state,base);
		// set up our input -- don't want to use the default base, it's unsafe here
		input = (DoubleData) state.parameters.getInstanceForParameterEq(
				base.push(P_DATA), null, DoubleData.class);
		input.setup(state,base.push(P_DATA));
		dataRaw   = state.parameters.getString(base.push(DATA_RAW), null);
		trainData = state.parameters.getString(base.push(TRAIN_DATA), null);
		
		/** 
		 * 1. Reads all csv files from "dataRaw" directory with prefix "ch_"
		 * 2. for each band(channel) read all rows and columns
		 * 3. Save  all array into (float) DataCruncher.DATA
		 * 4. Lock down read process.
		 */

		if(!DataCruncher.LOCK_DOWN) 
		{  
			DataCruncher.LOCK_DOWN = true;

			
			
			/*read first set*/
			//(float) DataCruncher.DATA = Hyperspectral.channels(dataRaw+"_1/", ",", "ch_");
			/*read and append second set to first set*/
			//(float) DataCruncher.DATA.addAll(Hyperspectral.channels(dataRaw+"_2/", ",", "ch_"));
            

			DataCruncher.TRAINING_DATA = Hyperspectral.trainingPoints(trainData, ",", "corn_"); //read ground truth image (selected points)

			Directory.listFilesForFolder(new File(dataRaw), "ch_"); //read raw band text files
			
			Directory.BASE = dataRaw; //set base directory
			IOInit.train_ggl_sat_img_tgi = state.parameters.getString(base.push(DATA_GTI), null);
			IOInit.tt_a2_img             = state.parameters.getString(base.push(DATA_OUT), null);

		}

	}



	public void evaluate(final EvolutionState state, 
			final Individual ind, 
			final int subpopulation,
			final int threadnum)
	{
		//System.out.println(Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",0).size() + "Hyperspectral.minimum:"+Hyperspectral.minimum + "Hyperspectral.maximum:"+Hyperspectral.maximum);

		if (!ind.evaluated)  // don't bother reevaluating
		{
			int hits = 0;
			//int [][] confusionMatrix = new int[2][2];
			
			//for(int r=START_ROW;r<HEIGHT+START_ROW;r++){ //row for(int c=START_COL;c<WIDTH+START_COL;c++){ //column
			for(int i=0;i<DataCruncher.TRAINING_DATA.size();i++){ 
					//decoded Data Point
					b1	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",0,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b2	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",1,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b3	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",2,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b4	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",3,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b5	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",4,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b6	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",5,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b7	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",6,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b8	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",7,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b9	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",8,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b10	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",9,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b11	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",10,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b12	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",11,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b13	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",12,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b14	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",13,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b15	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",14,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b16	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",15,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b17	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",16,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b18	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",17,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b19	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",18,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b20	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",19,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b21	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",20,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b22	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",21,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b23	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",22,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b24	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",23,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b25	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",24,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b26	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",25,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b27	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",26,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b28	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",27,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b29	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",28,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b30	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",29,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b31	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",30,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b32	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",31,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b33	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",32,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b34	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",33,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b35	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",34,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b36	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",35,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b37	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",36,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b38	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",37,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b39	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",38,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b40	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",39,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b41	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",40,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b42	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",41,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b43	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",42,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b44	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",43,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b45	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",44,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b46	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",45,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b47	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",46,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b48	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",47,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b49	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",48,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b50	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",49,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b51	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",50,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b52	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",51,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b53	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",52,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b54	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",53,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b55	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",54,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b56	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",55,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b57	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",56,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b58	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",57,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b59	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",58,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b60	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",59,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b61	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",60,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b62	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",61,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b63	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",62,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b64	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",63,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b65	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",64,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b66	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",65,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b67	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",66,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b68	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",67,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b69	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",68,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b70	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",69,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b71	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",70,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b72	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",71,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b73	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",72,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b74	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",73,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b75	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",74,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b76	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",75,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b77	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",76,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b78	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",77,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b79	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",78,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b80	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",79,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b81	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",80,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b82	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",81,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b83	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",82,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b84	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",83,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b85	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",84,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b86	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",85,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b87	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",86,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b88	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",87,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b89	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",88,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b90	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",89,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b91	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",90,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b92	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",91,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b93	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",92,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b94	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",93,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b95	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",94,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b96	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",95,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b97	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",96,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b98	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",97,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b99	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",98,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					
					b100	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",99,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b101	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",100,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b102	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",101,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b103	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",102,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b104	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",103,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b105	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",104,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b106	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",105,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b107	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",106,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b108	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",107,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b109	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",108,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b110	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",109,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b111	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",110,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b112	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",111,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b113	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",112,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b114	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",113,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b115	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",114,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b116	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",115,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b117	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",116,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b118	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",117,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b119	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",118,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b120	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",119,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b121	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",120,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b122	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",121,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b123	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",122,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b124	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",123,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b125	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",124,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b126	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",125,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b127	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",126,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b128	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",127,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b129	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",128,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b130	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",129,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b131	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",130,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b132	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",131,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b133	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",132,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b134	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",133,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b135	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",134,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b136	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",135,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b137	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",136,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b138	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",137,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b139	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",138,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b140	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",139,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b141	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",140,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b142	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",141,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b143	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",142,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b144	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",143,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b145	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",144,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b146	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",145,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b147	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",146,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b148	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",147,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b149	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",148,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b150	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",149,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b151	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",150,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b152	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",151,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b153	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",152,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b154	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",153,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b155	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",154,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b156	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",155,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b157	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",156,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b158	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",157,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b159	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",158,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b160	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",159,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b161	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",160,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b162	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",161,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b163	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",162,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b164	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",163,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b165	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",164,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b166	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",165,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b167	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",166,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b168	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",167,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b169	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",168,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b170	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",169,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b171	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",170,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b172	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",171,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b173	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",172,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b174	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",173,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b175	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",174,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b176	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",175,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b177	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",176,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b178	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",177,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b179	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",178,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b180	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",179,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b181	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",180,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b182	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",181,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b183	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",182,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b184	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",183,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b185	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",184,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b186	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",185,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b187	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",186,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b188	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",187,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b189	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",188,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b190	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",189,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b191	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",190,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b192	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",191,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b193	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",192,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b194	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",193,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b195	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",194,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b196	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",195,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b197	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",196,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b198	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",197,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b199	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",198,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b200	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",199,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));

					b201	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",200,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b202	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",201,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b203	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",202,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b204	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",203,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b205	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",204,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b206	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",205,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b207	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",206,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b208	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",207,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b209	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",208,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b210	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",209,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b211	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",210,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b212	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",211,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b213	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",212,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b214	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",213,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b215	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",214,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b216	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",215,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b217	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",216,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b218	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",217,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b219	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",218,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b220	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",219,(int) DataCruncher.TRAINING_DATA.get(i).get(1),(int) DataCruncher.TRAINING_DATA.get(i).get(0));
					
					//System.out.println(b1 +"::"+ b2 + "::"+b3);
					((GPIndividual)ind).trees[0].child.eval(
							state,threadnum,input,stack,((GPIndividual)ind),this);

					//R:30, G:30, B:246
					boolean isColor = IOInit.compareTwoPixels(2,IOInit.train_ggl_sat_img_tgi,new int[]{0,0,255},(int) (int) DataCruncher.TRAINING_DATA.get(i).get(0),(int) DataCruncher.TRAINING_DATA.get(i).get(1));

					if(input.x >= 0 && isColor)  
						hits++;
				    else if(input.x < 0  && !isColor) 
				    	hits++;
				//}
			}
			// the fitness better be KozaFitness!
			KozaFitness f = ((KozaFitness)ind.fitness);
			f.setStandardizedFitness(state,1 - (float)hits/(DataCruncher.TRAINING_DATA.size())); //height*width give the total number of pixels used for training
			f.hits = hits;
			ind.evaluated = true;
		}

	}

	/** PERFORM TESTING */
	@Override
	public void describe(final EvolutionState state, 
			final Individual ind, 
			final int subpopulation,
			final int threadnum,
			final int log){

		//Directory.BASE = dataRaw;
		int hits = 0;
		int [][] confusionMatrix = new int[2][2];
		int [][] colourMatrix    = new int[Hyperspectral.height][Hyperspectral.width];

        //System.out.println(Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",0).size() + ":::"+Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",0).get(0).size()+" :::"+Hyperspectral.height+"::::"+Hyperspectral.width);
		for(int r=0;r<Hyperspectral.height;r++){ //row
			
			for(int c=0;c<Hyperspectral.width;c++){ //column
				//decoded Data Point
				    b1	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",0, r, c);
					b2	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",1, r, c);
					b3	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",2, r, c);
					b4	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",3, r, c);
					b5	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",4, r, c);
					b6	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",5, r, c);
					b7	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",6, r, c);
					b8	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",7, r, c);
					b9	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",8, r, c);
					b10	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",9, r, c);
					b11	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",10, r, c);
					b12	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",11, r, c);
					b13	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",12, r, c);
					b14	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",13, r, c);
					b15	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",14, r, c);
					b16	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",15, r, c);
					b17	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",16, r, c);
					b18	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",17, r, c);
					b19	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",18, r, c);
					b20	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",19, r, c);
					b21	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",20, r, c);
					b22	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",21, r, c);
					b23	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",22, r, c);
					b24	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",23, r, c);
					b25	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",24, r, c);
					b26	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",25, r, c);
					b27	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",26, r, c);
					b28	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",27, r, c);
					b29	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",28, r, c);
					b30	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",29, r, c);
					b31	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",30, r, c);
					b32	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",31, r, c);
					b33	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",32, r, c);
					b34	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",33, r, c);
					b35	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",34, r, c);
					b36	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",35, r, c);
					b37	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",36, r, c);
					b38	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",37, r, c);
					b39	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",38, r, c);
					b40	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",39, r, c);
					b41	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",40, r, c);
					b42	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",41, r, c);
					b43	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",42, r, c);
					b44	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",43, r, c);
					b45	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",44, r, c);
					b46	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",45, r, c);
					b47	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",46, r, c);
					b48	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",47, r, c);
					b49	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",48, r, c);
					b50	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",49, r, c);
					b51	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",50, r, c);
					b52	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",51, r, c);
					b53	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",52, r, c);
					b54	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",53, r, c);
					b55	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",54, r, c);
					b56	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",55, r, c);
					b57	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",56, r, c);
					b58	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",57, r, c);
					b59	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",58, r, c);
					b60	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",59, r, c);
					b61	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",60, r, c);
					b62	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",61, r, c);
					b63	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",62, r, c);
					b64	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",63, r, c);
					b65	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",64, r, c);
					b66	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",65, r, c);
					b67	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",66, r, c);
					b68	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",67, r, c);
					b69	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",68, r, c);
					b70	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",69, r, c);
					b71	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",70, r, c);
					b72	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",71, r, c);
					b73	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",72, r, c);
					b74	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",73, r, c);
					b75	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",74, r, c);
					b76	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",75, r, c);
					b77	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",76, r, c);
					b78	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",77, r, c);
					b79	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",78, r, c);
					b80	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",79, r, c);
					b81	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",80, r, c);
					b82	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",81, r, c);
					b83	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",82, r, c);
					b84	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",83, r, c);
					b85	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",84, r, c);
					b86	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",85, r, c);
					b87	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",86, r, c);
					b88	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",87, r, c);
					b89	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",88, r, c);
					b90	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",89, r, c);
					b91	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",90, r, c);
					b92	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",91, r, c);
					b93	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",92, r, c);
					b94	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",93, r, c);
					b95	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",94, r, c);
					b96	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",95, r, c);
					b97	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",96, r, c);
					b98	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",97, r, c);
					b99	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",98, r, c);
					
					b100	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",99, r, c);
					b101	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",100, r, c);
					b102	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",101, r, c);
					b103	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",102, r, c);
					b104	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",103, r, c);
					b105	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",104, r, c);
					b106	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",105, r, c);
					b107	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",106, r, c);
					b108	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",107, r, c);
					b109	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",108, r, c);
					b110	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",109, r, c);
					b111	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",110, r, c);
					b112	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",111, r, c);
					b113	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",112, r, c);
					b114	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",113, r, c);
					b115	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",114, r, c);
					b116	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",115, r, c);
					b117	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",116, r, c);
					b118	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",117, r, c);
					b119	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",118, r, c);
					b120	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",119, r, c);
					b121	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",120, r, c);
					b122	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",121, r, c);
					b123	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",122, r, c);
					b124	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",123, r, c);
					b125	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",124, r, c);
					b126	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",125, r, c);
					b127	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",126, r, c);
					b128	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",127, r, c);
					b129	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",128, r, c);
					b130	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",129, r, c);
					b131	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",130, r, c);
					b132	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",131, r, c);
					b133	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",132, r, c);
					b134	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",133, r, c);
					b135	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",134, r, c);
					b136	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",135, r, c);
					b137	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",136, r, c);
					b138	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",137, r, c);
					b139	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",138, r, c);
					b140	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",139, r, c);
					b141	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",140, r, c);
					b142	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",141, r, c);
					b143	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",142, r, c);
					b144	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",143, r, c);
					b145	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",144, r, c);
					b146	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",145, r, c);
					b147	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",146, r, c);
					b148	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",147, r, c);
					b149	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",148, r, c);
					b150	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",149, r, c);
					b151	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",150, r, c);
					b152	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",151, r, c);
					b153	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",152, r, c);
					b154	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",153, r, c);
					b155	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",154, r, c);
					b156	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",155, r, c);
					b157	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",156, r, c);
					b158	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",157, r, c);
					b159	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",158, r, c);
					b160	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",159, r, c);
					b161	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",160, r, c);
					b162	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",161, r, c);
					b163	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",162, r, c);
					b164	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",163, r, c);
					b165	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",164, r, c);
					b166	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",165, r, c);
					b167	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",166, r, c);
					b168	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",167, r, c);
					b169	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",168, r, c);
					b170	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",169, r, c);
					b171	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",170, r, c);
					b172	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",171, r, c);
					b173	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",172, r, c);
					b174	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",173, r, c);
					b175	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",174, r, c);
					b176	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",175, r, c);
					b177	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",176, r, c);
					b178	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",177, r, c);
					b179	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",178, r, c);
					b180	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",179, r, c);
					b181	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",180, r, c);
					b182	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",181, r, c);
					b183	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",182, r, c);
					b184	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",183, r, c);
					b185	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",184, r, c);
					b186	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",185, r, c);
					b187	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",186, r, c);
					b188	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",187, r, c);
					b189	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",188, r, c);
					b190	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",189, r, c);
					b191	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",190, r, c);
					b192	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",191, r, c);
					b193	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",192, r, c);
					b194	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",193, r, c);
					b195	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",194, r, c);
					b196	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",195, r, c);
					b197	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",196, r, c);
					b198	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",197, r, c);
					b199	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",198, r, c);
					b200	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",199, r, c);

					b201	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",200, r, c);
					b202	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",201, r, c);
					b203	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",202, r, c);
					b204	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",203, r, c);
					b205	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",204, r, c);
					b206	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",205, r, c);
					b207	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",206, r, c);
					b208	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",207, r, c);
					b209	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",208, r, c);
					b210	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",209, r, c);
					b211	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",210, r, c);
					b212	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",211, r, c);
					b213	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",212, r, c);
					b214	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",213, r, c);
					b215	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",214, r, c);
					b216	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",215, r, c);
					b217	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",216, r, c);
					b218	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",217, r, c);
					b219	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",218, r, c);
					b220	=	Reader.readElementCSV(Directory.DIRECTORIES,Directory.BASE, ",",219, r, c);
					

				((GPIndividual)ind).trees[0].child.eval(
						state,threadnum,input,stack,((GPIndividual)ind),this);

				boolean isColor = IOInit.compareTwoPixels(2,IOInit.train_ggl_sat_img_tgi,new int[]{0,0,255},c,r);

				if(input.x >= 0)
				{
					if(isColor) 
					{ confusionMatrix[0][0]++;colourMatrix[r][c]=1;hits++;}
					else        
					{ confusionMatrix[1][0]++;colourMatrix[r][c]=3;}
				}
				else
				{
					if(isColor) 
					{ confusionMatrix[1][1]++;colourMatrix[r][c]=4;}
					else        
					{ confusionMatrix[0][1]++;colourMatrix[r][c]=2;hits++;}
				}
			}
		}
		// the fitness better be KozaFitness!
		KozaFitness f = ((KozaFitness)ind.fitness);
		f.setStandardizedFitness(state,1 - (float) hits/(Hyperspectral.height * Hyperspectral.width));
		f.hits = hits;
		ind.evaluated = true;

	
		//GENERATE CONFUSION MATRIX image
        IOInit.createImage(colourMatrix,colourMatrix.length,colourMatrix[1].length,"png",IOInit.tt_a2_img+"job."+(int)state.job[0],0);
        
        //GENERATE CONFUSION MATRIX image     
        state.output.println("\n\n"
        		           + "TP: "+confusionMatrix[0][0] + "\tTN: "+confusionMatrix[0][1]+"\t"
                           + "FP: "+confusionMatrix[1][0] + "\tFN: "+confusionMatrix[1][1]
                           + "\nTOTAL POSITIVE: "     + (confusionMatrix[0][0]+confusionMatrix[1][1])
                           + "\tTOTAL NEGATIVE: " + (Hyperspectral.height * Hyperspectral.width - (confusionMatrix[0][0]+confusionMatrix[1][1])),log);
         
	}//end describe


}

