package ec.alps.layers.agingscheme;


import java.util.ArrayList;

import ec.alps.Engine;
import ec.alps.layers.Layer;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public abstract class AgingScheme {

	public final static String AGING_SCHEME = "aging-scheme";
	public final static String AGE_GAP      = "age-gap";
	public final static String AGE_LAYERS   = "number-of-layers";


	public  static int alpsAgeGap                = 0;
	public  static int alpsAgeLayers             = 0;


	public Parameter defaultBase()
	{
		return Engine.base().push(AGING_SCHEME);
	}



	public void setup(final ParameterDatabase base)
	{
		/*
		if (!state.parameters.exists(base.push(AGE_GAP), null))
			 state.output.fatal("age gap parameter \"alps."+AGE_GAP+"\" has not been defined ");
		if (!state.parameters.exists(base.push(AGE_LAYERS), null))
			 state.output.fatal("number of layers parameter \"alps."+AGE_LAYERS+"\" has not been defined ");       
		 */

		alpsAgeGap       = base.getInt(Engine.base().push(AGE_GAP), null); 
		alpsAgeLayers    = base.getInt(Engine.base().push(AGE_LAYERS), null); 

	}


	public abstract String toString();

	/**
	 * 
	 * @param ageGap
	 * @return
	 */
	public abstract ArrayList<Layer> agingScheme();


	//public abstract ArrayList<Layer> agingScheme(int ageGap,int ageLayers);





}
