package ec.alps.layers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import ec.EvolutionState;
import ec.util.ParameterDatabase;



public class Layer implements LayerInterface {

	private int maxAgeLayer;
	private boolean isBottomLayer = Boolean.FALSE;
	private boolean isActive = Boolean.FALSE;
	public ParameterDatabase parameterDatabase;
	public EvolutionState evolutionState;
	/** 
	 * This stores the number of generations required for a layer
	 * It is the same as the number of default generaitons specified in the parameter file
	 */
	private int generations;
	private int layerId;
	
	/** 
	 * This is initialized during setting up of layers 
	 * It is incremented when a generation is evolved in a layer 
	 */
	public int layerGenerationalCount  = 1; //was 0;
	/**
	 * Keeps count of the number of evaluations in a layer
	 */
	public  int layerEvaluationCount   = 0; 
	
	/** The number of generations required per layer */
	public int numGenerations          = 0;
	
	 /** "The evolution run has not quit --jst default*/
    public int result                  = 2;
    
	public int layerCompleteGenerationCount = 0;
	
	/**
	 * (If do-time is true) How long initialization took in milliseconds, or how 
	 * long the previous generation took to breed to form this generation for 
	 * this layer
	 */
	public long initializationTime;
	/**
	 * (If do-time is true) How long evaluation took in milliseconds this generation 
	 * for this layer
	 */
	public long evaluationTime;
	
	public boolean initializerFlag = true;
	/**
	 * used to count the number of individuals added to a higher layer
	 * mostly useful for steady state evaluation when evolution first starts in a higher
	 * layer
	 * @deprecated NO LONGER USED
	 */
	public int individualCount = 0;
	public int evaluation;
	public int currentJob = 0;
	
	
	//public Map<String, Double>  nodeCountTerminalSet = new LinkedHashMap<String, Double>();
	
	public Layer() 
	{}
	
	public void setup(){}


	@Override
	public boolean getIsActive() 
	{
		return this.isActive;
	}

	@Override
	public void setIsActive(boolean status) 
	{
		this.isActive = status;
	}

	@Override
	public boolean getIsBottomLayer() 
	{
		return this.isBottomLayer;
	}

	@Override
	public void setIsBottomLayer(boolean status) 
	{
		this.isBottomLayer = status;
	}

	/**
	@Override
	public void tryMoveUp(int layer, Population pop) 
	{
		// TODO Auto-generated method stub
		
	} */

	@Override
	public int getMaxAge() 
	{
		return this.maxAgeLayer;
	}

	@Override
	public void setMaxAgeLayer(int age) 
	{
		this.maxAgeLayer = age;
	}
  

	@Override
	public int getGenerations() 
	{
		return this.generations;
	}



	@Override
	public void setGenerations(int count) 
	{
		this.generations = count;
	}



	@Override
	public int getId() 
	{
		return this.layerId;
	}



	@Override
	public void setId(int id) {
		this.layerId = id;
	}


	@Override
	public int getEvaluations() 
	{
		return this.evaluation;
	}


	@Override
	public void setEvaluations(int evaluation) 
	{
		this.evaluation = evaluation;
	}
	

}
