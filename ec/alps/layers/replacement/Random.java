package ec.alps.layers.replacement;

import ec.Population;
import ec.alps.layers.ALPSLayers;
import ec.alps.layers.Replacement;



public class Random  extends Replacement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1;

	public Random() 
	{
	}

	public String toString()
	{
		return "Random Replacement";
	}

	@Override
	public void layerMigrations(ALPSLayers alpsLayers, Population current) {
		// TODO Auto-generated method stub

	}



}
