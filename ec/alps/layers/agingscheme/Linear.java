package ec.alps.layers.agingscheme;

import java.util.ArrayList;

import ec.alps.layers.AgingScheme;
import ec.alps.layers.Layer;

/**
 * 
 * Linear: 12345678
 * This values are multipled by the age-gap parameter to determine the maximum age per layer
 * 
 * @author Anthony Awuley
 *
 */
public class Linear extends AgingScheme {

	public Linear() 
	{
		
	}
	
	public String toString()
	{
		return this.getClass().getName()+"aging scheme selected ";
	}
	
	public ArrayList<Layer> agingScheme()
	{
		layers = new ArrayList<>();
		
		//this.toString(" with an age gap of "+ ageGap +" and "+ ageLayers +" layers");
		
		for(int i=0; i<AgingScheme.alpsAgeLayers;i++)
		{
			Layer layer = new Layer();
			layer.setMaxAgeLayer((i+1)*AgingScheme.alpsAgeGap);
			layer.setIsActive(Boolean.FALSE);
			layer.setId(i);
			
			if(i==0)
			{
				layer.setIsBottomLayer(Boolean.TRUE);
				layer.setGenerations(layer.getMaxAge());
			}
			else
			{
				layer.setIsBottomLayer(Boolean.FALSE);
				layer.setGenerations( //get the difference between maximum of this layer and previous layer
						           layer.getMaxAge() - layers.get(i-1).getMaxAge());
			}
			layers.add(layer);
		}
		
		/*
		 * there is no age-limit to the last layer so as to be able to keep the 
		 * best individuals from the most promising (longest running) search
		 */
		//layers.get(layers.size()-1).setMaxAgeLayer(Constants.ALPS_MAX_AGE_LAST_LAYER);
		
		return layers;
	}
	

}
