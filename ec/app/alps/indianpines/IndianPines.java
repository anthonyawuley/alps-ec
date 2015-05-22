/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */



package ec.app.alps.indianpines;
import java.util.ArrayList;

import ec.simple.SimpleProblemForm;
import ec.util.*;
import ec.*;
import ec.app.alps.indianpines.DoubleData;
import ec.app.utils.datahouse.DataCruncher;
import ec.app.utils.datahouse.Hyperspectral;
import ec.gp.*;
import ec.gp.koza.*;
import vision.IOInit;

public class IndianPines extends GPProblem implements SimpleProblemForm
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
	b181,b182,b183,b184,b185,b186,b187,b188,b189,b190,b191,b192,b193,b194,b195,b196,b197,b198,b199,b200;



	/** directory to raw unprocessed file */
	private final String DATA_RAW     = "pines-raw";
	/** fetch training directory */
	private final String TRAIN_DATA   = "pines-training";
	
	/** directory to savel cleaned file */
	private final String DATA_GTI   = "pines-gti";
	private final String DATA_OUT   = "pines-out";
	private final String TARGET_COLOR = "target-color";
	private final String TRAIN_REGEX  = "train-regex";
	/**store color channels for target pixels*/
	private String[] targetColor;

	public  static int ctcmtrx = 0;
	
	private static final int START_ROW = 20;
	private static final int START_COL = 25;
	private static final int WIDTH     = 35;
	private static final int HEIGHT    = 20;

	public DoubleData input;

	public Object clone()
	{
		IndianPines newobj = (IndianPines) (super.clone());
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
		String dataRaw   = state.parameters.getString(base.push(DATA_RAW), null);
		String trainData = state.parameters.getString(base.push(TRAIN_DATA), null);
		/*extract target color channels */
		targetColor      = state.parameters.getString(base.push(TARGET_COLOR), null).split(",");
		
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
            
			DataCruncher.DATA          = Hyperspectral.bands(dataRaw, ",", "ch_");
			DataCruncher.TRAINING_DATA = Hyperspectral.trainingPoints(trainData, ",", 
					state.parameters.getString(base.push(TRAIN_REGEX), null));

			//DataCruncher.TRAINING_DATA = DataCruncher.shuffleData(state,DataCruncher.TRAINING_DATA,true);
			
			IOInit.train_ggl_sat_img_tgi = state.parameters.getString(base.push(DATA_GTI), null);
			IOInit.tt_a2_img             = state.parameters.getString(base.push(DATA_OUT), null);

		}

	}



	public void evaluate(final EvolutionState state, 
			final Individual ind, 
			final int subpopulation,
			final int threadnum)
	{
		//System.out.println((float) DataCruncher.DATA.get(0).size() + "Hyperspectral.minimum:"+Hyperspectral.minimum + "Hyperspectral.maximum:"+Hyperspectral.maximum);

		if (!ind.evaluated)  // don't bother reevaluating
		{
			int hits = 0;
			//int [][] confusionMatrix = new int[2][2];

			//for(int r=START_ROW;r<HEIGHT+START_ROW;r++){ //row for(int c=START_COL;c<WIDTH+START_COL;c++){ //column
			for(int i=0;i<DataCruncher.TRAINING_DATA.size();i++){ 
					//decoded Data Point
					b1	=	(float) DataCruncher.DATA.get(0).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b2	=	(float) DataCruncher.DATA.get(1).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b3	=	(float) DataCruncher.DATA.get(2).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b4	=	(float) DataCruncher.DATA.get(3).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b5	=	(float) DataCruncher.DATA.get(4).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b6	=	(float) DataCruncher.DATA.get(5).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b7	=	(float) DataCruncher.DATA.get(6).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b8	=	(float) DataCruncher.DATA.get(7).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b9	=	(float) DataCruncher.DATA.get(8).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b10	=	(float) DataCruncher.DATA.get(9).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b11	=	(float) DataCruncher.DATA.get(10).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b12	=	(float) DataCruncher.DATA.get(11).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b13	=	(float) DataCruncher.DATA.get(12).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b14	=	(float) DataCruncher.DATA.get(13).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b15	=	(float) DataCruncher.DATA.get(14).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b16	=	(float) DataCruncher.DATA.get(15).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b17	=	(float) DataCruncher.DATA.get(16).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b18	=	(float) DataCruncher.DATA.get(17).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b19	=	(float) DataCruncher.DATA.get(18).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b20	=	(float) DataCruncher.DATA.get(19).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b21	=	(float) DataCruncher.DATA.get(20).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b22	=	(float) DataCruncher.DATA.get(21).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b23	=	(float) DataCruncher.DATA.get(22).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b24	=	(float) DataCruncher.DATA.get(23).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b25	=	(float) DataCruncher.DATA.get(24).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b26	=	(float) DataCruncher.DATA.get(25).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b27	=	(float) DataCruncher.DATA.get(26).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b28	=	(float) DataCruncher.DATA.get(27).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b29	=	(float) DataCruncher.DATA.get(28).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b30	=	(float) DataCruncher.DATA.get(29).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b31	=	(float) DataCruncher.DATA.get(30).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b32	=	(float) DataCruncher.DATA.get(31).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b33	=	(float) DataCruncher.DATA.get(32).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b34	=	(float) DataCruncher.DATA.get(33).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b35	=	(float) DataCruncher.DATA.get(34).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b36	=	(float) DataCruncher.DATA.get(35).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b37	=	(float) DataCruncher.DATA.get(36).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b38	=	(float) DataCruncher.DATA.get(37).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b39	=	(float) DataCruncher.DATA.get(38).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b40	=	(float) DataCruncher.DATA.get(39).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b41	=	(float) DataCruncher.DATA.get(40).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b42	=	(float) DataCruncher.DATA.get(41).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b43	=	(float) DataCruncher.DATA.get(42).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b44	=	(float) DataCruncher.DATA.get(43).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b45	=	(float) DataCruncher.DATA.get(44).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b46	=	(float) DataCruncher.DATA.get(45).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b47	=	(float) DataCruncher.DATA.get(46).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b48	=	(float) DataCruncher.DATA.get(47).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b49	=	(float) DataCruncher.DATA.get(48).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b50	=	(float) DataCruncher.DATA.get(49).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b51	=	(float) DataCruncher.DATA.get(50).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b52	=	(float) DataCruncher.DATA.get(51).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b53	=	(float) DataCruncher.DATA.get(52).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b54	=	(float) DataCruncher.DATA.get(53).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b55	=	(float) DataCruncher.DATA.get(54).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b56	=	(float) DataCruncher.DATA.get(55).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b57	=	(float) DataCruncher.DATA.get(56).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b58	=	(float) DataCruncher.DATA.get(57).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b59	=	(float) DataCruncher.DATA.get(58).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b60	=	(float) DataCruncher.DATA.get(59).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b61	=	(float) DataCruncher.DATA.get(60).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b62	=	(float) DataCruncher.DATA.get(61).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b63	=	(float) DataCruncher.DATA.get(62).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b64	=	(float) DataCruncher.DATA.get(63).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b65	=	(float) DataCruncher.DATA.get(64).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b66	=	(float) DataCruncher.DATA.get(65).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b67	=	(float) DataCruncher.DATA.get(66).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b68	=	(float) DataCruncher.DATA.get(67).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b69	=	(float) DataCruncher.DATA.get(68).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b70	=	(float) DataCruncher.DATA.get(69).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b71	=	(float) DataCruncher.DATA.get(70).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b72	=	(float) DataCruncher.DATA.get(71).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b73	=	(float) DataCruncher.DATA.get(72).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b74	=	(float) DataCruncher.DATA.get(73).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b75	=	(float) DataCruncher.DATA.get(74).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b76	=	(float) DataCruncher.DATA.get(75).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b77	=	(float) DataCruncher.DATA.get(76).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b78	=	(float) DataCruncher.DATA.get(77).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b79	=	(float) DataCruncher.DATA.get(78).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b80	=	(float) DataCruncher.DATA.get(79).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b81	=	(float) DataCruncher.DATA.get(80).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b82	=	(float) DataCruncher.DATA.get(81).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b83	=	(float) DataCruncher.DATA.get(82).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b84	=	(float) DataCruncher.DATA.get(83).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b85	=	(float) DataCruncher.DATA.get(84).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b86	=	(float) DataCruncher.DATA.get(85).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b87	=	(float) DataCruncher.DATA.get(86).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b88	=	(float) DataCruncher.DATA.get(87).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b89	=	(float) DataCruncher.DATA.get(88).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b90	=	(float) DataCruncher.DATA.get(89).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b91	=	(float) DataCruncher.DATA.get(90).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b92	=	(float) DataCruncher.DATA.get(91).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b93	=	(float) DataCruncher.DATA.get(92).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b94	=	(float) DataCruncher.DATA.get(93).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b95	=	(float) DataCruncher.DATA.get(94).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b96	=	(float) DataCruncher.DATA.get(95).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b97	=	(float) DataCruncher.DATA.get(96).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b98	=	(float) DataCruncher.DATA.get(97).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b99	=	(float) DataCruncher.DATA.get(98).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b100	=	(float) DataCruncher.DATA.get(99).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b101	=	(float) DataCruncher.DATA.get(100).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b102	=	(float) DataCruncher.DATA.get(101).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b103	=	(float) DataCruncher.DATA.get(102).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b104	=	(float) DataCruncher.DATA.get(103).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b105	=	(float) DataCruncher.DATA.get(104).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b106	=	(float) DataCruncher.DATA.get(105).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b107	=	(float) DataCruncher.DATA.get(106).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b108	=	(float) DataCruncher.DATA.get(107).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b109	=	(float) DataCruncher.DATA.get(108).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b110	=	(float) DataCruncher.DATA.get(109).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b111	=	(float) DataCruncher.DATA.get(110).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b112	=	(float) DataCruncher.DATA.get(111).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b113	=	(float) DataCruncher.DATA.get(112).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b114	=	(float) DataCruncher.DATA.get(113).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b115	=	(float) DataCruncher.DATA.get(114).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b116	=	(float) DataCruncher.DATA.get(115).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b117	=	(float) DataCruncher.DATA.get(116).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b118	=	(float) DataCruncher.DATA.get(117).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b119	=	(float) DataCruncher.DATA.get(118).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b120	=	(float) DataCruncher.DATA.get(119).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b121	=	(float) DataCruncher.DATA.get(120).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b122	=	(float) DataCruncher.DATA.get(121).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b123	=	(float) DataCruncher.DATA.get(122).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b124	=	(float) DataCruncher.DATA.get(123).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b125	=	(float) DataCruncher.DATA.get(124).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b126	=	(float) DataCruncher.DATA.get(125).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b127	=	(float) DataCruncher.DATA.get(126).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b128	=	(float) DataCruncher.DATA.get(127).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b129	=	(float) DataCruncher.DATA.get(128).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b130	=	(float) DataCruncher.DATA.get(129).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b131	=	(float) DataCruncher.DATA.get(130).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b132	=	(float) DataCruncher.DATA.get(131).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b133	=	(float) DataCruncher.DATA.get(132).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b134	=	(float) DataCruncher.DATA.get(133).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b135	=	(float) DataCruncher.DATA.get(134).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b136	=	(float) DataCruncher.DATA.get(135).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b137	=	(float) DataCruncher.DATA.get(136).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b138	=	(float) DataCruncher.DATA.get(137).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b139	=	(float) DataCruncher.DATA.get(138).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b140	=	(float) DataCruncher.DATA.get(139).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b141	=	(float) DataCruncher.DATA.get(140).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b142	=	(float) DataCruncher.DATA.get(141).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b143	=	(float) DataCruncher.DATA.get(142).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b144	=	(float) DataCruncher.DATA.get(143).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b145	=	(float) DataCruncher.DATA.get(144).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b146	=	(float) DataCruncher.DATA.get(145).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b147	=	(float) DataCruncher.DATA.get(146).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b148	=	(float) DataCruncher.DATA.get(147).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b149	=	(float) DataCruncher.DATA.get(148).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b150	=	(float) DataCruncher.DATA.get(149).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b151	=	(float) DataCruncher.DATA.get(150).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b152	=	(float) DataCruncher.DATA.get(151).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b153	=	(float) DataCruncher.DATA.get(152).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b154	=	(float) DataCruncher.DATA.get(153).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b155	=	(float) DataCruncher.DATA.get(154).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b156	=	(float) DataCruncher.DATA.get(155).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b157	=	(float) DataCruncher.DATA.get(156).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b158	=	(float) DataCruncher.DATA.get(157).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b159	=	(float) DataCruncher.DATA.get(158).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b160	=	(float) DataCruncher.DATA.get(159).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b161	=	(float) DataCruncher.DATA.get(160).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b162	=	(float) DataCruncher.DATA.get(161).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b163	=	(float) DataCruncher.DATA.get(162).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b164	=	(float) DataCruncher.DATA.get(163).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b165	=	(float) DataCruncher.DATA.get(164).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b166	=	(float) DataCruncher.DATA.get(165).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b167	=	(float) DataCruncher.DATA.get(166).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b168	=	(float) DataCruncher.DATA.get(167).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b169	=	(float) DataCruncher.DATA.get(168).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b170	=	(float) DataCruncher.DATA.get(169).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b171	=	(float) DataCruncher.DATA.get(170).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b172	=	(float) DataCruncher.DATA.get(171).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b173	=	(float) DataCruncher.DATA.get(172).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b174	=	(float) DataCruncher.DATA.get(173).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b175	=	(float) DataCruncher.DATA.get(174).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b176	=	(float) DataCruncher.DATA.get(175).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b177	=	(float) DataCruncher.DATA.get(176).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b178	=	(float) DataCruncher.DATA.get(177).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b179	=	(float) DataCruncher.DATA.get(178).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b180	=	(float) DataCruncher.DATA.get(179).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b181	=	(float) DataCruncher.DATA.get(180).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b182	=	(float) DataCruncher.DATA.get(181).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b183	=	(float) DataCruncher.DATA.get(182).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b184	=	(float) DataCruncher.DATA.get(183).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b185	=	(float) DataCruncher.DATA.get(184).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b186	=	(float) DataCruncher.DATA.get(185).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b187	=	(float) DataCruncher.DATA.get(186).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b188	=	(float) DataCruncher.DATA.get(187).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b189	=	(float) DataCruncher.DATA.get(188).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b190	=	(float) DataCruncher.DATA.get(189).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b191	=	(float) DataCruncher.DATA.get(190).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b192	=	(float) DataCruncher.DATA.get(191).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b193	=	(float) DataCruncher.DATA.get(192).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b194	=	(float) DataCruncher.DATA.get(193).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b195	=	(float) DataCruncher.DATA.get(194).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b196	=	(float) DataCruncher.DATA.get(195).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b197	=	(float) DataCruncher.DATA.get(196).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b198	=	(float) DataCruncher.DATA.get(197).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b199	=	(float) DataCruncher.DATA.get(198).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));
					b200	=	(float) DataCruncher.DATA.get(199).get((int) DataCruncher.TRAINING_DATA.get(i).get(1)).get((int) DataCruncher.TRAINING_DATA.get(i).get(0));

					//System.out.println(b1 +"::"+ b2 + "::"+b3);
					((GPIndividual)ind).trees[0].child.eval(
							state,threadnum,input,stack,((GPIndividual)ind),this);
					//R:30, G:30, B:246
					boolean isColor = IOInit.compareTwoPixels(2,IOInit.train_ggl_sat_img_tgi,
							new int[]{Integer.parseInt(targetColor[0]),Integer.parseInt(targetColor[1]),Integer.parseInt(targetColor[2])},
							(int) DataCruncher.TRAINING_DATA.get(i).get(0),(int) DataCruncher.TRAINING_DATA.get(i).get(1));

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

		int hits = 0;
		int [][] confusionMatrix = new int[2][2];
		int [][] colourMatrix    = new int[Hyperspectral.height][Hyperspectral.width];

        //System.out.println((float) DataCruncher.DATA.get(0).size() + ":::"+(float) DataCruncher.DATA.get(0).get(0).size()+" :::"+Hyperspectral.height+"::::"+Hyperspectral.width);
		for(int r=0;r<Hyperspectral.height;r++){ //row
			
			for(int c=0;c<Hyperspectral.width;c++){ //column
				//decoded Data Point
				b1	=	(float) DataCruncher.DATA.get(0).get(r).get(c);
				b2	=	(float) DataCruncher.DATA.get(1).get(r).get(c);
				b3	=	(float) DataCruncher.DATA.get(2).get(r).get(c);
				b4	=	(float) DataCruncher.DATA.get(3).get(r).get(c);
				b5	=	(float) DataCruncher.DATA.get(4).get(r).get(c);
				b6	=	(float) DataCruncher.DATA.get(5).get(r).get(c);
				b7	=	(float) DataCruncher.DATA.get(6).get(r).get(c);
				b8	=	(float) DataCruncher.DATA.get(7).get(r).get(c);
				b9	=	(float) DataCruncher.DATA.get(8).get(r).get(c);
				b10	=	(float) DataCruncher.DATA.get(9).get(r).get(c);
				b11	=	(float) DataCruncher.DATA.get(10).get(r).get(c);
				b12	=	(float) DataCruncher.DATA.get(11).get(r).get(c);
				b13	=	(float) DataCruncher.DATA.get(12).get(r).get(c);
				b14	=	(float) DataCruncher.DATA.get(13).get(r).get(c);
				b15	=	(float) DataCruncher.DATA.get(14).get(r).get(c);
				b16	=	(float) DataCruncher.DATA.get(15).get(r).get(c);
				b17	=	(float) DataCruncher.DATA.get(16).get(r).get(c);
				b18	=	(float) DataCruncher.DATA.get(17).get(r).get(c);
				b19	=	(float) DataCruncher.DATA.get(18).get(r).get(c);
				b20	=	(float) DataCruncher.DATA.get(19).get(r).get(c);
				b21	=	(float) DataCruncher.DATA.get(20).get(r).get(c);
				b22	=	(float) DataCruncher.DATA.get(21).get(r).get(c);
				b23	=	(float) DataCruncher.DATA.get(22).get(r).get(c);
				b24	=	(float) DataCruncher.DATA.get(23).get(r).get(c);
				b25	=	(float) DataCruncher.DATA.get(24).get(r).get(c);
				b26	=	(float) DataCruncher.DATA.get(25).get(r).get(c);
				b27	=	(float) DataCruncher.DATA.get(26).get(r).get(c);
				b28	=	(float) DataCruncher.DATA.get(27).get(r).get(c);
				b29	=	(float) DataCruncher.DATA.get(28).get(r).get(c);
				b30	=	(float) DataCruncher.DATA.get(29).get(r).get(c);
				b31	=	(float) DataCruncher.DATA.get(30).get(r).get(c);
				b32	=	(float) DataCruncher.DATA.get(31).get(r).get(c);
				b33	=	(float) DataCruncher.DATA.get(32).get(r).get(c);
				b34	=	(float) DataCruncher.DATA.get(33).get(r).get(c);
				b35	=	(float) DataCruncher.DATA.get(34).get(r).get(c);
				b36	=	(float) DataCruncher.DATA.get(35).get(r).get(c);
				b37	=	(float) DataCruncher.DATA.get(36).get(r).get(c);
				b38	=	(float) DataCruncher.DATA.get(37).get(r).get(c);
				b39	=	(float) DataCruncher.DATA.get(38).get(r).get(c);
				b40	=	(float) DataCruncher.DATA.get(39).get(r).get(c);
				b41	=	(float) DataCruncher.DATA.get(40).get(r).get(c);
				b42	=	(float) DataCruncher.DATA.get(41).get(r).get(c);
				b43	=	(float) DataCruncher.DATA.get(42).get(r).get(c);
				b44	=	(float) DataCruncher.DATA.get(43).get(r).get(c);
				b45	=	(float) DataCruncher.DATA.get(44).get(r).get(c);
				b46	=	(float) DataCruncher.DATA.get(45).get(r).get(c);
				b47	=	(float) DataCruncher.DATA.get(46).get(r).get(c);
				b48	=	(float) DataCruncher.DATA.get(47).get(r).get(c);
				b49	=	(float) DataCruncher.DATA.get(48).get(r).get(c);
				b50	=	(float) DataCruncher.DATA.get(49).get(r).get(c);
				b51	=	(float) DataCruncher.DATA.get(50).get(r).get(c);
				b52	=	(float) DataCruncher.DATA.get(51).get(r).get(c);
				b53	=	(float) DataCruncher.DATA.get(52).get(r).get(c);
				b54	=	(float) DataCruncher.DATA.get(53).get(r).get(c);
				b55	=	(float) DataCruncher.DATA.get(54).get(r).get(c);
				b56	=	(float) DataCruncher.DATA.get(55).get(r).get(c);
				b57	=	(float) DataCruncher.DATA.get(56).get(r).get(c);
				b58	=	(float) DataCruncher.DATA.get(57).get(r).get(c);
				b59	=	(float) DataCruncher.DATA.get(58).get(r).get(c);
				b60	=	(float) DataCruncher.DATA.get(59).get(r).get(c);
				b61	=	(float) DataCruncher.DATA.get(60).get(r).get(c);
				b62	=	(float) DataCruncher.DATA.get(61).get(r).get(c);
				b63	=	(float) DataCruncher.DATA.get(62).get(r).get(c);
				b64	=	(float) DataCruncher.DATA.get(63).get(r).get(c);
				b65	=	(float) DataCruncher.DATA.get(64).get(r).get(c);
				b66	=	(float) DataCruncher.DATA.get(65).get(r).get(c);
				b67	=	(float) DataCruncher.DATA.get(66).get(r).get(c);
				b68	=	(float) DataCruncher.DATA.get(67).get(r).get(c);
				b69	=	(float) DataCruncher.DATA.get(68).get(r).get(c);
				b70	=	(float) DataCruncher.DATA.get(69).get(r).get(c);
				b71	=	(float) DataCruncher.DATA.get(70).get(r).get(c);
				b72	=	(float) DataCruncher.DATA.get(71).get(r).get(c);
				b73	=	(float) DataCruncher.DATA.get(72).get(r).get(c);
				b74	=	(float) DataCruncher.DATA.get(73).get(r).get(c);
				b75	=	(float) DataCruncher.DATA.get(74).get(r).get(c);
				b76	=	(float) DataCruncher.DATA.get(75).get(r).get(c);
				b77	=	(float) DataCruncher.DATA.get(76).get(r).get(c);
				b78	=	(float) DataCruncher.DATA.get(77).get(r).get(c);
				b79	=	(float) DataCruncher.DATA.get(78).get(r).get(c);
				b80	=	(float) DataCruncher.DATA.get(79).get(r).get(c);
				b81	=	(float) DataCruncher.DATA.get(80).get(r).get(c);
				b82	=	(float) DataCruncher.DATA.get(81).get(r).get(c);
				b83	=	(float) DataCruncher.DATA.get(82).get(r).get(c);
				b84	=	(float) DataCruncher.DATA.get(83).get(r).get(c);
				b85	=	(float) DataCruncher.DATA.get(84).get(r).get(c);
				b86	=	(float) DataCruncher.DATA.get(85).get(r).get(c);
				b87	=	(float) DataCruncher.DATA.get(86).get(r).get(c);
				b88	=	(float) DataCruncher.DATA.get(87).get(r).get(c);
				b89	=	(float) DataCruncher.DATA.get(88).get(r).get(c);
				b90	=	(float) DataCruncher.DATA.get(89).get(r).get(c);
				b91	=	(float) DataCruncher.DATA.get(90).get(r).get(c);
				b92	=	(float) DataCruncher.DATA.get(91).get(r).get(c);
				b93	=	(float) DataCruncher.DATA.get(92).get(r).get(c);
				b94	=	(float) DataCruncher.DATA.get(93).get(r).get(c);
				b95	=	(float) DataCruncher.DATA.get(94).get(r).get(c);
				b96	=	(float) DataCruncher.DATA.get(95).get(r).get(c);
				b97	=	(float) DataCruncher.DATA.get(96).get(r).get(c);
				b98	=	(float) DataCruncher.DATA.get(97).get(r).get(c);
				b99	=	(float) DataCruncher.DATA.get(98).get(r).get(c);
				b100	=	(float) DataCruncher.DATA.get(99).get(r).get(c);
				b101	=	(float) DataCruncher.DATA.get(100).get(r).get(c);
				b102	=	(float) DataCruncher.DATA.get(101).get(r).get(c);
				b103	=	(float) DataCruncher.DATA.get(102).get(r).get(c);
				b104	=	(float) DataCruncher.DATA.get(103).get(r).get(c);
				b105	=	(float) DataCruncher.DATA.get(104).get(r).get(c);
				b106	=	(float) DataCruncher.DATA.get(105).get(r).get(c);
				b107	=	(float) DataCruncher.DATA.get(106).get(r).get(c);
				b108	=	(float) DataCruncher.DATA.get(107).get(r).get(c);
				b109	=	(float) DataCruncher.DATA.get(108).get(r).get(c);
				b110	=	(float) DataCruncher.DATA.get(109).get(r).get(c);
				b111	=	(float) DataCruncher.DATA.get(110).get(r).get(c);
				b112	=	(float) DataCruncher.DATA.get(111).get(r).get(c);
				b113	=	(float) DataCruncher.DATA.get(112).get(r).get(c);
				b114	=	(float) DataCruncher.DATA.get(113).get(r).get(c);
				b115	=	(float) DataCruncher.DATA.get(114).get(r).get(c);
				b116	=	(float) DataCruncher.DATA.get(115).get(r).get(c);
				b117	=	(float) DataCruncher.DATA.get(116).get(r).get(c);
				b118	=	(float) DataCruncher.DATA.get(117).get(r).get(c);
				b119	=	(float) DataCruncher.DATA.get(118).get(r).get(c);
				b120	=	(float) DataCruncher.DATA.get(119).get(r).get(c);
				b121	=	(float) DataCruncher.DATA.get(120).get(r).get(c);
				b122	=	(float) DataCruncher.DATA.get(121).get(r).get(c);
				b123	=	(float) DataCruncher.DATA.get(122).get(r).get(c);
				b124	=	(float) DataCruncher.DATA.get(123).get(r).get(c);
				b125	=	(float) DataCruncher.DATA.get(124).get(r).get(c);
				b126	=	(float) DataCruncher.DATA.get(125).get(r).get(c);
				b127	=	(float) DataCruncher.DATA.get(126).get(r).get(c);
				b128	=	(float) DataCruncher.DATA.get(127).get(r).get(c);
				b129	=	(float) DataCruncher.DATA.get(128).get(r).get(c);
				b130	=	(float) DataCruncher.DATA.get(129).get(r).get(c);
				b131	=	(float) DataCruncher.DATA.get(130).get(r).get(c);
				b132	=	(float) DataCruncher.DATA.get(131).get(r).get(c);
				b133	=	(float) DataCruncher.DATA.get(132).get(r).get(c);
				b134	=	(float) DataCruncher.DATA.get(133).get(r).get(c);
				b135	=	(float) DataCruncher.DATA.get(134).get(r).get(c);
				b136	=	(float) DataCruncher.DATA.get(135).get(r).get(c);
				b137	=	(float) DataCruncher.DATA.get(136).get(r).get(c);
				b138	=	(float) DataCruncher.DATA.get(137).get(r).get(c);
				b139	=	(float) DataCruncher.DATA.get(138).get(r).get(c);
				b140	=	(float) DataCruncher.DATA.get(139).get(r).get(c);
				b141	=	(float) DataCruncher.DATA.get(140).get(r).get(c);
				b142	=	(float) DataCruncher.DATA.get(141).get(r).get(c);
				b143	=	(float) DataCruncher.DATA.get(142).get(r).get(c);
				b144	=	(float) DataCruncher.DATA.get(143).get(r).get(c);
				b145	=	(float) DataCruncher.DATA.get(144).get(r).get(c);
				b146	=	(float) DataCruncher.DATA.get(145).get(r).get(c);
				b147	=	(float) DataCruncher.DATA.get(146).get(r).get(c);
				b148	=	(float) DataCruncher.DATA.get(147).get(r).get(c);
				b149	=	(float) DataCruncher.DATA.get(148).get(r).get(c);
				b150	=	(float) DataCruncher.DATA.get(149).get(r).get(c);
				b151	=	(float) DataCruncher.DATA.get(150).get(r).get(c);
				b152	=	(float) DataCruncher.DATA.get(151).get(r).get(c);
				b153	=	(float) DataCruncher.DATA.get(152).get(r).get(c);
				b154	=	(float) DataCruncher.DATA.get(153).get(r).get(c);
				b155	=	(float) DataCruncher.DATA.get(154).get(r).get(c);
				b156	=	(float) DataCruncher.DATA.get(155).get(r).get(c);
				b157	=	(float) DataCruncher.DATA.get(156).get(r).get(c);
				b158	=	(float) DataCruncher.DATA.get(157).get(r).get(c);
				b159	=	(float) DataCruncher.DATA.get(158).get(r).get(c);
				b160	=	(float) DataCruncher.DATA.get(159).get(r).get(c);
				b161	=	(float) DataCruncher.DATA.get(160).get(r).get(c);
				b162	=	(float) DataCruncher.DATA.get(161).get(r).get(c);
				b163	=	(float) DataCruncher.DATA.get(162).get(r).get(c);
				b164	=	(float) DataCruncher.DATA.get(163).get(r).get(c);
				b165	=	(float) DataCruncher.DATA.get(164).get(r).get(c);
				b166	=	(float) DataCruncher.DATA.get(165).get(r).get(c);
				b167	=	(float) DataCruncher.DATA.get(166).get(r).get(c);
				b168	=	(float) DataCruncher.DATA.get(167).get(r).get(c);
				b169	=	(float) DataCruncher.DATA.get(168).get(r).get(c);
				b170	=	(float) DataCruncher.DATA.get(169).get(r).get(c);
				b171	=	(float) DataCruncher.DATA.get(170).get(r).get(c);
				b172	=	(float) DataCruncher.DATA.get(171).get(r).get(c);
				b173	=	(float) DataCruncher.DATA.get(172).get(r).get(c);
				b174	=	(float) DataCruncher.DATA.get(173).get(r).get(c);
				b175	=	(float) DataCruncher.DATA.get(174).get(r).get(c);
				b176	=	(float) DataCruncher.DATA.get(175).get(r).get(c);
				b177	=	(float) DataCruncher.DATA.get(176).get(r).get(c);
				b178	=	(float) DataCruncher.DATA.get(177).get(r).get(c);
				b179	=	(float) DataCruncher.DATA.get(178).get(r).get(c);
				b180	=	(float) DataCruncher.DATA.get(179).get(r).get(c);
				b181	=	(float) DataCruncher.DATA.get(180).get(r).get(c);
				b182	=	(float) DataCruncher.DATA.get(181).get(r).get(c);
				b183	=	(float) DataCruncher.DATA.get(182).get(r).get(c);
				b184	=	(float) DataCruncher.DATA.get(183).get(r).get(c);
				b185	=	(float) DataCruncher.DATA.get(184).get(r).get(c);
				b186	=	(float) DataCruncher.DATA.get(185).get(r).get(c);
				b187	=	(float) DataCruncher.DATA.get(186).get(r).get(c);
				b188	=	(float) DataCruncher.DATA.get(187).get(r).get(c);
				b189	=	(float) DataCruncher.DATA.get(188).get(r).get(c);
				b190	=	(float) DataCruncher.DATA.get(189).get(r).get(c);
				b191	=	(float) DataCruncher.DATA.get(190).get(r).get(c);
				b192	=	(float) DataCruncher.DATA.get(191).get(r).get(c);
				b193	=	(float) DataCruncher.DATA.get(192).get(r).get(c);
				b194	=	(float) DataCruncher.DATA.get(193).get(r).get(c);
				b195	=	(float) DataCruncher.DATA.get(194).get(r).get(c);
				b196	=	(float) DataCruncher.DATA.get(195).get(r).get(c);
				b197	=	(float) DataCruncher.DATA.get(196).get(r).get(c);
				b198	=	(float) DataCruncher.DATA.get(197).get(r).get(c);
				b199	=	(float) DataCruncher.DATA.get(198).get(r).get(c);
				b200	=	(float) DataCruncher.DATA.get(199).get(r).get(c);


				((GPIndividual)ind).trees[0].child.eval(
						state,threadnum,input,stack,((GPIndividual)ind),this);

				boolean isColor = IOInit.compareTwoPixels(2,IOInit.train_ggl_sat_img_tgi,
						new int[]{Integer.parseInt(targetColor[0]),Integer.parseInt(targetColor[1]),Integer.parseInt(targetColor[2])},c,r);

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
         
		
		
	}


}

