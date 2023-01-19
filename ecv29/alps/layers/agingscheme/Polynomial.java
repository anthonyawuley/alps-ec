package ec.alps.layers.agingscheme;

import java.util.ArrayList;

import ec.alps.layers.AgingScheme;
import ec.alps.layers.Layer;


/**
 * 
 * n^2: 1 2 4 9 16 25 49 64
 * This values are multipled by the age-gap parameter to determine the maximum age per layer
 * 
 * @author Anthony Awuley
 *
 */
public class Polynomial  extends AgingScheme {

	
	public Polynomial() 
	{ }
	
	public String toString()
	{
		return this.getClass().getName()+"aging scheme selected ";
	}
	
	/**
	 * @return n^2
	 */
	public ArrayList<Layer> agingScheme()
	{
		//this.toString(" with an age gap of "+ ageGap +" and "+ ageLayers +" layers");
		layers  = new ArrayList<>();
		
		for(int i=0; i<AgingScheme.alpsAgeLayers;i++)
		{
			Layer layer = new Layer();
			layer.setIsActive(Boolean.FALSE);
			//layer.setGenerationalCount(0); //initialize generational count
			layer.setId(i);
			
			if(i==0)
			{
				layer.setMaxAgeLayer((int) 1*AgingScheme.alpsAgeGap);//0:1
				layer.setIsBottomLayer(Boolean.TRUE);
				layer.setGenerations(layer.getMaxAge());
			}
			else
			{
				if(i==1) 
					layer.setMaxAgeLayer((int) 2*AgingScheme.alpsAgeGap); // 1:2
				else
					layer.setMaxAgeLayer((int) Math.pow(i,2)*AgingScheme.alpsAgeGap);	
				
				layer.setIsBottomLayer(Boolean.FALSE);
				layer.setGenerations( //get the difference between maximum of this layer and previous layer
				           layer.getMaxAge() - layers.get(i-1).getMaxAge());
			}
			layers.add(layer);
			//layers.set(i, layer);
		}
		/*
		 * there is no age-limit to the last layer so as to be able to keep the 
		 * best individuals from the most promising (longest running) search
		 */
		layers.get(layers.size()-1).setMaxAgeLayer(Integer.MAX_VALUE);
		// layers.get(layers.size()-1).setGenerations(
		// 		Constants.ALPS_MAX_AGE_LAST_LAYER - layers.get(layers.size()-2).getMaxAge());
		
		return layers;
	}

}
